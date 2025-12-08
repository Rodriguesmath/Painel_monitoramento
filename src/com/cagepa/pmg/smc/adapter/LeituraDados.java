package com.cagepa.pmg.smc.adapter;

public class LeituraDados {
    private String idSHA;
    private double valor;

    public LeituraDados(String idSHA, double valor) {
        this.idSHA = idSHA;
        this.valor = valor;
    }

    public String getIdSHA() {
        return idSHA;
    }

    public double getValor() {
        return valor;
    }
}
