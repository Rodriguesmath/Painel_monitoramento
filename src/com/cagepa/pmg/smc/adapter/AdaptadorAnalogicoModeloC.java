package com.cagepa.pmg.smc.adapter;

import com.cagepa.pmg.infra.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdaptadorAnalogicoModeloC implements IProcessadorImagem {
    private final File diretorioMonitorado;

    public AdaptadorAnalogicoModeloC() {
        this.diretorioMonitorado = new File("input/modeloC");
        if (!this.diretorioMonitorado.exists()) {
            this.diretorioMonitorado.mkdirs();
        }
    }

    @Override
    public List<LeituraDados> processarNovasImagens() {
        List<LeituraDados> leituras = new ArrayList<>();
        File[] arquivos = diretorioMonitorado.listFiles();
        if (arquivos == null)
            return leituras;

        for (File arquivo : arquivos) {
            if (arquivo.isDirectory() && arquivo.getName().startsWith("Img_")) {
                // Check for valid images (0-100, jpg/jpeg/png)
                boolean temImagensValidas = false;
                for (int i = 0; i <= 100; i++) {
                    File imgJpg = new File(arquivo, i + ".jpg");
                    File imgJpeg = new File(arquivo, i + ".jpeg");
                    File imgPng = new File(arquivo, i + ".png");

                    if (imgJpg.exists() || imgJpeg.exists() || imgPng.exists()) {
                        temImagensValidas = true;
                        break;
                    }
                }

                if (temImagensValidas) {
                    String[] parts = arquivo.getName().split("_");
                    if (parts.length >= 2) {
                        String idSHA = parts[1];
                        Logger.getInstance().logInfo("Adapter C: Processando imagens (0-100) em " + arquivo.getName());
                        double valor = 300.0 + Math.random() * 50;
                        leituras.add(new LeituraDados(idSHA, valor));
                    }
                }
            }
        }
        return leituras;
    }
}
