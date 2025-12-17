package com.cagepa.pmg.smc.adapter;

import com.cagepa.pmg.infra.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdaptadorAnalogicoModeloB implements IProcessadorImagem {
    private final java.util.Map<String, Long> lastProcessedTimes = new java.util.HashMap<>();
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
            // Preprocess image: Crop to ROI, convert to grayscale, and binarize
            tempImage = preprocessarImagem(imagem);

            // Use PSM 7 (Treat the image as a single text line)
            // Configure whitelist to only allow digits
            ProcessBuilder pb = new ProcessBuilder(
                    "tesseract",
                    tempImage.getAbsolutePath(),
                    "stdout",
                    "--psm", "6",
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
                                "Adapter B: Tesseract OCR realizado em " + imagem.getName() + " -> Valor Bruto: "
                                        + finalValue);

                        // Apply Smart Rounding (Inference)
                        double roundedValue = aplicarArredondamentoInteligente(finalValue);
                        Logger.getInstance().logInfo("Adapter B: Valor Arredondado: " + roundedValue);

                        return roundedValue;
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
            // Debug: Keep file
            // if (tempImage != null && tempImage.exists()) {
            // tempImage.delete();
            // }
        }

        // If Tesseract fails, return 0.0 (No Fallback)
        Logger.getInstance().logError("Adapter B: Falha no OCR para " + imagem.getName());
        return 0.0;
    }

    private double aplicarArredondamentoInteligente(double valor) {
        // Round to nearest 0.1 to eliminate OCR noise (e.g., 0.07 deviation)
        // 19.38 -> 19.40
        return Math.round(valor * 10.0) / 10.0;
    }

    private File preprocessarImagem(File original) throws java.io.IOException {
        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            if (original.exists() && original.length() > 0) {
                break;
            }
            try {
                Thread.sleep(100); // Esperar 100ms antes de tentar novamente
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new java.io.IOException("Interrompido enquanto esperava pela imagem");
            }
        }

        if (!original.exists() || original.length() == 0) {
            throw new java.io.IOException(
                    "Arquivo de imagem vazio ou inexistente após retentativas: " + original.getAbsolutePath());
        }

        java.awt.image.BufferedImage img = null;
        try {
            img = javax.imageio.ImageIO.read(original);
        } catch (Exception e) {
            // Ignorar erro pontual se for falha de decodificação momentânea
        }

        if (img == null) {
            // Tentar mais uma vez após sleep maior se falhou no ImageIO
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
            img = javax.imageio.ImageIO.read(original);
            if (img == null) {
                throw new java.io.IOException(
                        "Falha ao decodificar imagem (ImageIO retornou null): " + original.getAbsolutePath());
            }
        }

        // ROI Estimation based on HidrometroUI.cpp:
        // x_offset = 307, y_offset = 191 (baseline). Font size 18.
        // Digits span roughly from x=300 to x=450.
        // y range roughly 170 to 200.
        // Let's take a safe crop.
        int x = 300; // Original X
        int y = 160;
        int w = 160;
        int h = 50;

        // Ensure crop is within bounds
        x = Math.max(0, x);
        y = Math.max(0, y);
        w = Math.min(w, img.getWidth() - x);
        h = Math.min(h, img.getHeight() - y);

        java.awt.image.BufferedImage cropped = img.getSubimage(x, y, w, h);

        // Upscale the cropped image by 2x to improve OCR accuracy
        int scaledW = w * 2;
        int scaledH = h * 2;
        java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(scaledW, scaledH,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(cropped, 0, 0, scaledW, scaledH, null);
        g2d.dispose();

        // Binarize the image (Thresholding)
        java.awt.image.BufferedImage processed = binarizarImagem(scaled);

        File tempFile = new File("debug_processed.png"); // Fixed path for debugging
        javax.imageio.ImageIO.write(processed, "png", tempFile);
        return tempFile;
    }

    private java.awt.image.BufferedImage binarizarImagem(java.awt.image.BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] histogram = new int[256];

        // 1. Convert to grayscale and compute histogram
        int[][] grayData = new int[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);
                // Use Min(G, B) to maximize darkness for Red (low G, low B) and Black (low G,
                // low B)
                // White has high G and high B.
                int gray = Math.min(g, b);
                grayData[x][y] = gray;
            }
        }

        // Update histogram based on grayData (Original)
        histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                histogram[grayData[x][y]]++;
            }
        }

        // 2. Otsu's Thresholding
        int total = width * height;
        float sum = 0;
        for (int i = 0; i < 256; i++)
            sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float varMax = 0;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += histogram[t];
            if (wB == 0)
                continue;
            wF = total - wB;
            if (wF == 0)
                break;

            sumB += (float) (t * histogram[t]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }
        Logger.getInstance().logInfo("Adapter B: Otsu Threshold calculado: " + threshold);

        // 3. Apply Threshold
        java.awt.image.BufferedImage binarized = new java.awt.image.BufferedImage(width, height,
                java.awt.image.BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grayData[x][y] < threshold) {
                    binarized.setRGB(x, y, 0x000000); // Black
                } else {
                    binarized.setRGB(x, y, 0xFFFFFF); // White
                }
            }
        }
        return binarized;
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
                        com.cagepa.pmg.sgu.Usuario u = sgu.getUsuarioPorHidrometro(idSHA);
                        if (u == null) {
                            continue;
                        }

                        boolean isModelB = false;
                        for (com.cagepa.pmg.sgu.Hidrometro h : u.getHidrometros()) {
                            if (h.getId().equals(idSHA) && "B".equalsIgnoreCase(h.getModelo())) {
                                isModelB = true;
                                break;
                            }
                        }

                        if (!isModelB) {
                            continue;
                        }

                        // Latest file
                        File[] imagens = arquivo.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg") ||
                                name.toLowerCase().endsWith(".png"));

                        if (imagens != null && imagens.length > 0) {
                            File maisRecente = imagens[0];
                            long maxTime = maisRecente.lastModified();

                            for (int i = 1; i < imagens.length; i++) {
                                long time = imagens[i].lastModified();
                                if (time > maxTime) {
                                    maxTime = time;
                                    maisRecente = imagens[i];
                                }
                            }

                            // STRICT CHECK: Only process if image is recent (< 5 seconds)
                            long diff = System.currentTimeMillis() - maxTime;
                            if (diff < 5000) {
                                // DEDUPLICATION: Check if this file timestamp was already processed
                                long lastProcessed = lastProcessedTimes.getOrDefault(idSHA, 0L);
                                if (maxTime > lastProcessed) {
                                    Logger.getInstance().logInfo("Adapter B: Imagem recente encontrada: "
                                            + maisRecente.getName() + " para " + idSHA);
                                    leituras.add(new LeituraDados(idSHA, maisRecente));
                                    lastProcessedTimes.put(idSHA, maxTime);
                                }
                            }
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
            com.cagepa.pmg.sgu.Usuario u = sgu.getUsuarioPorHidrometro(idSHA);
            if (u != null) {
                for (com.cagepa.pmg.sgu.Hidrometro h : u.getHidrometros()) {
                    if (h.getId().equals(idSHA) && "B".equalsIgnoreCase(h.getModelo())) {
                        return new LeituraDados(idSHA, imagem);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String verificarStatus(String idSHA) {
        for (File diretorio : diretoriosMonitorados) {
            if (!diretorio.exists())
                continue;
            File[] arquivos = diretorio.listFiles();
            if (arquivos == null)
                continue;

            for (File arquivo : arquivos) {
                if (arquivo.isDirectory()) {
                    String nomeDir = arquivo.getName();
                    String dirId = null;

                    if (nomeDir.startsWith("Medições_")) {
                        dirId = nomeDir.substring(9);
                    } else if (nomeDir.startsWith("Scan_")) {
                        dirId = nomeDir.substring(5);
                    }

                    if (dirId != null && dirId.equals(idSHA)) {
                        File[] imagens = arquivo.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg") ||
                                name.toLowerCase().endsWith(".png"));

                        if (imagens != null && imagens.length > 0) {
                            File maisRecente = imagens[0];
                            long maxTime = maisRecente.lastModified();

                            for (int i = 1; i < imagens.length; i++) {
                                long time = imagens[i].lastModified();
                                if (time > maxTime) {
                                    maxTime = time;
                                    maisRecente = imagens[i];
                                }
                            }

                            long diff = System.currentTimeMillis() - maxTime;
                            // If image is younger than 5 seconds, it's running
                            if (diff < 5000) {
                                return "EM EXECUÇÃO";
                            }
                        }
                    }
                }
            }
        }
        return "PARADO";
    }
}
