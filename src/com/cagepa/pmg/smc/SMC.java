package com.cagepa.pmg.smc;

import com.cagepa.pmg.infra.Logger;
import com.cagepa.pmg.san.SAN;
import com.cagepa.pmg.smc.adapter.IProcessadorImagem;
import com.cagepa.pmg.smc.state.LeituraContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SMC {
    private List<SAN> observers = new ArrayList<>(); // List of observers (SAN)

    public void addObserver(SAN san) {
        observers.add(san);
    }

    public void processarLeitura(String idSHA, File diretorioImagens, IProcessadorImagem adaptador) {
        LeituraContext context = new LeituraContext(idSHA);

        try {
            context.processar(); // State: Processing

            // Adapter: Process images
            double valor = adaptador.processarImagens(diretorioImagens);
            context.setValorLeitura(valor);

            context.concluir(); // State: Completed

            // Observer: Notify SAN
            notificarObservers(idSHA, valor);

        } catch (Exception e) {
            context.reportarErro(e.getMessage()); // State: Error
        }
    }

    private void notificarObservers(String idSHA, double valor) {
        Logger.getInstance().logInfo("SMC (Subject): Notificando observadores sobre nova leitura.");
        for (SAN san : observers) {
            // Assuming idSHA maps to a user ID for simplicity in this demo, or we pass both
            // For this exercise, we'll treat idSHA as the user ID key for alerts
            san.verificarAnomalia(idSHA, valor);
        }
    }
}
