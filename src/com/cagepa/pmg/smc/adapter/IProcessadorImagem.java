package com.cagepa.pmg.smc.adapter;

import java.io.File;

public interface IProcessadorImagem {
    // Receives a directory path containing the sequence of images
    double processarImagens(File diretorioImagens) throws Exception;
}
