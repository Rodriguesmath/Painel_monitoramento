package com.cagepa.pmg;

import java.util.Scanner;

public class PainelCLI {
    public static void main(String[] args) {
        FachadaSistema fachada = new FachadaSistema();
        Scanner scanner = new Scanner(System.in);
        boolean executando = true;
        boolean logado = false;

        System.out.println("=== Painel de Monitoramento CAGEPA (PMG) v3.0 ===");

        while (executando) {
            if (!logado) {
                System.out.print("\nLogin (usuario): ");
                String usuario = scanner.nextLine();
                System.out.print("Senha: ");
                String senha = scanner.nextLine();

                if (fachada.autenticar(usuario, senha)) {
                    logado = true;
                    System.out.println("Login realizado com sucesso!");
                } else {
                    System.out.println("Credenciais inválidas. Tente novamente.");
                }
            } else {
                System.out.println("\n--- Menu Principal ---");
                System.out.println("1. Processar Leitura");
                System.out.println("2. Configurar Alerta");
                System.out.println("3. Gerar Relatório");
                System.out.println("0. Sair");
                System.out.print("Escolha uma opção: ");

                String opcao = scanner.nextLine();

                switch (opcao) {
                    case "1":
                        System.out.print("ID do SHA: ");
                        String idSHA = scanner.nextLine();
                        System.out.print("Caminho do diretório de imagens: ");
                        String caminho = scanner.nextLine();
                        System.out.print("Tipo de Medidor (DIGITAL/ANALOGICO): ");
                        String tipoMedidor = scanner.nextLine();
                        fachada.processarLeitura(idSHA, caminho, tipoMedidor);
                        break;
                    case "2":
                        System.out.print("ID do Usuário: ");
                        String idUserAlerta = scanner.nextLine();
                        System.out.print("Limite de Consumo: ");
                        double limite = Double.parseDouble(scanner.nextLine());
                        System.out.print("Tipo de Notificação (EMAIL/SMS): ");
                        String tipoNotif = scanner.nextLine();
                        fachada.configurarAlerta(idUserAlerta, limite, tipoNotif);
                        break;
                    case "3":
                        System.out.print("Tipo de Relatório (PDF/CSV): ");
                        String tipoRel = scanner.nextLine();
                        System.out.print("ID do Usuário: ");
                        String idUserRel = scanner.nextLine();
                        fachada.gerarRelatorio(tipoRel, idUserRel);
                        break;
                    case "0":
                        executando = false;
                        System.out.println("Encerrando sistema...");
                        break;
                    default:
                        System.out.println("Opção inválida.");
                }
            }
        }
        scanner.close();
    }
}
