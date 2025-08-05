package language.detector.service;

import java.util.Map;

public interface LanguageDetectorService {

	public void loadLanguagesFromIndexFile(String indexFilePath);
	
	public Map<String, Integer> loadWordCountsFromFile(String filePath);
	
	public String detect(String string);
	
	public String translate(String fromLanguage,String toLanguage,String message);
}
