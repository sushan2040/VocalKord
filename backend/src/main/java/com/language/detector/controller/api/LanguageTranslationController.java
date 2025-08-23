package com.language.detector.controller.api;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.language.detector.MarianModelPreloader;
import com.language.detector.bean.TranscriptionResponse;

import language.detector.service.LanguageDetectorService;
import language.detector.service.TranslationService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobResponse;
import software.amazon.awssdk.services.transcribe.model.Media;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus;

@RestController
@RequestMapping(value = "/api")
public class LanguageTranslationController {

    private static final int DAILY_TRANSLATION_LIMIT = 10;
    private final ConcurrentHashMap<String, AtomicInteger> translationCounts = new ConcurrentHashMap<>();
    private volatile LocalDate lastResetDate = LocalDate.now();

    @Autowired
    LanguageDetectorService languageDetectorService;

    @Autowired
    TranscribeClient transcribeClient;

    @Autowired
    S3Client client;

    @Autowired
    TranslationService translationService;

    // Reset translation counts daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetTranslationCounts() {
        translationCounts.clear();
        lastResetDate = LocalDate.now();
        System.out.println("Translation counts reset at " + lastResetDate);
    }

    private boolean canTranslate(HttpServletRequest request) {
        // Check if reset is needed (in case scheduler fails or during testing)
        if (!LocalDate.now().equals(lastResetDate)) {
            resetTranslationCounts();
        }

        String clientIp = getClientIp(request);
        System.out.println("Client IP :"+clientIp+" made request for translation ");
        AtomicInteger count = translationCounts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
        System.out.println("Client IP count is :"+count+"");
        if (count.get() >= DAILY_TRANSLATION_LIMIT) {
            return false;
        }
        count.incrementAndGet();
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        } else {
            // X-Forwarded-For may contain multiple IPs; take the first one
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    @PostMapping(value = "/translate")
    public ResponseEntity<String> fetchTranslatedText(
            @RequestParam("text") String text,
            @RequestParam("fromLanguage") String fromLanguage,
            @RequestParam("toLanguage") String toLanguage,
            HttpServletRequest request) {
        if (!canTranslate(request)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Daily translation limit of " + DAILY_TRANSLATION_LIMIT + " reached.");
        }
        return ResponseEntity.ok(translationService.translateText(text, fromLanguage, toLanguage));
    }

    @GetMapping("/get-languages")
    public ResponseEntity<Map<String, String>> getLanguages() {
        Map<String, String> languages = new LinkedHashMap<>(); // Keeps insertion order
        languages.put("af", "Afrikaans");
        languages.put("am", "Amharic");
        languages.put("ar", "Arabic");
        languages.put("az", "Azerbaijani");
        languages.put("be", "Belarusian");
        languages.put("bg", "Bulgarian");
        languages.put("bn", "Bengali");
        languages.put("bs", "Bosnian");
        languages.put("ca", "Catalan");
        languages.put("cs", "Czech");
        languages.put("cy", "Welsh");
        languages.put("da", "Danish");
        languages.put("de", "German");
        languages.put("el", "Greek");
        languages.put("en", "English");
        languages.put("es", "Spanish");
        languages.put("et", "Estonian");
        languages.put("fa", "Persian");
        languages.put("fi", "Finnish");
        languages.put("fr", "French");
        languages.put("gu", "Gujarati");
        languages.put("he", "Hebrew");
        languages.put("hi", "Hindi");
        languages.put("hr", "Croatian");
        languages.put("hu", "Hungarian");
        languages.put("hy", "Armenian");
        languages.put("id", "Indonesian");
        languages.put("is", "Icelandic");
        languages.put("it", "Italian");
        languages.put("ja", "Japanese");
        languages.put("jv", "Javanese");
        languages.put("ka", "Georgian");
        languages.put("kk", "Kazakh");
        languages.put("km", "Khmer");
        languages.put("kn", "Kannada");
        languages.put("ko", "Korean");
        languages.put("lt", "Lithuanian");
        languages.put("lv", "Latvian");
        languages.put("ml", "Malayalam");
        languages.put("mn", "Mongolian");
        languages.put("mr", "Marathi");
        languages.put("ms", "Malay");
        languages.put("my", "Burmese");
        languages.put("ne", "Nepali");
        languages.put("nl", "Dutch");
        languages.put("no", "Norwegian");
        languages.put("pa", "Punjabi");
        languages.put("pl", "Polish");
        languages.put("pt", "Portuguese");
        languages.put("ro", "Romanian");
        languages.put("ru", "Russian");
        languages.put("si", "Sinhala");
        languages.put("sk", "Slovak");
        languages.put("sl", "Slovenian");
        languages.put("sq", "Albanian");
        languages.put("sr", "Serbian");
        languages.put("sv", "Swedish");
        languages.put("sw", "Swahili");
        languages.put("ta", "Tamil");
        languages.put("te", "Telugu");
        languages.put("th", "Thai");
        languages.put("tl", "Tagalog");
        languages.put("tr", "Turkish");
        languages.put("uk", "Ukrainian");
        languages.put("ur", "Urdu");
        languages.put("vi", "Vietnamese");
        languages.put("zh", "Chinese");
        return ResponseEntity.ok(languages);
    }

    @Async
    @GetMapping(value = "/download-models")
    public void downloadModels() {
        System.out.println("🔄 Starting model preloading in background thread...");
        MarianModelPreloader modelPreloader = new MarianModelPreloader();
        modelPreloader.preloadModels();
    }

    @PostMapping("/translate-document")
    public ResponseEntity<byte[]> translateDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam("fromLanguage") String fromLanguage,
            @RequestParam("toLanguage") String toLanguage,
            HttpServletRequest request) {
        if (!canTranslate(request)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(("Daily translation limit of " + DAILY_TRANSLATION_LIMIT + " reached.").getBytes());
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename).toLowerCase();
        String extractedText;

        try {
            // Extract text based on file type
            switch (extension) {
                case "txt":
                    extractedText = new String(file.getBytes(), StandardCharsets.UTF_8);
                    break;
                case "pdf":
                    try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                        extractedText = new PDFTextStripper().getText(doc);
                    }
                    break;
                case "docx":
                    try (XWPFDocument docx = new XWPFDocument(file.getInputStream())) {
                        extractedText = new XWPFWordExtractor(docx).getText();
                    }
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }

            // Validate language inputs
            if (fromLanguage == null || fromLanguage.isBlank() ||
                toLanguage == null || toLanguage.isBlank()) {
                return ResponseEntity.badRequest().body("Language codes cannot be empty".getBytes());
            }

            // Translate text
            String translatedText = translationService.translateText(extractedText, fromLanguage, toLanguage);

            // Generate output file
            byte[] translatedFile;
            MediaType mediaType;

            switch (extension) {
                case "txt":
                    translatedFile = translatedText.getBytes(StandardCharsets.UTF_8);
                    mediaType = MediaType.TEXT_PLAIN;
                    break;
                case "pdf":
                    try (ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
                         PDDocument doc = new PDDocument()) {
                        PDPage page = new PDPage();
                        doc.addPage(page);

                        // Load a Unicode font
                        InputStream fontStream = new FileInputStream("src/main/resources/NotoSans-VariableFont_wdth,wght.ttf");
                        PDType0Font font = PDType0Font.load(doc, fontStream);

                        try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                            contentStream.beginText();
                            contentStream.setFont(font, 12);
                            contentStream.setLeading(14.5f);
                            contentStream.newLineAtOffset(25, 700);

                            String[] lines = translatedText.split("\\R");
                            for (String line : lines) {
                                contentStream.showText(line);
                                contentStream.newLine();
                            }
                            System.out.println("Translated text length: " + translatedText.length());
                            System.out.println("Translated text preview: " + translatedText.substring(0, Math.min(100, translatedText.length())));

                            contentStream.endText();
                        }

                        doc.save(pdfOut);
                        translatedFile = pdfOut.toByteArray();
                    }
                    mediaType = MediaType.APPLICATION_PDF;
                    break;
                case "docx":
                    try (ByteArrayOutputStream docxOut = new ByteArrayOutputStream()) {
                        XWPFDocument newDoc = new XWPFDocument();
                        newDoc.createParagraph().createRun().setText(translatedText);
                        newDoc.write(docxOut);
                        translatedFile = docxOut.toByteArray();
                    }
                    mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            // Build response with proper headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("translated-" + originalFilename)
                    .build());

            return new ResponseEntity<>(translatedFile, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    @PostMapping("/transcribe")
    public ResponseEntity<TranscriptionResponse> transcribeAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fromLanguage") String fromLanguage) {

        String bucketName = "s3audioinput";
        String outputBucketName = "s3audiooutput";
        String fileName = "recordings/" + System.currentTimeMillis() + "-" + file.getOriginalFilename();

        try {
            // Upload the file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            System.out.println("✅ File uploaded to S3: " + fileName);

            // Prepare the Transcribe Job
            String mediaFormat = fileName.substring(fileName.lastIndexOf('.') + 1);
            String s3Uri = "s3://" + bucketName + "/" + fileName;
            String jobName = "job-" + System.currentTimeMillis();

            StartTranscriptionJobRequest jobRequest = StartTranscriptionJobRequest.builder()
                    .transcriptionJobName(jobName)
                    .languageCode(fromLanguage + "-US")
                    .mediaFormat(mediaFormat)
                    .media(Media.builder().mediaFileUri(s3Uri).build())
                    .outputBucketName(outputBucketName)
                    .build();

            transcribeClient.startTranscriptionJob(jobRequest);
            System.out.println("🔁 Transcription job started: " + jobName);

            // Poll for completion
            TranscriptionJob transcriptionJob;
            do {
                Thread.sleep(3000);
                GetTranscriptionJobResponse getJobResponse = transcribeClient.getTranscriptionJob(
                        GetTranscriptionJobRequest.builder().transcriptionJobName(jobName).build()
                );
                transcriptionJob = getJobResponse.transcriptionJob();
                System.out.println("⏳ Job status: " + transcriptionJob.transcriptionJobStatus());
            } while (transcriptionJob.transcriptionJobStatus() == TranscriptionJobStatus.IN_PROGRESS);

            // Handle result
            if (transcriptionJob.transcriptionJobStatus() == TranscriptionJobStatus.COMPLETED) {
                String transcriptUrl = transcriptionJob.transcript().transcriptFileUri();
                System.out.println("✅ Transcription completed: " + transcriptUrl);
                try {
                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(outputBucketName)
                            .key(transcriptUrl.split("/")[transcriptUrl.split("/").length - 1])
                            .build();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(client.getObject(getObjectRequest), StandardCharsets.UTF_8))) {

                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        ObjectMapper mapper = new ObjectMapper();
                        TranscriptionResponse response = mapper.readValue(sb.toString(), TranscriptionResponse.class);
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TranscriptionResponse());
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new TranscriptionResponse());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TranscriptionResponse());
        }
    }
}