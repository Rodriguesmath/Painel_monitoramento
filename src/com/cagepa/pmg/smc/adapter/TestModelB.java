package com.cagepa.pmg.smc.adapter;

import java.io.File;

public class TestModelB {
    public static void main(String[] args) {
        try {
            File imageFile = new File("Simulador-Hidrometro-B/Medições_userB/411.jpeg");
            if (!imageFile.exists()) {
                System.err.println("Image file not found: " + imageFile.getAbsolutePath());
                return;
            }

            System.out.println("Testing OCR on: " + imageFile.getName());

            AdaptadorAnalogicoModeloB adapter = new AdaptadorAnalogicoModeloB();
            double result = adapter.realizarOCR(imageFile);

            System.out.println("Result from Adapter: " + result);

            // Also try running tesseract on the full image directly to see if it finds
            // anything
            System.out.println("\n--- Running Tesseract on full image (debug) ---");
            ProcessBuilder pb = new ProcessBuilder("tesseract", imageFile.getAbsolutePath(), "stdout", "--psm", "7",
                    "-c", "tessedit_char_whitelist=0123456789");
            Process p = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Full Image Output: " + line);
            }
            p.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
