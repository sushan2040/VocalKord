package language.detector.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import language.detector.service.LanguageDetectorService;

@Service
public class LanguageDetectorServiceImpl implements LanguageDetectorService {
	
	String languageFileDir;

	public LanguageDetectorServiceImpl() {
		
	}

	private Logger LOGGER = LogManager.getLogManager().getLogger(LanguageDetectorServiceImpl.class.getName());

	private SimpleDateFormat dateFormat;

	private List<String> likelyLanguages = new ArrayList<String>();
	private Map<String, Integer> languageWordCount = new HashMap<String, Integer>();
	private final Map<String, Map<String, Integer>> langaugeData = new HashMap<>();

	/**
	 * Loads languages data from directory.
	 * This library uses predefined data and matches the input text with predefined loaded data.
	 * @author Sushant Nikam
	 * @param rootDirPath
	 * */
	public void loadLanguagesFromIndexFile(String indexFilePath) {
	    try (InputStream indexStream = getClass().getClassLoader().getResourceAsStream(indexFilePath);
	         BufferedReader indexReader = new BufferedReader(new InputStreamReader(indexStream))) {

	        String relativePath;
	        while ((relativePath = indexReader.readLine()) != null) {
	            String languageCode = relativePath.split("/")[0]; // "af" from "af/af_full.txt"

	            InputStream langStream = getClass().getClassLoader().getResourceAsStream("languagewords/" + relativePath);
	            if (langStream == null) {
	                System.err.println("Failed to load language file: " + relativePath);
	                continue;
	            }

	            BufferedReader reader = new BufferedReader(new InputStreamReader(langStream));
	            Map<String, Integer> wordCounts = new HashMap<>();
	            String line;
	            while ((line = reader.readLine()) != null) {
	                String[] parts = line.split("\\s+");
	                if (parts.length == 2) {
	                    wordCounts.put(parts[0], Integer.parseInt(parts[1]));
	                }
	            }
	            langaugeData.put(languageCode, wordCounts);
	        }

	    } catch (IOException | NullPointerException e) {
	    	System.err.println("Error reading language index or word files");
	    }
	}

	/**
	 * Loads words from a text file which contains language specific letters/words.
	 * This method processes these words and add it into a {@link HashMap} to process further with other 
	 * input letters and words.
	 * There is limited language's data available.
	 * Currently there 62 language specific data available.
	 * In future version more data will be added.
	 * */
	public Map<String, Integer> loadWordCountsFromFile(String filePath) {
		Map<String, Integer> wordCountMap = new HashMap<>();
		InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line;
			// Read file line by line
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("\\s+");
				if (parts.length == 2) {
					String word = parts[0];
					int count = Integer.parseInt(parts[1]);
					wordCountMap.put(word, count);
				} else {
					System.out.println("Skipping invalid line: " + line);
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading file: " + filePath);
			e.printStackTrace();
		}
		return wordCountMap;
	}

	public Map<String, Map<String, Integer>> getLangaugeData() {
		return langaugeData;
	}

	/**
	 * This method is used to detect the language of the input text.
	 * @param can be a word or a sentence or a paragraph.
	 * */
	@Override
	public String detect(String string) {
		loadLanguagesFromIndexFile("index.txt");
	    if (string.split(" ").length == 1) {
	        String word = string.trim();
	        for (Map.Entry<String, Map<String, Integer>> language : langaugeData.entrySet()) {
	            Map<String, Integer> wordMap = language.getValue();
	            if (wordMap.containsKey(word)) {
	                int count = wordMap.get(word);
	                languageWordCount.put(language.getKey(), count);
	            }
	        }

	        // Find the language with max frequency
	        int max = Integer.MIN_VALUE;
	        String finalLanguage = "";

	        for (Map.Entry<String, Integer> freq : languageWordCount.entrySet()) {
	            if (freq.getValue() > max) {
	                max = freq.getValue();
	                finalLanguage = freq.getKey();
	            }
	        }

	        return finalLanguage.isEmpty() ? "" : finalLanguage;
		} else {
			System.out.println("Sentence detected..");
			List<String> wordsList = Arrays.stream(string.split(" ")).collect(Collectors.toList());
			for (String word : wordsList) {
				for (Map.Entry<String, Map<String, Integer>> language : langaugeData.entrySet()) {
					if (language.getValue().containsKey(word)) {
						if (!likelyLanguages.contains(language.getKey()))
							likelyLanguages.add(language.getKey());

						if (languageWordCount.get(language.getKey()) != null) {
							Integer langCount = languageWordCount.get(language.getKey());
							langCount++;
							languageWordCount.put(language.getKey(), langCount);
						} else {
							languageWordCount.put(language.getKey(), 1);
						}
					}
				}
			}
			Integer max = Integer.MIN_VALUE;
			String finalLanguage = "";
			for (Map.Entry<String, Integer> freq : languageWordCount.entrySet()) {
				if (freq.getValue() > max) {
					max = freq.getValue();
					finalLanguage = freq.getKey();
				}
			}
			return finalLanguage;
		}
	}
	public String translate(String fromLang, String toLang, String message) {
	    try {
	        // Create the command to run the Python script with language parameters
	        ProcessBuilder pb = new ProcessBuilder(
	            "python", "src/main/resources/translate.py", fromLang, toLang, message
	        );

	        pb.redirectErrorStream(true);
	        Process process = pb.start();

	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

	        String line;
	        StringBuilder result = new StringBuilder();
	        while ((line = reader.readLine()) != null) {
	            result.append(line).append("\n");
	        }
	        System.out.println("Errors:");
	        while ((line = errorReader.readLine()) != null) {
	            System.err.println(line);
	        }

	        int exitCode = process.waitFor();
	        if (exitCode == 0) {
	        	process.destroy();
	            return result.toString().trim();
	        } else {
	        	process.destroy();
	            System.err.println("Python script failed with exit code " + exitCode);
	            return "";
	        }

	    } catch (IOException | InterruptedException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	
}
