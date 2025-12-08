package com.cagepa.pmg.smc.state;

import com.cagepa.pmg.infra.Logger;

public class EstadoProcessando implements EstadoLeitura {
    @Override
    public void processar(LeituraContext context) {
        Logger.getInstance().logInfo("SMC (State): Já está processando.");
    }

    @Override
    public void concluir(LeituraContext context) {
        context.setEstado(new EstadoConcluido());
    }

    @Override
    public void reportarErro(LeituraContext context, String mensagem) {
        Logger.getInstance().logError("SMC (State): Erro durante processamento: " + mensagem);
        context.setEstado(new EstadoErro());
    }
}
