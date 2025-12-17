package com.cagepa.pmg.smc.adapter;

import com.cagepa.pmg.infra.Logger;
import com.cagepa.pmg.infra.MotorOCR;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdaptadorAnalogicoModeloA implements IProcessadorImagem {

    private final List<File> diretoriosMonitorados;
    private final MotorOCR motor = new MotorOCR(); // Composition (Adaptee)

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
            // Call Adaptee
            String rawOutput = motor.processarImagem(imagem, "--psm", "6");
            Logger.getInstance().logInfo("Adapter A: Tesseract Raw Output: '" + rawOutput + "'");

            // CLEANING STRATEGY:
            // 1. Remove common noise separators that Tesseract inserts between digits
            String cleaned = rawOutput.replaceAll("[/|\\\\.]", "");
            // 2. Remove all non-digit characters
            cleaned = cleaned.replaceAll("[^0-9]", "");

            Logger.getInstance().logInfo("Adapter A: Limpeza: '" + rawOutput + "' -> '" + cleaned + "'");

            // 3. Take the first 4 digits (Standard format: 0XXX)
            if (cleaned.length() >= 4) {
                String numStr = cleaned.substring(0, 4);
                double valor = Double.parseDouble(numStr);
                Logger.getInstance().logInfo(
                        "Adapter A: Tesseract OCR realizado em " + imagem.getName() + " -> Valor: " + valor);
                return valor;
            } else if (cleaned.length() > 0) {
                // Fallback for short numbers (unlikely but possible)
                double valor = Double.parseDouble(cleaned);
                Logger.getInstance().logInfo(
                        "Adapter A: Tesseract OCR (parcial) em " + imagem.getName() + " -> Valor: " + valor);
                return valor;
            } else {
                Logger.getInstance()
                        .logInfo("Adapter A: Tesseract retornou vazio (após limpeza) para " + imagem.getName());
            }
        } catch (Exception e) {
            Logger.getInstance().logInfo("Adapter A: Tesseract erro de execução: " + e.getMessage());
            // e.printStackTrace();
        }

        // If Tesseract fails, return 0.0 (No Fallback)
        Logger.getInstance().logError("Adapter A: Falha no OCR para " + imagem.getName());
        return 0.0;
    }

    private java.util.Map<String, Long> ultimaAtividadeMap = new java.util.HashMap<>();

    @Override
    public String verificarStatus(String idSHA) {
        // Use cached activity timestamp per ID
        Long lastTime = ultimaAtividadeMap.get(idSHA);
        if (lastTime != null) {
            long diff = System.currentTimeMillis() - lastTime;
            if (diff < 5000) { // Keep 5 seconds as requested
                return "EM EXECUÇÃO";
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
                        // BINDING CHECK: Verify if this SHA is registered as Model A
                        com.cagepa.pmg.sgu.Usuario u = sgu.getUsuarioPorHidrometro(idSHA);
                        if (u == null) {
                            continue;
                        }

                        boolean isModelA = false;
                        for (com.cagepa.pmg.sgu.Hidrometro h : u.getHidrometros()) {
                            if (h.getId().equals(idSHA) && "A".equalsIgnoreCase(h.getModelo())) {
                                isModelA = true;
                                break;
                            }
                        }

                        if (!isModelA) {
                            continue;
                        }

                        // LATEST FILE CHECK: Find the most recent image
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
                                Logger.getInstance().logInfo("Adapter A: Imagem recente encontrada: "
                                        + maisRecente.getName() + " para " + idSHA);
                                leituras.add(new LeituraDados(idSHA, maisRecente));

                                // UPDATE ACTIVITY TIMESTAMP PER ID
                                this.ultimaAtividadeMap.put(idSHA, System.currentTimeMillis());
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
        // Validate file pattern (must be number.jpeg)
        if (!imagem.getName().matches("\\d+\\.jpeg")) {
            return null;
        }

        // Extract ID/SHA from parent directory: Medicoes_MATRICULA_ID
        String parentDir = imagem.getParentFile().getName();
        String idSHA = null;

        // Try parsing "Medicoes_MATRICULA_ID"
        if (parentDir.startsWith("Medicoes_")) {
            String[] parts = parentDir.split("_");
            if (parts.length >= 3) {
                idSHA = parts[2]; // ID is the 3rd part
            }
        }

        if (idSHA == null) {
            return null;
        }

        return new LeituraDados(idSHA, imagem);
    }
}
