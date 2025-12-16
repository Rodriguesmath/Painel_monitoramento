package com.cagepa.pmg.sgu;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String id;
    private String nome;
    private String senha;
    private TipoUsuario tipo;
    private List<Hidrometro> hidrometros;

    public Usuario(String id, String nome, String senha, TipoUsuario tipo) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
        this.tipo = tipo;
        this.hidrometros = new ArrayList<>();
    }

    public String getSenha() {
        return senha;
    }

    public void adicionarHidrometro(Hidrometro h) {
        this.hidrometros.add(h);
    }

    public List<Hidrometro> getHidrometros() {
        return hidrometros;
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
}
