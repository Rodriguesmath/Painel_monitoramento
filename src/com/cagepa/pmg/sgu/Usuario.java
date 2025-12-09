package com.cagepa.pmg.sgu;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String id;
    private String nome;
    private String senha;
    private TipoUsuario tipo;
    private String modeloAdapter; // New field
    private double consumoAtual; // New field
    private List<String> shasAssociados;

    public Usuario(String id, String nome, String senha, TipoUsuario tipo, String modeloAdapter) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
        this.tipo = tipo;
        this.modeloAdapter = modeloAdapter;
        this.consumoAtual = 0.0;
        this.shasAssociados = new ArrayList<>();
    }

    public Usuario(String id, String nome, String senha, TipoUsuario tipo, String modeloAdapter, double consumoAtual) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
        this.tipo = tipo;
        this.modeloAdapter = modeloAdapter;
        this.consumoAtual = consumoAtual;
        this.shasAssociados = new ArrayList<>();
    }

    public String getSenha() {
        return senha;
    }

    public void adicionarSha(String idSha) {
        this.shasAssociados.add(idSha);
    }

    public boolean possuiSha(String idSha) {
        return this.shasAssociados.contains(idSha);
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public boolean validarSenha(String senha) {
        return this.senha.equals(senha);
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public String getModeloAdapter() {
        return modeloAdapter;
    }

    public double getConsumoAtual() {
        return consumoAtual;
    }

    public void setConsumoAtual(double consumoAtual) {
        this.consumoAtual = consumoAtual;
    }

    public List<String> getShas() {
        List<String> shas = new ArrayList<>(shasAssociados);
        shas.add(id); // Automatically associate ID as a SHA
        return shas;
    }
}
