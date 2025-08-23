package com.language.detector;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@AutoConfiguration
@ComponentScan(basePackages = {"com.language","language.detector"})
@PropertySource(value = "application.properties")
@EnableWebMvc
@EnableAsync
public class App extends SpringBootServletInitializer
{
	@Autowired
	Environment environment;
	
	private static final org.slf4j.Logger LOGGER=LoggerFactory.getLogger(App.class);
	
		public static void main(String[] args) {
			new SpringApplicationBuilder(App.class).sources(App.class)
					.run(args);

			LOGGER.info(":: Language Detector Web ::        (v1.1.RELEASE)");
			LOGGER.info(":: JAVA version       ::        (" + System.getProperty("java.version") + ")");
			System.out.println(" ****************************************************  ");
			LOGGER.info(" ****************************************************  ");
		}
		@Bean
	    public TranscribeClient transcribeClient() {
			AwsCredentials awsCredentials = AwsBasicCredentials.create(environment.getProperty("vocalKord.aws.accessKey"),environment.getProperty("vocalKord.aws.secretAccessKey"));
	        return TranscribeClient.builder()
	        		.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
	                .region(Region.US_WEST_2) // Change this to your AWS region
	                .build();
	    }

	    @Bean
	    public S3Client s3Client() {
	    	AwsCredentials awsCredentials = AwsBasicCredentials.create(environment.getProperty("vocalKord.aws.accessKey"),environment.getProperty("vocalKord.aws.secretAccessKey"));
	        return S3Client.builder()
	        		.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
	                .region(Region.US_WEST_2) // Same region as your S3 bucket
	                .build();
	    }
		

//    public static void main(String[] args)
//    {
//    	if (args.length == 0) {
//            printHelp();
//            return;
//        }
//
//        String command = args[0];
//
//        switch (command.toLowerCase()) {
//            case "detect":
//                if (args.length < 2) {
//                    System.out.println("Please provide a text to detect.");
//                } else {
//                    String text = args[1];
//                    LanguageDetectorService detector = new LanguageDetectorServiceImpl(); // Your custom class
//                    String lang = detector.detect(text);
//                    System.out.println("Detected Language: " + lang);
//                }
//                break;
//
//            case "translate":
//                    String text = args[1];
//                    LanguageDetectorService translator = new LanguageDetectorServiceImpl(); // Your custom class
//                    String translated = translator.translate(text);
//                    System.out.println("Translated Text: " + translated);
//                break;
//            default:
//                printHelp();
//        }
//        
//    }
//    public static void printHelp() {
//        System.out.println("Usage:");
//        System.out.println("  detect \"text\"               Detect language of input text");
//        System.out.println("  translate \"text\" --to LANG  Translate input text to LANG");
//    }
}
