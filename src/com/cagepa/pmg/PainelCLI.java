package com.cagepa.pmg;

import com.cagepa.pmg.sgu.TipoUsuario;
import java.util.Scanner;
import java.util.List;
// Removed unused imports

public class PainelCLI {

    public static void main(String[] args) {
        FachadaSistema fachada = new FachadaSistema();
        Scanner scanner = new Scanner(System.in);
        boolean executando = true;
        boolean logado = false;

        System.out.println("=== Painel de Monitoramento CAGEPA (PMG) v3.1 ===");
        System.out.println("NOTA: Os logs de execução estão sendo gravados em 'system.log'.");
        System.out.println("DICA: Abra outro terminal e execute 'tail -f system.log' para acompanhar os eventos.");

        while (executando) {
            if (!logado) {
                System.out.print("Login (ID): ");
                String usuario = scanner.nextLine();
                System.out.print("Senha: ");
                String senha = scanner.nextLine();

                if (fachada.autenticar(usuario, senha)) {
                    logado = true;
                    TipoUsuario tipo = fachada.getTipoUsuario(usuario);
                    System.out.println("Login realizado com sucesso! Perfil: " + tipo);

                    // Inner loop for logged in session
                    while (logado) {
                        if (tipo == TipoUsuario.ADMIN) {
                            System.out.println("\n--- Menu Principal (ADMIN) ---");
                            System.out.println("1. Visualizar Atualização de Vazão");
                            System.out.println("2. Configurar Alerta");
                            System.out.println("3. Gerar Relatório");
                            System.out.println("4. Cadastrar Usuário");
                            System.out.println("5. Listar Usuários");
                            System.out.println("6. Atualizar Senha de Usuário");
                            System.out.println("7. Deletar Usuário");
                            System.out.println("8. Inspecionar Banco de Dados");
                            System.out.println("9. Verificar Status do Hidrômetro");
                            System.out.println("0. Logout");
                        } else {
                            System.out.println("\n--- Menu Principal (PADRAO) ---");
                            System.out.println("1. Configurar Alerta Pessoal");
                            System.out.println("2. Gerar Relatório Pessoal");
                            System.out.println("3. Alterar Minha Senha");
                            System.out.println("4. Verificar Meu Status");
                            System.out.println("0. Logout");
                        }
                        System.out.print("Escolha uma opção: ");
                        String opcao = scanner.nextLine();

                        if (tipo == TipoUsuario.ADMIN) {
                            switch (opcao) {
                                case "1":
                                    // Visualizar Atualização de Vazão
                                    System.out.println("Iniciando visualização de vazão...");
                                    fachada.iniciarMonitoramento(); // Ensure monitoring is active

                                    System.out.println("Pressione ENTER para parar a visualização.");

                                    // Simple loop to show status
                                    // We need a way to break this loop.
                                    // Since Java Scanner blocks, we can't easily do a non-blocking check without
                                    // threads.
                                    // Let's use a separate thread for the display loop.

                                    Thread displayThread = new Thread(() -> {
                                        while (!Thread.currentThread().isInterrupted()) {
                                            try {
                                                // Clear screen (ANSI)
                                                System.out.print("\033[H\033[2J");
                                                System.out.flush();

                                                System.out.println("=== Monitoramento em Tempo Real ===");
                                                List<com.cagepa.pmg.sgu.Usuario> users = fachada.listarUsuarios();
                                                for (com.cagepa.pmg.sgu.Usuario u : users) {
                                                    String status = fachada.getStatusHidrometro(u.getId());
                                                    System.out.printf(
                                                            "ID: %-10s | Modelo: %-2s | Status: %-12s | Consumo: %.2f%n",
                                                            u.getId(), u.getModeloAdapter(), status,
                                                            u.getConsumoAtual());
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
                                    System.out.print("ID do Usuário Alvo: ");
                                    String idAlvo = scanner.nextLine();
                                    System.out.print("Limite: ");
                                    double lim = Double.parseDouble(scanner.nextLine());
                                    System.out.print("Tipo (EMAIL/SMS): ");
                                    String tNotif = scanner.nextLine();
                                    fachada.configurarAlerta(idAlvo, lim, tNotif);
                                    break;
                                case "3":
                                    System.out.print("Tipo (PDF/CSV): ");
                                    String tRel = scanner.nextLine();
                                    System.out.print("ID Usuário: ");
                                    String idRel = scanner.nextLine();
                                    fachada.gerarRelatorio(tRel, idRel);
                                    break;
                                case "4":
                                    System.out.print("ID: ");
                                    String nId = scanner.nextLine();
                                    System.out.print("Nome: ");
                                    String nNome = scanner.nextLine();
                                    System.out.print("Senha: ");
                                    String nSenha = scanner.nextLine();
                                    System.out.print("Tipo (ADMIN/PADRAO): ");
                                    String nTipoStr = scanner.nextLine();
                                    TipoUsuario nTipo = "ADMIN".equalsIgnoreCase(nTipoStr) ? TipoUsuario.ADMIN
                                            : TipoUsuario.PADRAO;
                                    System.out.print("Modelo do Adaptador (A/B/C): ");
                                    String nModelo = scanner.nextLine();
                                    fachada.cadastrarUsuario(nId, nNome, nSenha, nTipo, nModelo);
                                    break;
                                case "5":
                                    System.out.println("--- Lista de Usuários ---");
                                    List<com.cagepa.pmg.sgu.Usuario> usuarios = fachada.listarUsuarios();
                                    for (com.cagepa.pmg.sgu.Usuario u : usuarios) {
                                        System.out.printf("ID: %s | Nome: %s | Tipo: %s | Modelo: %s | Consumo: %.2f%n",
                                                u.getId(), u.getNome(), u.getTipo(), u.getModeloAdapter(),
                                                u.getConsumoAtual());
                                    }
                                    break;
                                case "6":
                                    System.out.print("ID do Usuário: ");
                                    String idUpd = scanner.nextLine();
                                    System.out.print("Nova Senha: ");
                                    String novaSenha = scanner.nextLine();
                                    fachada.atualizarSenha(idUpd, novaSenha);
                                    break;
                                case "7":
                                    System.out.print("ID do Usuário a deletar: ");
                                    String idDel = scanner.nextLine();
                                    fachada.deletarUsuario(idDel);
                                    break;
                                case "8":
                                    System.out.println(fachada.listarUsuariosRaw());
                                    break;
                                case "9":
                                    System.out.print("ID do Usuário: ");
                                    String idStat = scanner.nextLine();
                                    System.out.println("Status: " + fachada.getStatusHidrometro(idStat));
                                    break;
                                case "0":
                                    logado = false;
                                    break;
                                default:
                                    System.out.println("Opção inválida.");
                            }
                        } else {
                            // PADRAO User Menu
                            switch (opcao) {
                                case "1":
                                    System.out.print("Limite de Consumo: ");
                                    double lim = Double.parseDouble(scanner.nextLine());
                                    System.out.print("Tipo (EMAIL/SMS): ");
                                    String tNotif = scanner.nextLine();
                                    fachada.configurarAlerta(usuario, lim, tNotif); // Self-configuration
                                    break;
                                case "2":
                                    System.out.print("Tipo (PDF/CSV): ");
                                    String tRel = scanner.nextLine();
                                    fachada.gerarRelatorio(tRel, usuario); // Self-report
                                    break;
                                case "3":
                                    System.out.print("Nova Senha: ");
                                    String novaSenha = scanner.nextLine();
                                    fachada.atualizarSenha(usuario, novaSenha);
                                    break;
                                case "4":
                                    System.out.println("Status: " + fachada.getStatusHidrometro(usuario));
                                    break;
                                case "0":
                                    logado = false;
                                    break;
                                default:
                                    System.out.println("Opção inválida.");
                            }
                        }
                    }
                } else {
                    System.out.println("Credenciais inválidas. Tente novamente.");
                }
            }
        }
        scanner.close();
    }
}
