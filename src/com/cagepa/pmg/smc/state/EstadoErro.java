package com.cagepa.pmg.smc.state;

import com.cagepa.pmg.infra.Logger;

public class EstadoErro implements EstadoLeitura {
    @Override
    public void processar(LeituraContext context) {
        Logger.getInstance().logInfo("SMC (State): Tentando recuperar de erro e reprocessar...");
        context.setEstado(new EstadoProcessando());
    }

    @Override
    public void concluir(LeituraContext context) {
        Logger.getInstance().logError("SMC (State): Não é possível concluir uma leitura em estado de erro.");
    }

    @Override
    public void reportarErro(LeituraContext context, String mensagem) {
        Logger.getInstance().logError("SMC (State): Já está em estado de erro: " + mensagem);
    }
}
