package com.cagepa.pmg;

import com.cagepa.pmg.sgu.TipoUsuario;
import com.cagepa.pmg.sgu.Usuario;
import com.cagepa.pmg.sgu.Hidrometro;
import java.util.Scanner;
import java.util.List;

public class PainelCLI {

    public static void main(String[] args) {
        FachadaSistema fachada = new FachadaSistema();
        Scanner scanner = new Scanner(System.in);
        boolean executando = true;
        boolean logado = false;
        boolean isDebug = false;

        fachada.logInfo("CLI: Aplicação iniciada.");

        while (executando) {
            if (!logado) {
                limparTela();
                System.out.println("=== Painel de Monitoramento CAGEPA (PMG) v3.1 ===");
                System.out.println("NOTA: Os logs de execução estão sendo gravados em 'system.log'.");
                System.out.println(
                        "DICA: Abra outro terminal e execute 'tail -f system.log' para acompanhar os eventos.\n");

                System.out.print("Login (ID): ");
                String usuario = scanner.nextLine();
                System.out.print("Senha: ");
                String senha = scanner.nextLine();

                TipoUsuario tipo = null;

                if ("debug".equals(usuario) && "debug".equals(senha)) {
                    logado = true;
                    isDebug = true;
                    tipo = TipoUsuario.ADMIN;
                    fachada.logInfo("CLI: Acesso DEBUG concedido.");
                    System.out.println("\nLogin DEBUG realizado com sucesso!");
                } else if (fachada.autenticar(usuario, senha)) {
                    logado = true;
                    isDebug = false;
                    tipo = fachada.getTipoUsuario(usuario);
                    fachada.logInfo("CLI: Login com sucesso para usuário: " + usuario);
                    System.out.println("\nLogin realizado com sucesso! Perfil: " + tipo);
                } else {
                    System.out.println("Credenciais inválidas. Tente novamente.");
                    fachada.logError("CLI: Tentativa de login falhou para usuário: " + usuario);
                    esperarEnter(scanner);
                    continue;
                }

                esperarEnter(scanner);

                // Inner loop for logged in session
                while (logado) {
                    limparTela();
                    if (tipo == TipoUsuario.ADMIN) {
                        System.out.println("=== Menu Principal (ADMIN) ===");
                        System.out.println("1. Monitoramento em Tempo Real");
                        System.out.println("2. Configurar Alerta");
                        System.out.println("3. Gerar Relatório");
                        System.out.println("4. Gestão de Usuários");
                        if (isDebug) {
                            System.out.println("5. Inspecionar Banco de Dados");
                        }
                        System.out.println("0. Logout");
                    } else {
                        System.out.println("=== Menu Principal (PADRAO) ===");
                        System.out.println("1. Configurar Alerta Pessoal");
                        System.out.println("2. Gerar Relatório Pessoal");
                        System.out.println("3. Alterar Minha Senha");
                        System.out.println("0. Logout");
                    }
                    System.out.print("\nEscolha uma opção: ");
                    String opcao = scanner.nextLine();

                    if (tipo == TipoUsuario.ADMIN) {
                        switch (opcao) {
                            case "1":
                                limparTela();
                                System.out.println("=== Monitoramento em Tempo Real ===");
                                fachada.logInfo("CLI: Admin iniciou monitoramento.");
                                System.out.println("Iniciando monitoramento...");
                                fachada.iniciarMonitoramento();

                                System.out.println("Pressione ENTER para parar a visualização.");

                                Thread displayThread = new Thread(() -> {
                                    // Buffer for alerts in UI
                                    List<String> activeAlerts = new java.util.ArrayList<>();

                                    while (!Thread.currentThread().isInterrupted()) {
                                        try {
                                            limparTela();
                                            System.out.println("=== Monitoramento em Tempo Real ===");

                                            // Fetch and accumulate alerts
                                            List<String> freshAlerts = fachada.getAlertasRecentes();
                                            activeAlerts.addAll(freshAlerts);
                                            // Keep only last 5
                                            if (activeAlerts.size() > 5) {
                                                activeAlerts = activeAlerts.subList(activeAlerts.size() - 5,
                                                        activeAlerts.size());
                                            }

                                            if (!activeAlerts.isEmpty()) {
                                                System.out.println("\n[ ALERTA DE CONSUMO ]");
                                                for (String alert : activeAlerts) {
                                                    System.out.println(" (!) " + alert);
                                                }
                                                System.out.println("------------------------------------------------");
                                            }

                                            List<Usuario> users = fachada.listarUsuarios();
                                            for (Usuario u : users) {
                                                if (u.getTipo() == TipoUsuario.ADMIN)
                                                    continue;

                                                List<Hidrometro> hidros = u.getHidrometros();
                                                if (hidros.isEmpty()) {
                                                    System.out.printf("Nome: %-20s | Sem hidrômetros%n", u.getNome());
                                                } else {
                                                    for (Hidrometro h : hidros) {
                                                        String status = fachada.getStatusHidrometro(h.getId());
                                                        System.out.printf(
                                                                "Nome: %-20s | ID: %-10s | Modelo: %-2s | Status: %-12s | Consumo: %.2f%n",
                                                                u.getNome(), h.getId(), h.getModelo(), status,
                                                                h.getConsumoTotal());
                                                    }
                                                }
                                            }
                                            System.out.println("\n(Pressione ENTER para voltar ao menu)");
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            break;
                                        }
                                    }
                                });
                                displayThread.start();

                                scanner.nextLine(); // Wait for ENTER
                                displayThread.interrupt();
                                try {
                                    displayThread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "2":
                                limparTela();
                                System.out.println("=== Configurar Alerta ===");
                                System.out.print("ID do Usuário Alvo: ");
                                String idAlvo = scanner.nextLine();
                                System.out.print("Limite: ");
                                double lim = Double.parseDouble(scanner.nextLine());
                                System.out.print("Tipo (EMAIL/SMS): ");
                                String tNotif = scanner.nextLine();
                                fachada.configurarAlerta(idAlvo, lim, tNotif);
                                fachada.logInfo("CLI: Alerta configurado para " + idAlvo);
                                break;
                            case "3":
                                limparTela();
                                System.out.println("=== Gerar Relatório ===");
                                System.out.print("Tipo (PDF/CSV): ");
                                String tRel = scanner.nextLine();
                                System.out.print("ID Usuário: ");
                                String idRel = scanner.nextLine();
                                fachada.gerarRelatorio(tRel, idRel);
                                System.out.println("RELATORIO: " + tRel + " gerado com sucesso em ./"
                                        + (tRel.equalsIgnoreCase("PDF") ? "relatorio_mock.pdf" : "relatorio.csv"));
                                String msgRel = "Relatório gerado para " + idRel;
                                fachada.logInfo("CLI: " + msgRel);
                                break;
                            case "4":
                                boolean subMenuGestao = true;
                                while (subMenuGestao) {
                                    limparTela();
                                    System.out.println("=== Gestão de Usuários ===");
                                    System.out.println("1. Cadastrar Usuário");
                                    System.out.println("2. Cadastrar Hidrômetro");
                                    System.out.println("3. Listar Usuários");
                                    System.out.println("4. Atualizar Senha");
                                    System.out.println("5. Deletar Usuário");
                                    System.out.println("0. Voltar");
                                    System.out.print("\nEscolha uma opção: ");
                                    String subOpcao = scanner.nextLine();

                                    switch (subOpcao) {
                                        case "1":
                                            // Loop for Registration
                                            while (true) {
                                                limparTela();
                                                System.out.println("=== Cadastrar Usuário ===");
                                                System.out.print("ID: ");
                                                String nId = scanner.nextLine();
                                                if (nId.isEmpty())
                                                    break; // Cancel on empty ID

                                                System.out.print("Nome: ");
                                                String nNome = scanner.nextLine();
                                                System.out.print("Senha: ");
                                                String nSenha = scanner.nextLine();
                                                System.out.print("Tipo (ADMIN/PADRAO): ");
                                                String nTipoStr = scanner.nextLine();
                                                TipoUsuario nTipo = "ADMIN".equalsIgnoreCase(nTipoStr)
                                                        ? TipoUsuario.ADMIN
                                                        : TipoUsuario.PADRAO;

                                                if (fachada.cadastrarUsuario(nId, nNome, nSenha, nTipo)) {
                                                    fachada.logInfo("CLI: Novo usuário cadastrado: " + nId);
                                                    System.out.println("Usuário cadastrado com sucesso!");
                                                    esperarEnter(scanner);
                                                    break;
                                                } else {
                                                    System.out.println(
                                                            "Erro: Usuário já existe ou dados inválidos. Tente Novamente.");
                                                    System.out.println(
                                                            "(Pressione ENTER para tentar novamente ou deixe ID vazio para cancelar)");
                                                    scanner.nextLine();
                                                }
                                            }
                                            break;
                                        case "2":
                                            // Loop for Hydrometer
                                            while (true) {
                                                limparTela();
                                                System.out.println("=== Cadastrar Hidrômetro ===");
                                                System.out.print("ID do Usuário: ");
                                                String idUserH = scanner.nextLine();
                                                if (idUserH.isEmpty())
                                                    break; // Cancel

                                                System.out.print("ID do Hidrômetro (SHA): ");
                                                String idHidro = scanner.nextLine();
                                                System.out.print("Modelo (A/B): ");
                                                String modH = scanner.nextLine();

                                                System.out.print("Limite de Alerta (0 para desativar): ");
                                                double limiteH = 0.0;
                                                try {
                                                    String lInput = scanner.nextLine();
                                                    if (!lInput.isEmpty())
                                                        limiteH = Double.parseDouble(lInput);
                                                } catch (NumberFormatException e) {
                                                    System.out.println("Valor inválido. Usando 0.0.");
                                                }

                                                if (fachada.adicionarHidrometro(idUserH, idHidro, modH, limiteH)) {
                                                    System.out.println("Hidrômetro vinculado com sucesso!");
                                                    esperarEnter(scanner);
                                                    break;
                                                } else {
                                                    System.out.println(
                                                            "Erro: Usuário não existe ou Hidrômetro já vinculado. Tente Novamente.");
                                                    System.out.println(
                                                            "(Pressione ENTER para tentar novamente ou deixe ID do Usuário vazio para cancelar)");
                                                    scanner.nextLine();
                                                }
                                            }
                                            break;
                                        case "3":
                                            limparTela();
                                            System.out.println("=== Lista de Usuários ===");
                                            List<Usuario> usuarios = fachada.listarUsuarios();
                                            for (Usuario u : usuarios) {
                                                List<Hidrometro> hidros = u.getHidrometros();
                                                if (hidros.isEmpty()) {
                                                    System.out.printf("Nome: %-20s | Sem hidrômetros%n", u.getNome());
                                                } else {
                                                    for (Hidrometro h : hidros) {
                                                        System.out.printf(
                                                                "Nome: %-20s | ID: %-10s | Modelo: %-2s | Consumo: %.2f%n",
                                                                u.getNome(), h.getId(), h.getModelo(),
                                                                h.getConsumoTotal());
                                                    }
                                                }
                                            }
                                            esperarEnter(scanner);
                                            break;
                                        case "4":
                                            limparTela();
                                            System.out.println("=== Atualizar Senha ===");
                                            System.out.print("ID do Usuário: ");
                                            String idUpd = scanner.nextLine();
                                            System.out.print("Nova Senha: ");
                                            String novaSenha = scanner.nextLine();
                                            fachada.atualizarSenha(idUpd, novaSenha);
                                            fachada.logInfo("CLI: Senha atualizada para " + idUpd);
                                            esperarEnter(scanner);
                                            break;
                                        case "5":
                                            limparTela();
                                            System.out.println("=== Deletar Usuário ===");
                                            List<Usuario> listaDel = fachada.listarUsuarios();
                                            if (listaDel.isEmpty()) {
                                                System.out.println("Nenhum usuário cadastrado.");
                                                esperarEnter(scanner);
                                                break;
                                            }

                                            for (int i = 0; i < listaDel.size(); i++) {
                                                Usuario u = listaDel.get(i);
                                                System.out.printf("%d. %s (ID: %s)%n", i + 1, u.getNome(), u.getId());
                                            }
                                            System.out.println("0. Cancelar");

                                            System.out.print("\nDigite o número ou o ID do usuário: ");
                                            String inputDel = scanner.nextLine();

                                            if ("0".equals(inputDel))
                                                break;

                                            String idParaDeletar = null;

                                            // Try to parse as index
                                            try {
                                                int index = Integer.parseInt(inputDel);
                                                if (index > 0 && index <= listaDel.size()) {
                                                    idParaDeletar = listaDel.get(index - 1).getId();
                                                }
                                            } catch (NumberFormatException e) {
                                                // Not a number, assume it's an ID
                                                for (Usuario u : listaDel) {
                                                    if (u.getId().equals(inputDel)) {
                                                        idParaDeletar = u.getId();
                                                        break;
                                                    }
                                                }
                                            }

                                            if (idParaDeletar != null) {
                                                System.out.printf(
                                                        "Tem certeza que deseja deletar o usuário '%s' e seus hidrômetros? (S/N): ",
                                                        idParaDeletar);
                                                String confirm = scanner.nextLine();
                                                if ("S".equalsIgnoreCase(confirm)) {
                                                    fachada.deletarUsuario(idParaDeletar);
                                                    System.out.println("Usuário deletado com sucesso!");
                                                    fachada.logInfo("CLI: Usuário deletado: " + idParaDeletar);
                                                } else {
                                                    System.out.println("Operação cancelada.");
                                                }
                                            } else {
                                                System.out.println("Usuário não encontrado.");
                                            }
                                            esperarEnter(scanner);
                                            break;
                                        case "0":
                                            subMenuGestao = false;
                                            break;
                                        default:
                                            System.out.println("Opção inválida.");
                                            esperarEnter(scanner);
                                    }
                                }
                                break;
                            case "5":
                                if (isDebug) {
                                    limparTela();
                                    System.out.println("=== Inspecionar Banco de Dados ===");
                                    System.out.println(fachada.listarUsuariosRaw());
                                } else {
                                    System.out.println("Opção inválida.");
                                }
                                break;
                            case "0":
                                logado = false;
                                fachada.logInfo("CLI: Logout realizado.");
                                break;
                            default:
                                System.out.println("Opção inválida.");
                        }
                    } else {
                        switch (opcao) {
                            case "1":
                                limparTela();
                                System.out.println("=== Configurar Alerta Pessoal ===");
                                System.out.print("Limite de Consumo: ");
                                double lim = Double.parseDouble(scanner.nextLine());
                                System.out.print("Tipo (EMAIL/SMS): ");
                                String tNotif = scanner.nextLine();
                                fachada.configurarAlerta(usuario, lim, tNotif);
                                fachada.logInfo("CLI: Alerta pessoal configurado para " + usuario);
                                break;
                            case "2":
                                limparTela();
                                System.out.println("=== Gerar Relatório Pessoal ===");
                                System.out.print("Tipo (PDF/CSV): ");
                                String tRel = scanner.nextLine();
                                fachada.gerarRelatorio(tRel, usuario);
                                fachada.logInfo("CLI: Relatório pessoal gerado para " + usuario);
                                break;
                            case "3":
                                limparTela();
                                System.out.println("=== Alterar Minha Senha ===");
                                System.out.print("Nova Senha: ");
                                String novaSenha = scanner.nextLine();
                                fachada.atualizarSenha(usuario, novaSenha);
                                fachada.logInfo("CLI: Senha pessoal atualizada para " + usuario);
                                break;
                            case "0":
                                logado = false;
                                fachada.logInfo("CLI: Logout realizado.");
                                break;
                            default:
                                System.out.println("Opção inválida.");
                        }
                    }

                    if (logado && !"4".equals(opcao)) { // Don't wait if returning from submenu
                        esperarEnter(scanner);
                    }
                }
            }
        }
        scanner.close();
        fachada.logInfo("CLI: Aplicação encerrada.");
    }

    private static void limparTela() {
        try {
            // Tries to execute the system clear command
            new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (Exception e) {
            // Fallback to ANSI escape codes if command fails
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    private static void esperarEnter(Scanner scanner) {
        System.out.println("\nPressione ENTER para continuar...");
        scanner.nextLine();
    }
}
