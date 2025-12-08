package com.cagepa.pmg.smc.adapter;

import java.util.List;

public interface IProcessadorImagem {
    // Scans the internal directory for new readings
    List<LeituraDados> processarNovasImagens();
}
