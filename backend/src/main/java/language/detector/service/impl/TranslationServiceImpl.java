package language.detector.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import language.detector.service.TranslationService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

@Service
public class TranslationServiceImpl implements TranslationService {

	 private final TranslateClient translateClient;

		@Autowired
	    public TranslationServiceImpl(@Value("${vocalKord.aws.accessKey}") String accessKey,
	                              @Value("${vocalKord.aws.secretAccessKey}") String secretKey) {
	        AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
	        this.translateClient = TranslateClient.builder()
	                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
	                .region(Region.US_WEST_2)
	                .build();
	    }
	
	@Override
	public String translateText(String text, String sourceLanguageCode, String targetLanguageCode) {
		TranslateTextRequest request = TranslateTextRequest.builder()
                .sourceLanguageCode(sourceLanguageCode)
                .targetLanguageCode(targetLanguageCode)
                .text(text)
                .build();
        TranslateTextResponse response = translateClient.translateText(request);
        String translatedText = response.translatedText();
        return translatedText;
	}

}
