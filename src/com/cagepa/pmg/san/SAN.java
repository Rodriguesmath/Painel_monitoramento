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

    private com.cagepa.pmg.sgu.SGU sgu;

    public void setSgu(com.cagepa.pmg.sgu.SGU sgu) {
        this.sgu = sgu;
    }

    // Observer method called by SMC
    public void verificarAnomalia(String idSHA, double leituraAtual) {
        String idUsuario = null;

        // Resolve User from SHA if SGU is available
        if (sgu != null) {
            com.cagepa.pmg.sgu.Usuario usuario = sgu.getUsuarioPorSha(idSHA);
            if (usuario != null) {
                idUsuario = usuario.getId();
            }
        }

        // Fallback: if no user found, maybe the alert is on the SHA itself (legacy
        // behavior) or we skip
        if (idUsuario == null) {
            Logger.getInstance().logInfo("SAN: Não foi possível identificar o usuário para o SHA " + idSHA);
            return;
        }

        if (regrasAlerta.containsKey(idUsuario)) {
            double limite = regrasAlerta.get(idUsuario);
            if (leituraAtual > limite) {
                String msg = "ALERTA: Consumo " + leituraAtual + " excedeu o limite de " + limite + " para o usuário "
                        + idUsuario + " (SHA: " + idSHA + ")";
                Logger.getInstance().logInfo("SAN: Anomalia detectada. Disparando notificação.");
                notificador.enviar(msg);
            }
        }
    }
}
