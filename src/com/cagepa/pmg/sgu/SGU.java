package com.cagepa.pmg.sgu;

import com.cagepa.pmg.infra.Logger;

public class SGU {
    private java.util.Map<String, Usuario> usuarios;

    public SGU() {
        this.usuarios = new java.util.HashMap<>();
        // Mock data
        Usuario admin = new Usuario("admin", "Administrador", "admin");
        admin.adicionarSha("SHA-001"); // Admin owns SHA-001 for testing
        usuarios.put("admin", admin);

        Usuario user1 = new Usuario("user123", "Usuário Teste", "123456");
        user1.adicionarSha("SHA-002");
        usuarios.put("user123", user1);
    }

    public void cadastrarUsuario(String id, String nome, String senha) {
        if (!usuarios.containsKey(id)) {
            Usuario novoUsuario = new Usuario(id, nome, senha);
            usuarios.put(id, novoUsuario);
            Logger.getInstance().logInfo("SGU: Usuário cadastrado com sucesso: " + nome + " (" + id + ")");
        } else {
            Logger.getInstance().logError("SGU: Erro ao cadastrar. ID de usuário já existe: " + id);
        }
    }

    public boolean autenticar(String usuario, String senha) {
        Logger.getInstance().logInfo("SGU: Tentativa de autenticação para usuário: " + usuario);
        if (usuarios.containsKey(usuario)) {
            boolean valido = usuarios.get(usuario).validarSenha(senha);
            if (valido) {
                Logger.getInstance().logInfo("SGU: Usuário autenticado com sucesso.");
                return true;
            }
        }
        Logger.getInstance().logError("SGU: Falha na autenticação.");
        return false;
    }

    public Usuario getUsuarioPorId(String idUsuario) {
        return usuarios.get(idUsuario);
    }

    public Usuario getUsuarioPorSha(String idSha) {
        for (Usuario u : usuarios.values()) {
            if (u.possuiSha(idSha)) {
                return u;
            }
        }
        return null;
    }
}
