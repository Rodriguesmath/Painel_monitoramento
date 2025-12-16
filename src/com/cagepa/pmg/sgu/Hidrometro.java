package com.cagepa.pmg.sgu;

public class Hidrometro {
    private String id;
    private String idUsuario;
    private String modelo;
    private double consumoAtual;
    private double offset;

    public Hidrometro(String id, String idUsuario, String modelo, double consumoAtual, double offset) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.modelo = modelo;
        this.consumoAtual = consumoAtual;
        this.offset = offset;
    }

    public String getId() {
        return id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getModelo() {
        return modelo;
    }

    public double getConsumoAtual() {
        return consumoAtual;
    }

    public double getOffset() {
        return offset;
    }

    public double getConsumoTotal() {
        return consumoAtual; // Offset removed as per request
    }

    public void setConsumoAtual(double consumoAtual) {
        this.consumoAtual = consumoAtual;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "Hidrometro{id='" + id + "', modelo='" + modelo + "', consumoTotal=" + getConsumoTotal() + "}";
    }
}
