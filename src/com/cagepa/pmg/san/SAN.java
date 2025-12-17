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

    public void configurarAlerta(String idUsuario, double limiteConsumo, String tipoNotificacao) {
        // Delegate to SGU for persistence
        if (sgu != null) {
            String tipo = (tipoNotificacao != null && !tipoNotificacao.isEmpty()) ? tipoNotificacao : "EMAIL";
            sgu.atualizarConfiguracaoAlerta(idUsuario, limiteConsumo, tipo);
        } else {
            Logger.getInstance().logError("SAN: SGU não inicializado. Não foi possível configurar alerta.");
        }

        regrasAlerta.put(idUsuario, limiteConsumo);
        Logger.getInstance()
                .logInfo("SAN: Alerta configurado para usuário " + idUsuario + " com limite " + limiteConsumo + " via "
                        + tipoNotificacao);
    }

    // Legacy method overload for backward compatibility if needed, though we should
    // update callers
    public void configurarAlerta(String idUsuario, double limiteConsumo) {
        configurarAlerta(idUsuario, limiteConsumo, "EMAIL");
    }

    public void setEstrategiaNotificacao(INotificador notificador) {
        this.notificador = notificador;
    }

    private com.cagepa.pmg.sgu.SGU sgu;

    public void setSgu(com.cagepa.pmg.sgu.SGU sgu) {
        this.sgu = sgu;
    }

    private java.util.List<String> bufferAlertas = new java.util.ArrayList<>();

    public java.util.List<String> getAlertasRecentes() {
        // Return a copy and clear? Or just return the last N?
        // Let's return a copy of the current buffer and clear it, acting as a queue
        // consumption.
        java.util.List<String> copy = new java.util.ArrayList<>(bufferAlertas);
        bufferAlertas.clear();
        return copy;
    }

    public void verificarAnomalia(String idSHA, double leituraAtual) {
        String idUsuario = null;

        // Resolve User from SHA if SGU is available
        if (sgu != null) {
            com.cagepa.pmg.sgu.Usuario usuario = sgu.getUsuarioPorHidrometro(idSHA);
            if (usuario != null) {
                idUsuario = usuario.getId();
                // Find specific hydrometer to get its limit
                for (com.cagepa.pmg.sgu.Hidrometro h : usuario.getHidrometros()) {
                    if (h.getId().equals(idSHA)) {
                        double limite = h.getLimiteAlerta();
                        if (limite > 0 && leituraAtual > limite) {
                            String tipo = h.getTipoAlerta();
                            if ("SMS".equalsIgnoreCase(tipo)) {
                                this.notificador = new NotificadorSMS(); // Use cached instances if perf issues arise
                            } else {
                                this.notificador = new NotificadorEmail();
                            }

                            String msg = "ALERTA: Consumo " + String.format("%.2f", leituraAtual) + " > " + limite
                                    + " (User: "
                                    + idUsuario + ", Hidro: " + idSHA + ", Via: " + tipo + ")";
                            Logger.getInstance()
                                    .logInfo("SAN: Anomalia detectada. Disparando notificação e bufferizando.");
                            bufferAlertas.add(msg);
                            if (notificador != null) {
                                notificador.enviar(msg);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
}
