package com.cagepa.pmg.smc.adapter;

import java.util.List;

public interface IProcessadorImagem {
    // Scans the internal directory for new readings
    List<LeituraDados> processarNovasImagens();

    void adicionarDiretorio(String path);

    void removerDiretorio(String path);

    // Performs the actual OCR processing (potentially slow)
    double realizarOCR(java.io.File imagem);
}
