package com.cagepa.pmg.smc.adapter;

import com.cagepa.pmg.infra.Logger;
import java.io.File;

public class AdaptadorMostradorAnalogico implements IProcessadorImagem {
    @Override
    public double processarImagens(File diretorioImagens) throws Exception {
        Logger.getInstance().logInfo(
                "SMC (Adapter): Iniciando processamento ANALÓGICO no diretório: " + diretorioImagens.getName());

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
                .logInfo("SMC (Adapter): Encontradas " + arquivos.length + " imagens para processamento de ponteiros.");

        // Simulating pointer angle analysis
        double leituraSimulada = 5678.90;

        Logger.getInstance().logInfo("SMC (Adapter): Leitura ANALÓGICA concluída: " + leituraSimulada);
        return leituraSimulada;
    }
}
