package com.cagepa.pmg.san;

import com.cagepa.pmg.infra.Logger;

public class NotificadorEmail implements INotificador {
    @Override
    public void enviar(String mensagem) {
        Logger.getInstance().logInfo("EMAIL enviado: " + mensagem);
    }
}
