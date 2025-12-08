package com.cagepa.pmg.sgr;

import com.cagepa.pmg.infra.Logger;

public abstract class GeradorRelatorio {

    // Template Method
    public final void gerarRelatorio(String idUsuario) {
        Logger.getInstance().logInfo("SGR: Iniciando geração de relatório para usuário " + idUsuario);
        buscarDados(idUsuario);
        formatarConteudo();
        salvarArquivo();
        Logger.getInstance().logInfo("SGR: Relatório gerado com sucesso.");
    }

    protected void buscarDados(String idUsuario) {
        Logger.getInstance().logInfo("SGR: Buscando dados no banco de dados para " + idUsuario + "...");
        // Mock data retrieval
    }

    protected abstract void formatarConteudo();

    protected abstract void salvarArquivo();
}
