package com.cagepa.pmg;

import com.cagepa.pmg.infra.Logger;
import com.cagepa.pmg.san.INotificador;
import com.cagepa.pmg.san.NotificadorEmail;
import com.cagepa.pmg.san.NotificadorSMS;
import com.cagepa.pmg.san.SAN;
import com.cagepa.pmg.sgr.GeradorRelatorio;
import com.cagepa.pmg.sgr.RelatorioCSV;
import com.cagepa.pmg.sgr.RelatorioPDF;
import com.cagepa.pmg.sgu.SGU;
import com.cagepa.pmg.smc.SMC;
import com.cagepa.pmg.smc.adapter.AdaptadorAnalogicoModeloA;
import com.cagepa.pmg.smc.adapter.AdaptadorAnalogicoModeloB;
import com.cagepa.pmg.smc.adapter.AdaptadorAnalogicoModeloC;

public class FachadaSistema {
    private SGU sgu;
    private SMC smc;
    private SAN san;

    public FachadaSistema() {
        this.sgu = new SGU();
        this.san = new SAN();
        this.san.setSgu(this.sgu);
        this.smc = new SMC();

        // Connect SMC to SAN
        this.smc.addObserver(this.san);

        // Register Adapters
        this.smc.adicionarAdaptador(new AdaptadorAnalogicoModeloA());
        this.smc.adicionarAdaptador(new AdaptadorAnalogicoModeloB());
        this.smc.adicionarAdaptador(new AdaptadorAnalogicoModeloC());

        Logger.getInstance().logInfo("Fachada: Sistema inicializado e subsistemas conectados.");
    }

    public boolean autenticar(String usuario, String senha) {
        return sgu.autenticar(usuario, senha);
    }

    public void cadastrarUsuario(String id, String nome, String senha) {
        sgu.cadastrarUsuario(id, nome, senha);
    }

    public void iniciarMonitoramento() {
        smc.iniciarMonitoramento();
    }

    public void pararMonitoramento() {
        smc.pararMonitoramento();
    }

    public void configurarAlerta(String idUsuario, double limiteConsumo, String tipoNotificacao) {
        san.configurarAlerta(idUsuario, limiteConsumo);

        INotificador estrategia;
        if ("SMS".equalsIgnoreCase(tipoNotificacao)) {
            estrategia = new NotificadorSMS();
        } else {
            estrategia = new NotificadorEmail();
        }
        san.setEstrategiaNotificacao(estrategia);
    }

    public void gerarRelatorio(String tipo, String idUsuario) {
        GeradorRelatorio gerador;
        if ("PDF".equalsIgnoreCase(tipo)) {
            gerador = new RelatorioPDF();
        } else if ("CSV".equalsIgnoreCase(tipo)) {
            gerador = new RelatorioCSV();
        } else {
            Logger.getInstance().logError("Fachada: Tipo de relatório desconhecido: " + tipo);
            return;
        }
        gerador.gerarRelatorio(idUsuario);
    }
}
