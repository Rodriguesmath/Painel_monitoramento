package com.cagepa.pmg.smc;

import com.cagepa.pmg.infra.Logger;

// Removed direct SAN import to decouple
// import com.cagepa.pmg.san.SAN;
import com.cagepa.pmg.smc.adapter.IProcessadorImagem;
import com.cagepa.pmg.smc.state.LeituraContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cagepa.pmg.smc.adapter.LeituraDados;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.cagepa.pmg.sgu.Usuario;
import com.cagepa.pmg.sgu.Hidrometro;

public class SMC {
    private List<IObservadorLeitura> observers = new ArrayList<>();
    private List<IProcessadorImagem> adaptadores = new ArrayList<>();
    private java.nio.file.WatchService watcher;
    private java.util.Map<java.nio.file.WatchKey, java.nio.file.Path> keys;
    private boolean monitorando = false;

    private java.util.concurrent.ExecutorService executor;
    private java.util.concurrent.ScheduledExecutorService scheduler;

    public SMC() {
        try {
            this.watcher = java.nio.file.FileSystems.getDefault().newWatchService();
            this.keys = new java.util.HashMap<>();
        } catch (java.io.IOException e) {
            Logger.getInstance().logError("SMC: Falha ao iniciar WatchService: " + e.getMessage());
        }
    }

    public void addObserver(IObservadorLeitura observer) {
        observers.add(observer);
    }

    public void adicionarAdaptador(IProcessadorImagem adaptador) {
        this.adaptadores.add(adaptador);
    }

    public void iniciarMonitoramento() {
        if (monitorando) {
            Logger.getInstance().logInfo("SMC: Monitoramento já está em execução.");
            return;
        }

        if (this.watcher == null) {
            Logger.getInstance().logError("SMC: WatchService não inicializado. Abortando.");
            return;
        }

        Logger.getInstance().logInfo("SMC: Iniciando monitoramento SÍNCRONO (WatchService)...");
        monitorando = true;
        executor = Executors.newFixedThreadPool(2);
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Schedule polling for adapters (Fallback for WatchService)
        // Increased frequency to 1s to ensure responsiveness (User Feedback: "updates
        // once per cycle")
        scheduler.scheduleAtFixedRate(this::pollAdapters, 0, 1, TimeUnit.SECONDS);

        // Start the event loop in a separate thread
        new Thread(this::processEvents).start();
    }

    private void registerRecursive(java.nio.file.Path start) throws java.io.IOException {
        java.nio.file.Files.walkFileTree(start, new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
            @Override
            public java.nio.file.FileVisitResult preVisitDirectory(java.nio.file.Path dir,
                    java.nio.file.attribute.BasicFileAttributes attrs)
                    throws java.io.IOException {
                register(dir);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(java.nio.file.Path dir) throws java.io.IOException {
        java.nio.file.WatchKey key = dir.register(watcher, java.nio.file.StandardWatchEventKinds.ENTRY_CREATE);
        keys.put(key, dir);
        Logger.getInstance().logInfo("SMC: Observando diretório: " + dir);
    }

    public void adicionarDiretorioLeitura(String pathStr) {
        Logger.getInstance().logInfo("SMC: Adicionando e indexando diretório: " + pathStr);
        java.nio.file.Path path = java.nio.file.Paths.get(pathStr);
        try {
            registerRecursive(path);
            // Propagate to adapters so they can poll/check status in this directory
            for (IProcessadorImagem adapter : adaptadores) {
                adapter.adicionarDiretorio(pathStr);
            }
        } catch (java.io.IOException e) {
            Logger.getInstance().logError("SMC: Erro ao registrar diretório " + pathStr + ": " + e.getMessage());
        }
    }

    public void pararMonitoramento() {
        if (monitorando) {
            Logger.getInstance().logInfo("SMC: Parando monitoramento...");
            monitorando = false;
            try {
                if (watcher != null)
                    watcher.close();
            } catch (java.io.IOException e) {
                // ignore
            }
            if (executor != null)
                executor.shutdown();
            if (scheduler != null)
                scheduler.shutdown();
        }
    }

    private void processEvents() {
        while (monitorando) {
            java.nio.file.WatchKey key;
            try {
                key = watcher.take(); // Wait for event
            } catch (InterruptedException | java.nio.file.ClosedWatchServiceException e) {
                return;
            }

            java.nio.file.Path dir = keys.get(key);
            if (dir == null) {
                continue;
            }

            for (java.nio.file.WatchEvent<?> event : key.pollEvents()) {
                java.nio.file.WatchEvent.Kind<?> kind = event.kind();

                if (kind == java.nio.file.StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                java.nio.file.WatchEvent<java.nio.file.Path> ev = (java.nio.file.WatchEvent<java.nio.file.Path>) event;
                java.nio.file.Path name = ev.context();
                java.nio.file.Path child = dir.resolve(name);

                Logger.getInstance().logInfo("SMC: Evento detectado: " + kind.name() + " em " + child);

                if (java.nio.file.Files.isDirectory(child, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                    try {
                        registerRecursive(child);
                        // Propagate to adapters
                        for (IProcessadorImagem adapter : adaptadores) {
                            adapter.adicionarDiretorio(child.toAbsolutePath().toString());
                        }
                    } catch (java.io.IOException x) {
                        Logger.getInstance().logError("SMC: Falha ao registrar novo diretório: " + child);
                    }
                } else {
                    // It is a file, try to process with adapters
                    File arquivo = child.toFile();
                    for (IProcessadorImagem adaptador : adaptadores) {
                        LeituraDados leitura = adaptador.processarImagem(arquivo);
                        if (leitura != null) {
                            // Process IMMEDIATELY (Synchronous/No Debounce)
                            processarLeitura(leitura, adaptador);
                            break;
                        }
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void pollAdapters() {
        for (IProcessadorImagem adapter : adaptadores) {
            try {
                List<LeituraDados> leituras = adapter.processarNovasImagens();
                for (LeituraDados leitura : leituras) {
                    // Process IMMEDIATELY
                    processarLeitura(leitura, adapter);
                }
            } catch (Exception e) {
                Logger.getInstance().logError("SMC: Erro ao realizar polling no adaptador: " + e.getMessage());
            }
        }
    }

    private void processarLeitura(LeituraDados leitura, IProcessadorImagem adapter) {
        executor.submit(() -> {
            try {
                double valor = adapter.realizarOCR(leitura.getImagem());
                leitura.setValor(valor);
                processarLeituraIndividual(leitura);
            } catch (Exception e) {
                Logger.getInstance().logError("SMC: Erro no OCR para " + leitura.getIdSHA() + ": " + e.getMessage());
            }
        });
    }

    private com.cagepa.pmg.sgu.SGU sgu = new com.cagepa.pmg.sgu.SGU();

    private java.util.Map<String, Integer> consecutiveZeros = new java.util.HashMap<>();
    private static final int MAX_ZERO_TOLERANCE = 10;
    private static final double MAX_JUMP_THRESHOLD = 5.0;

    private void processarLeituraIndividual(LeituraDados dados) {
        // 1. Get previous value for Validation
        double valorAnterior = 0.0;
        Usuario u = sgu.getUsuarioPorHidrometro(dados.getIdSHA());
        if (u != null) {
            for (Hidrometro h : u.getHidrometros()) {
                if (h.getId().equals(dados.getIdSHA())) {
                    valorAnterior = h.getConsumoAtual();
                    break;
                }
            }
        }

        double valorNovo = dados.getValor();

        // 2. Inference Logic
        // A) Gap Filling (Zero Tolerance)
        if (valorNovo == 0.0 && valorAnterior > 0.0) {
            int zeros = consecutiveZeros.getOrDefault(dados.getIdSHA(), 0);
            zeros++;
            consecutiveZeros.put(dados.getIdSHA(), zeros);

            if (zeros <= MAX_ZERO_TOLERANCE) {
                Logger.getInstance().logInfo("SMC: Leitura 0.0 detectada para " + dados.getIdSHA()
                        + ". Usando valor anterior (Recuperação " + zeros + "/" + MAX_ZERO_TOLERANCE + ")");
                valorNovo = valorAnterior;
                dados.setValor(valorNovo); // Correct the data object
            } else {
                Logger.getInstance()
                        .logError("SMC: Leitura 0.0 persistente para " + dados.getIdSHA()
                                + ". Falha assumida. IGNORANDO ATUALIZAÇÃO.");
                // STOP PROCESSING to avoid false reset detection in SGU
                return;
            }
        } else {
            // Reset counter if valid reading
            if (valorNovo > 0.0) {
                consecutiveZeros.put(dados.getIdSHA(), 0);
            }

            // B) Coherence Filter (Anti-Surge / Anti-Drop)
            if (valorAnterior > 0.0) {
                double diff = valorNovo - valorAnterior;

                // Drop Check (Reverse flow or Bad OCR)
                // Allow small -0.1 due to float jitter if not rounded perfectly, but strictly <
                // -0.5 is error
                if (diff < -0.5) {
                    Logger.getInstance().logInfo("SMC: Leitura descartada (Queda brusca): " + valorAnterior + " -> "
                            + valorNovo + " (" + dados.getIdSHA() + ")");
                    return; // SKIP UPDATE
                }

                // Jump Check (Surge)
                if (diff > MAX_JUMP_THRESHOLD) {
                    Logger.getInstance().logInfo("SMC: Leitura descartada (Salto absurdo): " + valorAnterior + " -> "
                            + valorNovo + " (" + dados.getIdSHA() + ")");
                    return; // SKIP UPDATE
                }
            }
        }

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

    public String getStatusHidrometro(String idHidrometro) {
        // Let's find the hydrometer owner first to get the model
        Usuario u = sgu.getUsuarioPorHidrometro(idHidrometro);
        String modelo = null;
        if (u != null) {
            for (Hidrometro h : u.getHidrometros()) {
                if (h.getId().equals(idHidrometro)) {
                    modelo = h.getModelo();
                    break;
                }
            }
        }

        if (modelo == null) {
            // Fallback: try all adapters
            for (IProcessadorImagem adapter : adaptadores) {
                String status = adapter.verificarStatus(idHidrometro);
                if (!"PARADO".equals(status)) {
                    return status;
                }
            }
            return "PARADO";
        }

        // Check specific adapter based on model
        for (IProcessadorImagem adapter : adaptadores) {
            if ("A".equalsIgnoreCase(modelo)
                    && adapter instanceof com.cagepa.pmg.smc.adapter.AdaptadorAnalogicoModeloA) {
                return adapter.verificarStatus(idHidrometro);
            } else if ("B".equalsIgnoreCase(modelo)
                    && adapter instanceof com.cagepa.pmg.smc.adapter.AdaptadorAnalogicoModeloB) {
                return adapter.verificarStatus(idHidrometro);

            }
        }
        return "PARADO";
    }

    private void notificarObservers(String idSHA, double valor) {
        Logger.getInstance().logInfo("SMC (Subject): Notificando observadores sobre nova leitura.");
        for (IObservadorLeitura obs : observers) {
            obs.atualizar(idSHA, valor);
        }
    }
}
