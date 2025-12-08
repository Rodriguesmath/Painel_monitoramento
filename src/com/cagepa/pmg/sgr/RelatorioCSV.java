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
    }
}
