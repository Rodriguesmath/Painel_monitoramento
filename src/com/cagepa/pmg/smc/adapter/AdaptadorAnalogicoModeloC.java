package com.cagepa.pmg.smc.adapter;

import com.cagepa.pmg.infra.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdaptadorAnalogicoModeloC implements IProcessadorImagem {
    private final List<File> diretoriosMonitorados;

    public AdaptadorAnalogicoModeloC() {
        this.diretoriosMonitorados = new ArrayList<>();
        adicionarDiretorio("input/modeloC");
    }

    @Override
    public void adicionarDiretorio(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!diretoriosMonitorados.contains(dir)) {
            diretoriosMonitorados.add(dir);
            Logger.getInstance().logInfo("Adapter C: Diretório adicionado: " + path);
        }
    }

    @Override
    public void removerDiretorio(String path) {
        File dir = new File(path);
        if (diretoriosMonitorados.remove(dir)) {
            Logger.getInstance().logInfo("Adapter C: Diretório removido: " + path);
        }
    }

    @Override
    public double realizarOCR(File imagem) {
        try {
            Thread.sleep(500 + (long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Logger.getInstance().logInfo("Adapter C: OCR realizado em " + imagem.getName());
        return 300.0 + Math.random() * 50;
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
                if (arquivo.isDirectory() && arquivo.getName().startsWith("Img_")) {
                    String[] parts = arquivo.getName().split("_");
                    if (parts.length >= 2) {
                        String idSHA = parts[1];
                        com.cagepa.pmg.sgu.Usuario u = sgu.getUsuarioPorHidrometro(idSHA);
                        if (u == null) {
                            continue;
                        }

                        boolean isModelC = false;
                        for (com.cagepa.pmg.sgu.Hidrometro h : u.getHidrometros()) {
                            if (h.getId().equals(idSHA) && "C".equalsIgnoreCase(h.getModelo())) {
                                isModelC = true;
                                break;
                            }
                        }

                        if (!isModelC) {
                            continue;
                        }

                        File[] imagens = arquivo.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg") ||
                                name.toLowerCase().endsWith(".png"));

                        if (imagens != null && imagens.length > 0) {
                            java.util.Arrays.sort(imagens,
                                    (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                            File maisRecente = imagens[0];
                            Logger.getInstance().logInfo("Adapter C: Imagem mais recente encontrada: "
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

        if (parentDir.startsWith("Img_")) {
            idSHA = parentDir.substring(4); // Remove "Img_"
        }

        if (idSHA != null) {
            return new LeituraDados(idSHA, imagem);
        }
        return null;
    }

    @Override
    public String verificarStatus(String idSHA) {
        return "DESCONHECIDO (Modelo C não suportado)";
    }
}
