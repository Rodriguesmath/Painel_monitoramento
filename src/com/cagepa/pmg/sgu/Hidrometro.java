package com.cagepa.pmg.sgu;

public class Hidrometro {
    private String id;
    private String idUsuario;
    private String modelo;
    private double consumoAtual;
    private double offset;
    private double limiteAlerta;
    private String tipoAlerta;

    public Hidrometro(String id, String idUsuario, String modelo, double consumoAtual, double offset,
            double limiteAlerta, String tipoAlerta) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.modelo = modelo;
        this.consumoAtual = consumoAtual;
        this.offset = offset;
        this.limiteAlerta = limiteAlerta;
        this.tipoAlerta = tipoAlerta;
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

    public double getLimiteAlerta() {
        return limiteAlerta;
    }

    public String getTipoAlerta() {
        return tipoAlerta;
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

    public void setLimiteAlerta(double limiteAlerta) {
        this.limiteAlerta = limiteAlerta;
    }

    public void setTipoAlerta(String tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    @Override
    public String toString() {
        return "Hidrometro{id='" + id + "', modelo='" + modelo + "', consumoTotal=" + getConsumoTotal() + ", limite="
                + limiteAlerta + ", tipo=" + tipoAlerta + "}";
    }
}
