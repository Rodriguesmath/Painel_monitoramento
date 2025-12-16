package com.cagepa.pmg.sgr;

import com.cagepa.pmg.infra.Logger;

public class RelatorioCSV extends GeradorRelatorio {
    @Override
    protected void formatarConteudo() {
        Logger.getInstance().logInfo("SGR: Formatando conteúdo como CSV (texto separado por vírgulas)...");
    }

    @Override
    protected void salvarArquivo() {
        Logger.getInstance().logInfo("SGR: Salvando arquivo .csv no disco.");
        try {
            java.io.File file = new java.io.File("relatorio_mock.csv");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write("ID;DATA;LEITURA\n");
            writer.write("MOCK;2025-12-16;123.45\n");
            writer.close();
            System.out.println(">> [MOCK SGR] Arquivo gerado: " + file.getAbsolutePath());
        } catch (java.io.IOException e) {
            Logger.getInstance().logError("SGR: Erro ao gerar mock CSV: " + e.getMessage());
        }
    }
}
