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

    public void adicionarDiretorioLeitura(String path) {
        Logger.getInstance().logInfo("SMC: Adicionando novo diretório de leitura: " + path);
        for (IProcessadorImagem adaptador : adaptadores) {
            adaptador.adicionarDiretorio(path);
        }
    }

    private java.util.concurrent.ExecutorService executor;

    public void iniciarMonitoramento() {
        if (monitorando) {
            Logger.getInstance().logInfo("SMC: Monitoramento já está em execução.");
            return;
        }

        Logger.getInstance().logInfo("SMC: Iniciando monitoramento contínuo...");
        monitorando = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        executor = Executors.newFixedThreadPool(5); // Pool of 5 threads for parallel OCR

        // Poll every 5 seconds
        scheduler.scheduleAtFixedRate(this::cicloMonitoramento, 0, 5, TimeUnit.SECONDS);
    }

    public void pararMonitoramento() {
        if (monitorando && scheduler != null) {
            Logger.getInstance().logInfo("SMC: Parando monitoramento...");
            scheduler.shutdown();
            if (executor != null) {
                executor.shutdown();
            }
            monitorando = false;
        }
    }

    private void cicloMonitoramento() {
        for (IProcessadorImagem adaptador : adaptadores) {
            try {
                // Get pending readings (with files, no values yet)
                List<LeituraDados> novasLeituras = adaptador.processarNovasImagens();

                for (LeituraDados leitura : novasLeituras) {
                    // Submit OCR task to thread pool
                    executor.submit(() -> {
                        try {
                            Logger.getInstance().logInfo("SMC: Iniciando OCR paralelo para " + leitura.getIdSHA());
                            double valor = adaptador.realizarOCR(leitura.getImagem());
                            leitura.setValor(valor);
                            processarLeituraIndividual(leitura);
                        } catch (Exception e) {
                            Logger.getInstance()
                                    .logError("SMC: Erro no OCR para " + leitura.getIdSHA() + ": " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                Logger.getInstance().logError("SMC: Erro no ciclo de monitoramento: " + e.getMessage());
            }
        }
    }

    private com.cagepa.pmg.sgu.SGU sgu = new com.cagepa.pmg.sgu.SGU();

    private void processarLeituraIndividual(LeituraDados dados) {
        // Update user consumption in DB
        sgu.atualizarConsumo(dados.getIdSHA(), dados.getValor());

        LeituraContext context = new LeituraContext(dados);
        try {
            context.processar(); // State: Processing
            context.concluir(); // State: Completed

            // Observer: Notify SAN
            notificarObservers(dados.getIdSHA(), dados.getValor());
        } catch (Exception e) {
            context.reportarErro(e.getMessage());
        }
    }

    public String getStatusHidrometro(String idUsuario) {
        com.cagepa.pmg.sgu.Usuario u = sgu.getUsuarioPorId(idUsuario);
        if (u == null)
            return "USUÁRIO NÃO ENCONTRADO";

        if ("A".equalsIgnoreCase(u.getModeloAdapter())) {
            for (IProcessadorImagem adapter : adaptadores) {
                if (adapter instanceof com.cagepa.pmg.smc.adapter.AdaptadorAnalogicoModeloA) {
                    return ((com.cagepa.pmg.smc.adapter.AdaptadorAnalogicoModeloA) adapter).verificarStatus(idUsuario);
                }
            }
        }
        return "DESCONHECIDO (Modelo não suporta status)";
    }

    private void notificarObservers(String idSHA, double valor) {
        Logger.getInstance().logInfo("SMC (Subject): Notificando observadores sobre nova leitura.");
        for (SAN san : observers) {
            san.verificarAnomalia(idSHA, valor);
        }
    }
}
