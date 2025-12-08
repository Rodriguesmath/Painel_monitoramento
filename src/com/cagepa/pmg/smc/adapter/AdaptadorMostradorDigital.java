package com.cagepa.pmg.smc.adapter;

import com.cagepa.pmg.infra.Logger;
import java.io.File;

public class AdaptadorMostradorDigital implements IProcessadorImagem {
    @Override
    public double processarImagens(File diretorioImagens) throws Exception {
        Logger.getInstance()
                .logInfo("SMC (Adapter): Iniciando processamento DIGITAL no diretório: " + diretorioImagens.getName());

        if (!diretorioImagens.isDirectory()) {
            throw new Exception("O caminho fornecido não é um diretório.");
        }

        File[] arquivos = diretorioImagens
                .listFiles((dir, name) -> name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".jpg"));

        if (arquivos == null || arquivos.length == 0) {
            Logger.getInstance().logInfo("SMC (Adapter): Nenhuma imagem encontrada no diretório.");
            return 0.0;
        }

        Logger.getInstance()
                .logInfo("SMC (Adapter): Encontradas " + arquivos.length + " imagens para processamento OCR digital.");

        // Simulating OCR processing on the sequence of images
        // In a real scenario, this would combine the images to read the digits
        double leituraSimulada = 1234.56;

        Logger.getInstance().logInfo("SMC (Adapter): Leitura DIGITAL concluída: " + leituraSimulada);
        return leituraSimulada;
    }
}
