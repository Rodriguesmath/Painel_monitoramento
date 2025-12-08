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
    }
}
