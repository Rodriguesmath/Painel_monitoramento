package com.cagepa.pmg.san;

import com.cagepa.pmg.infra.Logger;

public class NotificadorEmail implements INotificador {
    @Override
    public void enviar(String mensagem) {
        String logMsg = "EMAIL enviado: " + mensagem;
        Logger.getInstance().logInfo(logMsg);
        System.out.println(">> [MOCK EMAIL] " + mensagem);
    }
}
