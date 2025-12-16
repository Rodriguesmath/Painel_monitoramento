package com.cagepa.pmg.sgu;

public class Hidrometro {
    private String id;
    private String idUsuario;
    private String modelo;
    private double consumoAtual;

    public Hidrometro(String id, String idUsuario, String modelo, double consumoAtual) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.modelo = modelo;
        this.consumoAtual = consumoAtual;
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

    public void setConsumoAtual(double consumoAtual) {
        this.consumoAtual = consumoAtual;
    }

    @Override
    public String toString() {
        return "Hidrometro{" +
                "id='" + id + '\'' +
                ", modelo='" + modelo + '\'' +
                ", consumo=" + consumoAtual +
                '}';
    }
}
