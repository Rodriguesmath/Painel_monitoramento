package com.cagepa.pmg.infra;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MotorOCR (Adaptee)
 * Encapsula a complexidade de execução do Tesseract via linha de comando.
 * Esta classe representa o sistema legado/externo que precisa ser adaptado.
 */
public class MotorOCR {

    public String processarImagem(File imagem, String... parametrosExtras) throws IOException {
        if (!imagem.exists()) {
            throw new IOException("Arquivo de imagem não encontrado: " + imagem.getAbsolutePath());
        }

        List<String> command = new ArrayList<>();
        command.add("tesseract");
        command.add(imagem.getAbsolutePath());
        command.add("stdout");

        if (parametrosExtras != null) {
            // Split parameters by space to handle combined strings like "--psm 6" correctly
            // if passed as one arg,
            // or rely on caller to pass them split. The plan suggested passing explicit
            // args.
            // Safe approach: add all passed strings.
            for (String param : parametrosExtras) {
                // Simple splitting to ensure ProcessBuilder gets separate args
                command.addAll(Arrays.asList(param.split("\\s+")));
            }
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        Process p = pb.start();

        // Read Output
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(p.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n"); // Preserve newlines for structure if needed
        }

        try {
            int exitCode = p.waitFor();
            if (exitCode == 0) {
                return output.toString().trim();
            } else {
                // Read Error
                java.io.BufferedReader errorReader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorOutput.append(errorLine);
                }
                throw new IOException("Tesseract falhou (Exit Code " + exitCode + "): " + errorOutput.toString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrompido durante execução do OCR", e);
        }
    }
}
