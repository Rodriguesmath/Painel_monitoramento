package com.cagepa.pmg.san;

import com.cagepa.pmg.infra.Logger;
import java.util.HashMap;
import java.util.Map;

public class SAN {
    private Map<String, Double> regrasAlerta = new HashMap<>(); // ID -> Threshold
    private INotificador notificador;

    public SAN() {
        // Default strategy
        this.notificador = new NotificadorEmail();
    }

    public void configurarAlerta(String idUsuario, double limiteConsumo) {
        regrasAlerta.put(idUsuario, limiteConsumo);
        Logger.getInstance()
                .logInfo("SAN: Alerta configurado para usuário " + idUsuario + " com limite " + limiteConsumo);
    }

    public void setEstrategiaNotificacao(INotificador notificador) {
        this.notificador = notificador;
    }

    // Observer method called by SMC
    public void verificarAnomalia(String idUsuario, double leituraAtual) {
        if (regrasAlerta.containsKey(idUsuario)) {
            double limite = regrasAlerta.get(idUsuario);
            if (leituraAtual > limite) {
                String msg = "ALERTA: Consumo " + leituraAtual + " excedeu o limite de " + limite + " para o usuário "
                        + idUsuario;
                Logger.getInstance().logInfo("SAN: Anomalia detectada. Disparando notificação.");
                notificador.enviar(msg);
            }
        }
    }
}
