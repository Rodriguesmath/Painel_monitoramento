package com.cagepa.pmg.smc.adapter;

public class LeituraDados {
    private String idSHA;
    private double valor;
    private java.io.File imagem; // New field

    public LeituraDados(String idSHA, java.io.File imagem) {
        this.idSHA = idSHA;
        this.imagem = imagem;
        this.valor = 0.0; // Pending
    }

    // Constructor for backward compatibility or direct value
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

    public void setValor(double valor) {
        this.valor = valor;
    }

    public java.io.File getImagem() {
        return imagem;
    }
}
