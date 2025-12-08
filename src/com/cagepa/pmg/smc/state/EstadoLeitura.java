package com.cagepa.pmg.smc.state;

public interface EstadoLeitura {
    void processar(LeituraContext context);

    void concluir(LeituraContext context);

    void reportarErro(LeituraContext context, String mensagem);
}
