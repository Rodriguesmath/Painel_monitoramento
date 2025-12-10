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
import com.cagepa.pmg.sgu.TipoUsuario;
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

    public TipoUsuario getTipoUsuario(String id) {
        return sgu.getTipoUsuario(id);
    }

    public void cadastrarUsuario(String id, String nome, String senha, TipoUsuario tipo, String modeloAdapter) {
        sgu.cadastrarUsuario(id, nome, senha, tipo, modeloAdapter);
    }

    public java.util.List<com.cagepa.pmg.sgu.Usuario> listarUsuarios() {
        return sgu.listarUsuarios();
    }

    public String listarUsuariosRaw() {
        return sgu.listarUsuariosRaw();
    }

    public void atualizarSenha(String id, String novaSenha) {
        sgu.atualizarSenha(id, novaSenha);
    }

    public void deletarUsuario(String id) {
        sgu.deletarUsuario(id);
    }

    public void iniciarMonitoramento() {
        // Inject default configuration here (Facade Pattern acting as Configurator)
        smc.adicionarDiretorioLeitura("/home/rodrigues/Documentos/Painel_monitoramento/Simulador-Hidrometro");
        smc.iniciarMonitoramento();
    }

    public void pararMonitoramento() {
        smc.pararMonitoramento();
    }

    public void adicionarDiretorioLeitura(String path) {
        smc.adicionarDiretorioLeitura(path);
    }

    public String getStatusHidrometro(String idUsuario) {
        return smc.getStatusHidrometro(idUsuario);
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
