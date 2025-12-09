package com.cagepa.pmg.smc.adapter;

import com.cagepa.pmg.infra.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdaptadorAnalogicoModeloA implements IProcessadorImagem {
    private final List<File> diretoriosMonitorados;

    public AdaptadorAnalogicoModeloA() {
        this.diretoriosMonitorados = new ArrayList<>();
        // Default directory
        adicionarDiretorio("input/modeloA");
    }

    @Override
    public void adicionarDiretorio(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!diretoriosMonitorados.contains(dir)) {
            diretoriosMonitorados.add(dir);
            Logger.getInstance().logInfo("Adapter A: Diretório adicionado: " + path);
        }
    }

    @Override
    public void removerDiretorio(String path) {
        File dir = new File(path);
        if (diretoriosMonitorados.remove(dir)) {
            Logger.getInstance().logInfo("Adapter A: Diretório removido: " + path);
        }
    }

    @Override
    public double realizarOCR(File imagem) {
        // Try Tesseract first
        try {
            ProcessBuilder pb = new ProcessBuilder("tesseract", imagem.getAbsolutePath(), "stdout", "--psm", "7");
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
                String ocrResult = output.toString().replaceAll("[^0-9.]", "");
                if (!ocrResult.isEmpty()) {
                    double valor = Double.parseDouble(ocrResult);
                    Logger.getInstance().logInfo(
                            "Adapter A: Tesseract OCR realizado em " + imagem.getName() + " -> Valor: " + valor);
                    return valor;
                }
            } else {
                Logger.getInstance()
                        .logInfo("Adapter A: Tesseract falhou (exit code " + exitCode + "). Usando fallback.");
            }
        } catch (Exception e) {
            Logger.getInstance()
                    .logInfo("Adapter A: Tesseract não disponível ou erro (" + e.getMessage() + "). Usando fallback.");
        }

        // Fallback: Parse filename: "01.jpeg" -> 1.0
        String nome = imagem.getName();
        try {
            String valorStr = nome.substring(0, nome.lastIndexOf('.'));
            double valor = Double.parseDouble(valorStr);
            Logger.getInstance()
                    .logInfo("Adapter A: Fallback OCR (nome do arquivo) em " + nome + " -> Valor: " + valor);
            return valor;
        } catch (NumberFormatException e) {
            Logger.getInstance().logError("Adapter A: Falha ao realizar OCR (nome inválido): " + nome);
            return 0.0;
        }
    }

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
                    // Check if directory matches user ID (e.g., Medicoes_1999..._teste_img or just
                    // ID)
                    if (nomeDir.contains(idSHA)) {
                        File[] imagens = arquivo.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg") ||
                                name.toLowerCase().endsWith(".png"));

                        if (imagens != null && imagens.length > 0) {
                            java.util.Arrays.sort(imagens,
                                    (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                            File maisRecente = imagens[0];
                            long diff = System.currentTimeMillis() - maisRecente.lastModified();
                            // If image is younger than 30 seconds, it's running
                            if (diff < 30000) {
                                return "EM EXECUÇÃO";
                            }
                        }
                    }
                }
            }
        }
        return "PARADO";
    }

    @Override
    public List<LeituraDados> processarNovasImagens() {
        List<LeituraDados> leituras = new ArrayList<>();
        com.cagepa.pmg.sgu.SGU sgu = new com.cagepa.pmg.sgu.SGU(); // Need SGU to check model

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

                    if (nomeDir.startsWith("Medicao_")) {
                        String[] parts = nomeDir.split("_");
                        if (parts.length >= 2) {
                            idSHA = parts[1];
                        }
                    } else if (nomeDir.startsWith("Medições_")) {
                        String[] parts = nomeDir.split("_");
                        if (parts.length >= 2) {
                            idSHA = parts[1];
                        }
                    } else if (nomeDir.startsWith("Medicoes_")) {
                        String[] parts = nomeDir.split("_");
                        if (parts.length >= 3) {
                            idSHA = parts[2];
                        } else if (parts.length >= 2) {
                            idSHA = parts[1];
                        }
                    }

                    if (idSHA != null) {
                        // BINDING CHECK: Verify if user is assigned to Model A
                        com.cagepa.pmg.sgu.Usuario u = sgu.getUsuarioPorId(idSHA);
                        if (u == null) {
                            // System.out.println("DEBUG: Usuario nao encontrado: " + idSHA);
                            continue;
                        }
                        if (!"A".equalsIgnoreCase(u.getModeloAdapter())) {
                            // System.out.println("DEBUG: Usuario " + idSHA + " nao e Modelo A");
                            continue;
                        }

                        // LATEST FILE CHECK: Find the most recent image
                        File[] imagens = arquivo.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg") ||
                                name.toLowerCase().endsWith(".png"));

                        if (imagens != null && imagens.length > 0) {
                            // Sort by last modified descending
                            java.util.Arrays.sort(imagens,
                                    (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                            File maisRecente = imagens[0];

                            Logger.getInstance().logInfo("Adapter A: Imagem mais recente encontrada: "
                                    + maisRecente.getName() + " para " + idSHA);
                            // Return data with image for deferred processing
                            leituras.add(new LeituraDados(idSHA, maisRecente));
                        } else {
                            // System.out.println("DEBUG: Nenhuma imagem encontrada em " + nomeDir);
                        }
                    }
                }
            }
        }
        return leituras;
    }
}
