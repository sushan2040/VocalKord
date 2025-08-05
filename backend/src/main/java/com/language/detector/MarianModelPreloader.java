package com.language.detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

//@Component
public class MarianModelPreloader {

	//@PostConstruct
    public void preloadModels() {
        try {
            // Adjust path to your script that downloads MarianMT models
            ProcessBuilder pb = new ProcessBuilder("python", "src/main/resources/download_models.py");

            pb.redirectErrorStream(true); // Combine stdout and stderr
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("===== Starting MarianMT model preloading =====");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("✅ MarianMT models successfully preloaded.");
            } else {
                System.err.println("❌ Preloading failed with exit code: " + exitCode);
            }

            process.destroy();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
