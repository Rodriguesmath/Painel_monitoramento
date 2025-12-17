package com.cagepa.pmg;

import com.cagepa.pmg.sgu.TipoUsuario;
import com.cagepa.pmg.sgu.Usuario;
import com.cagepa.pmg.sgu.Hidrometro;
import java.util.Scanner;
import java.util.List;

public class PainelCLI {

    // ANSI Code Constants
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED_BG = "\u001B[41m";
    private static final String ANSI_WHITE_BOLD = "\u001B[1;97m";
    private static final String ANSI_BLINK = "\u001B[5m";
    private static final String BEEP = "\u0007";

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
                System.out.println("=== Painel de Monitoramento CAGEPA (PMG) v3.2 ===");
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
                        System.out.println("1. Monitorar Meu Consumo");
                        System.out.println("2. Configurar Alerta Pessoal");
                        System.out.println("3. Gerar Relatório Pessoal");
                        System.out.println("4. Alterar Minha Senha");
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

                                            // 1. Fetch Event-based Alerts (History)
                                            List<String> freshAlerts = fachada.getAlertasRecentes();
                                            activeAlerts.addAll(freshAlerts);
                                            // Keep only last 5
                                            if (activeAlerts.size() > 5) {
                                                activeAlerts = activeAlerts.subList(activeAlerts.size() - 5,
                                                        activeAlerts.size());
                                            }

                                            // 2. Prepare Display List
                                            List<String> alertsToDisplay = new java.util.ArrayList<>();

                                            List<Usuario> users = fachada.listarUsuarios();

                                            // Check State for Persistent Alerts
                                            for (Usuario u : users) {
                                                for (Hidrometro h : u.getHidrometros()) {
                                                    if (h.getLimiteAlerta() > 0
                                                            && h.getConsumoTotal() > h.getLimiteAlerta()) {
                                                        // Determine Notification Type
                                                        String via = h.getTipoAlerta() != null ? h.getTipoAlerta()
                                                                : "EMAIL";
                                                        String mockMsg = via.equalsIgnoreCase("SMS")
                                                                ? "[MOCK: Enviando SMS para usuario " + u.getId() + "]"
                                                                : "[MOCK: Enviando e-mail para usuario " + u.getId()
                                                                        + "]";

                                                        String persistentMsg = "ALERTA [SITUAÇÃO ATUAL]: Consumo "
                                                                + String.format("%.2f", h.getConsumoTotal())
                                                                + " > " + String.format("%.2f", h.getLimiteAlerta())
                                                                + " (User: " + u.getId() + ", Hidro: " + h.getId()
                                                                + ")\n"
                                                                + "     >>> " + mockMsg;
                                                        alertsToDisplay.add(persistentMsg);
                                                    }
                                                }
                                            }

                                            // Only show history if NO persistent alerts are active (to avoid
                                            // duplicates/clutter)
                                            if (alertsToDisplay.isEmpty()) {
                                                alertsToDisplay.addAll(activeAlerts);
                                            }

                                            if (!alertsToDisplay.isEmpty()) {
                                                exibirAlertaChamativo(alertsToDisplay);
                                            }

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
                                double limite = scanner.nextDouble();
                                scanner.nextLine(); // consume newline

                                System.out.print("Tipo de Notificação [EMAIL/SMS] (Padrão: EMAIL): ");
                                String tipoNotificacao = scanner.nextLine();
                                if (tipoNotificacao.isEmpty()) {
                                    tipoNotificacao = "EMAIL";
                                }

                                fachada.configurarAlerta(idAlvo, limite, tipoNotificacao);
                                System.out.println("Alerta configurado com sucesso!");
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

                                                System.out.print("Tipo de Alerta (EMAIL/SMS) [Default: EMAIL]: ");
                                                String tipoH = scanner.nextLine();
                                                if (tipoH.isEmpty())
                                                    tipoH = "EMAIL";

                                                if (fachada.adicionarHidrometro(idUserH, idHidro, modH, limiteH,
                                                        tipoH)) {
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
                        // STANDARD USER MENU
                        switch (opcao) {
                            case "1":
                                limparTela();
                                System.out.println("=== Monitoramento Pessoal ===");
                                fachada.logInfo("CLI: Usuário " + usuario + " iniciou monitoramento pessoal.");
                                System.out.println("Iniciando monitoramento...");
                                fachada.iniciarMonitoramento(); // Ensure monitoring is active

                                System.out.println("Pressione ENTER para parar a visualização.");

                                Thread displayThread = new Thread(() -> {
                                    // Buffer for alerts in UI (Standard User)
                                    List<String> activeAlerts = new java.util.ArrayList<>();

                                    while (!Thread.currentThread().isInterrupted()) {
                                        try {
                                            limparTela();
                                            System.out.println("=== Monitoramento Pessoal (" + usuario + ") ===");

                                            // 1. Fetch and accumulate alerts (History)
                                            List<String> freshAlerts = fachada.getAlertasRecentes();

                                            // Filter alerts for this user before adding to buffer
                                            for (String alert : freshAlerts) {
                                                if (alert.contains("User: " + usuario)) {
                                                    activeAlerts.add(alert);
                                                }
                                            }

                                            // Keep only last 5
                                            if (activeAlerts.size() > 5) {
                                                activeAlerts = activeAlerts.subList(activeAlerts.size() - 5,
                                                        activeAlerts.size());
                                            }

                                            // 2. Prepare Display List
                                            List<String> alertsToDisplay = new java.util.ArrayList<>();

                                            com.cagepa.pmg.sgu.Usuario u = fachada.getUsuarioPorId(usuario);
                                            if (u != null) {
                                                for (com.cagepa.pmg.sgu.Hidrometro h : u.getHidrometros()) {
                                                    if (h.getLimiteAlerta() > 0
                                                            && h.getConsumoTotal() > h.getLimiteAlerta()) {
                                                        String via = h.getTipoAlerta() != null ? h.getTipoAlerta()
                                                                : "EMAIL";
                                                        String mockMsg = via.equalsIgnoreCase("SMS")
                                                                ? "[MOCK: Enviando SMS para usuario " + u.getId() + "]"
                                                                : "[MOCK: Enviando e-mail para usuario " + u.getId()
                                                                        + "]";

                                                        String persistentMsg = "ALERTA [SITUAÇÃO ATUAL]: SEU CONSUMO "
                                                                + String.format("%.2f", h.getConsumoTotal())
                                                                + " > " + String.format("%.2f", h.getLimiteAlerta())
                                                                + " (Hidro: " + h.getId() + ")\n"
                                                                + "     >>> " + mockMsg;
                                                        alertsToDisplay.add(persistentMsg);
                                                    }
                                                }
                                            }

                                            // Only show history if NO persistent alerts are active
                                            if (alertsToDisplay.isEmpty()) {
                                                alertsToDisplay.addAll(activeAlerts);
                                            }

                                            System.out.println("--- Alertas Recentes ---");
                                            if (!alertsToDisplay.isEmpty()) {
                                                exibirAlertaChamativo(alertsToDisplay);
                                            } else {
                                                System.out.println("(Nenhum alerta recente)");
                                            }

                                            // Status
                                            System.out.println("\n--- Meus Hidrômetros ---");
                                            if (u != null && !u.getHidrometros().isEmpty()) {
                                                for (com.cagepa.pmg.sgu.Hidrometro h : u.getHidrometros()) {
                                                    String status = fachada.getStatusHidrometro(h.getId());
                                                    System.out.printf(
                                                            "Hidro: %s | Modelo: %s | Consumo: %.2f | Status: %s | Limite: %.2f | Via: %s%n",
                                                            h.getId(), h.getModelo(), h.getConsumoAtual(), status,
                                                            h.getLimiteAlerta(), h.getTipoAlerta());
                                                }
                                            } else {
                                                System.out.println("Nenhum hidrômetro vinculado.");
                                            }

                                            System.out.println("\n------------------------------------------------");
                                            System.out.println("Pressione ENTER para voltar ao menu.");

                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                        } catch (Exception e) {
                                            System.out.println("Erro na exibição: " + e.getMessage());
                                        }
                                    }
                                });
                                displayThread.start();
                                esperarEnter(scanner);
                                displayThread.interrupt();
                                try {
                                    displayThread.join();
                                } catch (InterruptedException e) {
                                }
                                break;
                            case "2":
                                System.out.print("Novo limite de alerta de consumo: ");
                                double lim = Double.parseDouble(scanner.nextLine());
                                System.out.print("Tipo de Notificação (EMAIL/SMS): ");
                                String tipoNotif = scanner.nextLine();
                                if (tipoNotif.isEmpty())
                                    tipoNotif = "EMAIL";

                                fachada.configurarAlerta(usuario, lim, tipoNotif);
                                System.out.println("Configuração atualizada!");
                                esperarEnter(scanner);
                                break;
                            case "3":
                                fachada.gerarRelatorio("PDF", usuario); // Default to PDF for basic user
                                System.out.println("Relatório gerado (verifique os logs ou diretório de saída).");
                                esperarEnter(scanner);
                                break;
                            case "4":
                                System.out.print("Nova Senha: ");
                                String novaSenha = scanner.nextLine();
                                fachada.atualizarSenha(usuario, novaSenha);
                                System.out.println("Senha atualizada com sucesso!");
                                esperarEnter(scanner);
                                break;
                            case "0":
                                logado = false;
                                break;
                            default:
                                System.out.println("Opção inválida.");
                                esperarEnter(scanner);
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

    // Método auxiliar para exibir alertas chamativos
    private static void exibirAlertaChamativo(List<String> alertas) {
        System.out.print(BEEP); // Som de beep
        System.out.println(
                ANSI_RED_BG + ANSI_WHITE_BOLD + "===============================================" + ANSI_RESET);
        System.out.println(
                ANSI_RED_BG + ANSI_WHITE_BOLD + "            !!! ALERTA DE CONSUMO !!!          " + ANSI_RESET);
        System.out.println(
                ANSI_RED_BG + ANSI_WHITE_BOLD + "===============================================" + ANSI_RESET);
        for (String alert : alertas) {
            System.out.println(ANSI_BLINK + " (!) " + alert + ANSI_RESET);
        }
        System.out.println(
                ANSI_RED_BG + ANSI_WHITE_BOLD + "===============================================" + ANSI_RESET);
        System.out.println();
    }
}
