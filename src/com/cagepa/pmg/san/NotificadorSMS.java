package com.cagepa.pmg.san;

import com.cagepa.pmg.infra.Logger;

public class NotificadorSMS implements INotificador {
    @Override
    public void enviar(String mensagem) {
        String logMsg = "SMS enviado: " + mensagem;
        Logger.getInstance().logInfo(logMsg);
        System.out.println(">> [MOCK SMS] " + mensagem);
    }
}
