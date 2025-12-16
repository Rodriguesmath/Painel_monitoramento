package com.cagepa.pmg.sgr;

import com.cagepa.pmg.infra.Logger;

public class RelatorioPDF extends GeradorRelatorio {
    @Override
    protected void formatarConteudo() {
        Logger.getInstance().logInfo("SGR: Formatando conteúdo como PDF (binário, layout fixo)...");
    }

    @Override
    protected void salvarArquivo() {
        Logger.getInstance().logInfo("SGR: Salvando arquivo .pdf no disco.");
        try {
            java.io.File file = new java.io.File("relatorio_mock.pdf");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write("%PDF-1.4 (MOCK CONTENT)\n");
            writer.write("Relatório gerado para fins de demonstração.\n");
            writer.close();
            System.out.println(">> [MOCK SGR] Arquivo gerado: " + file.getAbsolutePath());
        } catch (java.io.IOException e) {
            Logger.getInstance().logError("SGR: Erro ao gerar mock PDF: " + e.getMessage());
        }
    }
}
