package com.cagepa.pmg.smc.state;

import com.cagepa.pmg.infra.Logger;

public class EstadoConcluido implements EstadoLeitura {
    @Override
    public void processar(LeituraContext context) {
        Logger.getInstance().logInfo("SMC (State): Leitura já concluída. Não pode reprocessar.");
    }

    @Override
    public void concluir(LeituraContext context) {
        Logger.getInstance().logInfo("SMC (State): Leitura já está no estado concluído.");
    }

    @Override
    public void reportarErro(LeituraContext context, String mensagem) {
        Logger.getInstance().logError("SMC (State): Erro reportado após conclusão (ignorando): " + mensagem);
    }
}
