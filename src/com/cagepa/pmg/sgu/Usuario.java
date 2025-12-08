package com.cagepa.pmg.sgu;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String id;
    private String nome;
    private String senha;
    private List<String> shas;

    public Usuario(String id, String nome, String senha) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
        this.shas = new ArrayList<>();
    }

    public void adicionarSha(String idSha) {
        this.shas.add(idSha);
    }

    public boolean possuiSha(String idSha) {
        return this.shas.contains(idSha);
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

    public List<String> getShas() {
        return new ArrayList<>(shas);
    }
}
