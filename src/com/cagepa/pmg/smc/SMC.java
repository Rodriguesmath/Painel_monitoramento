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
    private java.nio.file.WatchService watcher;
    private java.util.Map<java.nio.file.WatchKey, java.nio.file.Path> keys;
    private boolean trace = false;
    private boolean monitorando = false;
    private java.util.concurrent.ExecutorService executor;

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

        try {
            this.watcher = java.nio.file.FileSystems.getDefault().newWatchService();
            this.keys = new java.util.HashMap<>();
        } catch (java.io.IOException e) {
            Logger.getInstance().logError("SMC: Falha ao iniciar WatchService: " + e.getMessage());
            return;
        }

        Logger.getInstance().logInfo("SMC: Iniciando monitoramento SÍNCRONO (WatchService)...");
        monitorando = true;
        executor = Executors.newFixedThreadPool(5);

        // AUTO-REGISTER DEFAULT DIRECTORY (Hardcoded for Modelo A / Simulator)
        adicionarDiretorioLeitura("/home/rodrigues/Documentos/Painel_monitoramento/Simulador-Hidrometro");

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
                    } catch (java.io.IOException x) {
                        Logger.getInstance().logError("SMC: Falha ao registrar novo diretório: " + child);
                    }
                } else {
                    // It is a file, try to process with adapters
                    File arquivo = child.toFile();
                    for (IProcessadorImagem adaptador : adaptadores) {
                        LeituraDados leitura = adaptador.processarImagem(arquivo);
                        if (leitura != null) {
                            // Submit OCR task
                            executor.submit(() -> {
                                try {
                                    Logger.getInstance()
                                            .logInfo("SMC: Iniciando OCR Síncrono para " + leitura.getIdSHA());
                                    double valor = adaptador.realizarOCR(leitura.getImagem());
                                    leitura.setValor(valor);
                                    processarLeituraIndividual(leitura);
                                } catch (Exception e) {
                                    Logger.getInstance().logError(
                                            "SMC: Erro no OCR para " + leitura.getIdSHA() + ": " + e.getMessage());
                                }
                            });
                            break; // Found the right adapter
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
