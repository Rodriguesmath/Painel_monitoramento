package com.cagepa.pmg.smc.state;

import com.cagepa.pmg.infra.Logger;

public class LeituraContext {
    private EstadoLeitura estadoAtual;
    private double valorLeitura;
    private String idSHA;

    public LeituraContext(String idSHA) {
        this.idSHA = idSHA;
        this.estadoAtual = new EstadoProcessando(); // Initial state
        Logger.getInstance().logInfo("SMC (State): Nova leitura iniciada para SHA " + idSHA);
    }

    public LeituraContext(com.cagepa.pmg.smc.adapter.LeituraDados dados) {
        this.idSHA = dados.getIdSHA();
        this.valorLeitura = dados.getValor();
        this.estadoAtual = new EstadoProcessando();
        Logger.getInstance().logInfo("SMC (State): Nova leitura iniciada para SHA " + idSHA);
    }

    public void setEstado(EstadoLeitura novoEstado) {
        this.estadoAtual = novoEstado;
        Logger.getInstance().logInfo("SMC (State): Transição de estado para " + novoEstado.getClass().getSimpleName());
    }

    public void processar() {
        estadoAtual.processar(this);
    }

    public void concluir() {
        estadoAtual.concluir(this);
    }

    public void reportarErro(String mensagem) {
        estadoAtual.reportarErro(this, mensagem);
    }

    public void setValorLeitura(double valor) {
        this.valorLeitura = valor;
    }

    public double getValorLeitura() {
        return valorLeitura;
    }

    public String getIdSHA() {
        return idSHA;
    }
}
