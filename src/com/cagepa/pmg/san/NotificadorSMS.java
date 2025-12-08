package com.cagepa.pmg.san;

import com.cagepa.pmg.infra.Logger;

public class NotificadorSMS implements INotificador {
    @Override
    public void enviar(String mensagem) {
        Logger.getInstance().logInfo("SMS enviado: " + mensagem);
    }
}
