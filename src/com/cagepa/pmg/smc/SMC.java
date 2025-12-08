package com.cagepa.pmg.smc;

import com.cagepa.pmg.infra.Logger;
import com.cagepa.pmg.san.SAN;
import com.cagepa.pmg.smc.adapter.IProcessadorImagem;
import com.cagepa.pmg.smc.state.LeituraContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cagepa.pmg.smc.adapter.LeituraDados;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SMC {
    private List<SAN> observers = new ArrayList<>();
    private List<IProcessadorImagem> adaptadores = new ArrayList<>();
    private ScheduledExecutorService scheduler;
    private boolean monitorando = false;

    public void addObserver(SAN san) {
        observers.add(san);
    }

    public void adicionarAdaptador(IProcessadorImagem adaptador) {
        this.adaptadores.add(adaptador);
    }

    public void iniciarMonitoramento() {
        if (monitorando) {
            Logger.getInstance().logInfo("SMC: Monitoramento já está em execução.");
            return;
        }

        Logger.getInstance().logInfo("SMC: Iniciando monitoramento contínuo...");
        monitorando = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Poll every 5 seconds
        scheduler.scheduleAtFixedRate(this::cicloMonitoramento, 0, 5, TimeUnit.SECONDS);
    }

    public void pararMonitoramento() {
        if (monitorando && scheduler != null) {
            Logger.getInstance().logInfo("SMC: Parando monitoramento...");
            scheduler.shutdown();
            monitorando = false;
        }
    }

    private void cicloMonitoramento() {
        for (IProcessadorImagem adaptador : adaptadores) {
            try {
                List<LeituraDados> novasLeituras = adaptador.processarNovasImagens();
                for (LeituraDados leitura : novasLeituras) {
                    processarLeituraIndividual(leitura);
                }
            } catch (Exception e) {
                Logger.getInstance().logError("SMC: Erro no ciclo de monitoramento: " + e.getMessage());
            }
        }
    }

    private void processarLeituraIndividual(LeituraDados dados) {
        LeituraContext context = new LeituraContext(dados.getIdSHA());
        try {
            context.processar(); // State: Processing
            context.setValorLeitura(dados.getValor());
            context.concluir(); // State: Completed

            // Observer: Notify SAN
            notificarObservers(dados.getIdSHA(), dados.getValor());
        } catch (Exception e) {
            context.reportarErro(e.getMessage());
        }
    }

    private void notificarObservers(String idSHA, double valor) {
        Logger.getInstance().logInfo("SMC (Subject): Notificando observadores sobre nova leitura.");
        for (SAN san : observers) {
            san.verificarAnomalia(idSHA, valor);
        }
    }
}
