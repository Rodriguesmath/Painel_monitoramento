package com.cagepa.pmg.smc.adapter;

import com.cagepa.pmg.infra.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdaptadorAnalogicoModeloB implements IProcessadorImagem {
    private final List<File> diretoriosMonitorados;

    public AdaptadorAnalogicoModeloB() {
        this.diretoriosMonitorados = new ArrayList<>();
        adicionarDiretorio("input/modeloB");
        // Also monitor the actual simulator output directory
        adicionarDiretorio("Simulador-Hidrometro-B");
    }

    @Override
    public void adicionarDiretorio(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!diretoriosMonitorados.contains(dir)) {
            diretoriosMonitorados.add(dir);
            Logger.getInstance().logInfo("Adapter B: Diretório adicionado: " + path);
        }
    }

    @Override
    public void removerDiretorio(String path) {
        File dir = new File(path);
        if (diretoriosMonitorados.remove(dir)) {
            Logger.getInstance().logInfo("Adapter B: Diretório removido: " + path);
        }
    }

    @Override
    public double realizarOCR(File imagem) {
        File tempImage = null;
        try {
            // Preprocess image: Crop to ROI and convert to grayscale
            tempImage = preprocessarImagem(imagem);

            // Use PSM 7 (Treat the image as a single text line)
            // Configure whitelist to only allow digits
            ProcessBuilder pb = new ProcessBuilder(
                    "tesseract",
                    tempImage.getAbsolutePath(),
                    "stdout",
                    "--psm", "7",
                    "-c", "tessedit_char_whitelist=0123456789");
            Process p = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = p.waitFor();
            if (exitCode == 0) {
                String rawOutput = output.toString().trim();
                Logger.getInstance().logInfo("Adapter B: Tesseract Raw Output: '" + rawOutput + "'");

                // CLEANING STRATEGY:
                // 1. Remove common noise separators
                String cleaned = rawOutput.replaceAll("[/|\\\\.]", "");
                // 2. Remove all non-digit characters
                cleaned = cleaned.replaceAll("[^0-9]", "");

                Logger.getInstance().logInfo("Adapter B: Limpeza: '" + rawOutput + "' -> '" + cleaned + "'");

                if (cleaned.length() > 0) {
                    try {
                        // Parse as integer first
                        double rawValue = Double.parseDouble(cleaned);
                        // MODEL B SPECIFIC: Last 2 digits are red (decimal places)
                        // Example: "000170" -> 1.70
                        double finalValue = rawValue / 100.0;

                        Logger.getInstance().logInfo(
                                "Adapter B: Tesseract OCR realizado em " + imagem.getName() + " -> Valor: "
                                        + finalValue);
                        return finalValue;
                    } catch (NumberFormatException e) {
                        Logger.getInstance().logError("Adapter B: Falha ao converter valor limpo: " + cleaned);
                    }
                } else {
                    Logger.getInstance()
                            .logInfo("Adapter B: Tesseract retornou vazio (após limpeza) para " + imagem.getName());
                }
            } else {
                java.io.BufferedReader errorReader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorOutput.append(errorLine);
                }
                Logger.getInstance().logInfo(
                        "Adapter B: Tesseract falhou (exit code " + exitCode + "). Erro: " + errorOutput.toString());
            }
        } catch (Exception e) {
            Logger.getInstance().logInfo("Adapter B: Tesseract erro de execução: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (tempImage != null && tempImage.exists()) {
                tempImage.delete();
            }
        }

        // Fallback: Parse filename if OCR fails
        String nome = imagem.getName();
        try {
            String valorStr = nome.substring(0, nome.lastIndexOf('.'));
            double valor = Double.parseDouble(valorStr);
            Logger.getInstance()
                    .logInfo("Adapter B: Fallback OCR (nome do arquivo) em " + nome + " -> Valor: " + valor);
            return valor;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private File preprocessarImagem(File original) throws java.io.IOException {
        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(original);

        // ROI Estimation based on HidrometroUI.cpp:
        // x_offset = 307, y_offset = 191 (baseline). Font size 18.
        // Digits span roughly from x=300 to x=450.
        // y range roughly 170 to 200.
        // Let's take a safe crop.
        int x = 300;
        int y = 160;
        int w = 160;
        int h = 50;

        // Ensure crop is within bounds
        x = Math.max(0, x);
        y = Math.max(0, y);
        w = Math.min(w, img.getWidth() - x);
        h = Math.min(h, img.getHeight() - y);

        java.awt.image.BufferedImage cropped = img.getSubimage(x, y, w, h);

        // Convert to Grayscale and Binarize (Simple threshold)
        java.awt.image.BufferedImage processed = new java.awt.image.BufferedImage(w, h,
                java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
        java.awt.Graphics2D g = processed.createGraphics();
        g.drawImage(cropped, 0, 0, null);
        g.dispose();

        File tempFile = File.createTempFile("ocr_processed_", ".png");
        javax.imageio.ImageIO.write(processed, "png", tempFile);
        return tempFile;
    }

    @Override
    public List<LeituraDados> processarNovasImagens() {
        List<LeituraDados> leituras = new ArrayList<>();
        com.cagepa.pmg.sgu.SGU sgu = new com.cagepa.pmg.sgu.SGU();

        for (File diretorio : diretoriosMonitorados) {
            if (!diretorio.exists() || !diretorio.isDirectory()) {
                continue;
            }

            File[] arquivos = diretorio.listFiles();
            if (arquivos == null)
                continue;

            for (File arquivo : arquivos) {
                if (arquivo.isDirectory()) {
                    String nomeDir = arquivo.getName();
                    String idSHA = null;

                    if (nomeDir.startsWith("Medições_")) {
                        idSHA = nomeDir.substring(9);
                    } else if (nomeDir.startsWith("Scan_")) {
                        idSHA = nomeDir.substring(5);
                    }

                    if (idSHA != null) {
                        com.cagepa.pmg.sgu.Usuario u = sgu.getUsuarioPorId(idSHA);
                        if (u == null || !"B".equalsIgnoreCase(u.getModeloAdapter())) {
                            continue;
                        }

                        // Latest file
                        File[] imagens = arquivo.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg") ||
                                name.toLowerCase().endsWith(".png"));

                        if (imagens != null && imagens.length > 0) {
                            java.util.Arrays.sort(imagens,
                                    (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                            File maisRecente = imagens[0];
                            Logger.getInstance().logInfo("Adapter B: Imagem mais recente encontrada: "
                                    + maisRecente.getName() + " para " + idSHA);
                            leituras.add(new LeituraDados(idSHA, maisRecente));
                        }
                    }
                }
            }
        }
        return leituras;
    }

    @Override
    public LeituraDados processarImagem(File imagem) {
        String parentDir = imagem.getParentFile().getName();
        String idSHA = null;

        if (parentDir.startsWith("Medições_")) {
            idSHA = parentDir.substring(9); // Remove "Medições_"
        } else if (parentDir.startsWith("Scan_")) {
            idSHA = parentDir.substring(5); // Remove "Scan_"
        }

        if (idSHA != null) {
            // Verify if user is Model B
            com.cagepa.pmg.sgu.SGU sgu = new com.cagepa.pmg.sgu.SGU();
            com.cagepa.pmg.sgu.Usuario u = sgu.getUsuarioPorId(idSHA);
            if (u != null && "B".equalsIgnoreCase(u.getModeloAdapter())) {
                return new LeituraDados(idSHA, imagem);
            }
        }
        return null;
    }
}
