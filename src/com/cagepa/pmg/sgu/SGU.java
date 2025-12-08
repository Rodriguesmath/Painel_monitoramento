package com.cagepa.pmg.sgu;

import com.cagepa.pmg.infra.Logger;

public class SGU {
    public boolean autenticar(String usuario, String senha) {
        // Mock authentication logic
        Logger.getInstance().logInfo("SGU: Tentativa de autenticação para usuário: " + usuario);
        if ("admin".equals(usuario) && "admin".equals(senha)) {
            Logger.getInstance().logInfo("SGU: Usuário autenticado com sucesso.");
            return true;
        }
        Logger.getInstance().logError("SGU: Falha na autenticação.");
        return false;
    }
}
