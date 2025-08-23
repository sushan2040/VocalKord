package language.detector.service;

public interface TranslationService {

	public String translateText(String text, String sourceLanguageCode, String targetLanguageCode);
}
