package com.cagepa.pmg.smc.adapter;

import com.cagepa.pmg.infra.Logger;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class TestAccuracyModelB {
    public static void main(String[] args) {
        System.out.println("=== Teste de Acurácia Completo - Modelo B ===");

        AdaptadorAnalogicoModeloB adapter = new AdaptadorAnalogicoModeloB();
        File dir = new File("Simulador-Hidrometro-B/Medições_userB");

        if (!dir.exists()) {
            System.err.println("Diretório não encontrado: " + dir.getAbsolutePath());
            return;
        }

        File[] images = dir
                .listFiles((d, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
        if (images == null || images.length == 0) {
            System.err.println("Nenhuma imagem encontrada.");
            return;
        }

        // Sort by filename number (e.g. 1.jpeg, 2.jpeg...)
        Arrays.sort(images, new Comparator<File>() {
            public int compare(File f1, File f2) {
                try {
                    int n1 = extractNumber(f1.getName());
                    int n2 = extractNumber(f2.getName());
                    return Integer.compare(n1, n2);
                } catch (Exception e) {
                    return f1.getName().compareTo(f2.getName());
                }
            }

            int extractNumber(String name) {
                String num = name.replaceAll("\\D", "");
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });

        int total = 0;
        int successes = 0;
        int failures = 0;
        double totalError = 0.0;
        int nonZeroCount = 0;
        int maxConsecutiveZeros = 0;
        int currentConsecutiveZeros = 0;
        String currentZeroStart = "";
        String maxZeroRange = "";
        String lastImgName = "";

        System.out.printf("%-20s | %-15s | %-15s | %-10s%n", "Arquivo", "Esperado", "Lido", "Status");
        System.out.println("-----------------------------------------------------------------------");

        for (File img : images) {
            total++;
            // Suppress logs for the test loop to keep output clean, strictly relying on
            // return value
            // We can't easily suppress the adapter's internal logging without modifying
            // Logger or Adapter,
            // so we'll just deal with the noise or grep the output.

            double valor = adapter.realizarOCR(img);

            // Expected value calculation: 02.jpeg -> 2 -> 0.2
            double expected = 0.0;
            try {
                String name = img.getName();
                String numStr = name.replaceAll("\\D", "");
                if (!numStr.isEmpty()) {
                    expected = Double.parseDouble(numStr) / 10.0;
                }
            } catch (Exception e) {
            }

            if (valor > 0.0) {
                totalError += Math.abs(valor - expected);
                nonZeroCount++;
            }

            // Tolerance of 0.05 as requested by user
            boolean success = Math.abs(valor - expected) <= 0.05;

            if (success) {
                successes++;
                // Show difference if acceptable but not exact
                if (Math.abs(valor - expected) > 0.001) {
                    System.out.printf("%-20s | %-15.2f | %-15.2f | %-10s (Diff: %.2f)%n", img.getName(), expected,
                            valor, "SUCESSO", (valor - expected));
                } else {
                    System.out.printf("%-20s | %-15.2f | %-15.2f | %-10s%n", img.getName(), expected, valor, "SUCESSO");
                }
            } else {
                failures++;
                System.out.printf("%-20s | %-15.2f | %-15.2f | %-10s (Diff: %.2f)%n", img.getName(), expected, valor,
                        "FALHA", (valor - expected));
            }
            if (valor == 0.0) {
                currentConsecutiveZeros++;
                if (currentConsecutiveZeros == 1) {
                    currentZeroStart = img.getName();
                }
            } else {
                if (currentConsecutiveZeros > maxConsecutiveZeros) {
                    maxConsecutiveZeros = currentConsecutiveZeros;
                    maxZeroRange = currentZeroStart + " -> " + lastImgName;
                }
                currentConsecutiveZeros = 0;
            }
            lastImgName = img.getName();
        }
        // Check last streak
        if (currentConsecutiveZeros > maxConsecutiveZeros) {
            maxConsecutiveZeros = currentConsecutiveZeros;
            maxZeroRange = currentZeroStart + " -> " + lastImgName;
        }

        System.out.println("-----------------------------------------------------------------------");
        System.out.println("Total de Imagens: " + total);
        System.out.println("Leituras com Sucesso (Dentro da Tolerância): " + successes);
        System.out.println("Falhas (Fora da Tolerância ou 0.0): " + failures);
        System.out.println("Maior sequência de Zeros (Falha total): " + maxConsecutiveZeros + " imagens");
        System.out.println("Faixa da maior falha: " + maxZeroRange);

        double avgError = (nonZeroCount > 0) ? totalError / nonZeroCount : 0.0;
        System.out.printf("Erro Médio (Absoluto) nas leituras: %.4f%n", avgError);
        System.out.printf("Taxa de Sucesso: %.2f%%%n", (total > 0) ? ((double) successes / total) * 100 : 0);
    }
}
