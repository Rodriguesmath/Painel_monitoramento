package com.cagepa.pmg;

import com.cagepa.pmg.sgu.TipoUsuario;
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
                            System.out.println("1. Iniciar Monitoramento Contínuo");
                            System.out.println("2. Parar Monitoramento");
                            System.out.println("3. Configurar Alerta");
                            System.out.println("4. Gerar Relatório");
                            System.out.println("5. Cadastrar Usuário");
                            System.out.println("6. Listar Usuários");
                            System.out.println("7. Atualizar Usuário");
                            System.out.println("8. Deletar Usuário");
                            System.out.println("9. Inspecionar Banco de Dados");
                            System.out.println("0. Logout");
                        } else {
                            System.out.println("\n--- Menu Principal (PADRAO) ---");
                            System.out.println("1. Configurar Alerta Pessoal");
                            System.out.println("2. Gerar Relatório Pessoal");
                            System.out.println("0. Logout");
                        }
                        System.out.print("Escolha uma opção: ");
                        String opcao = scanner.nextLine();

                        if (tipo == TipoUsuario.ADMIN) {
                            switch (opcao) {
                                case "1":
                                    fachada.iniciarMonitoramento();
                                    break;
                                case "2":
                                    fachada.pararMonitoramento();
                                    break;
                                case "3":
                                    System.out.print("ID do Usuário Alvo: ");
                                    String idAlvo = scanner.nextLine();
                                    System.out.print("Limite: ");
                                    double lim = Double.parseDouble(scanner.nextLine());
                                    System.out.print("Tipo (EMAIL/SMS): ");
                                    String tNotif = scanner.nextLine();
                                    fachada.configurarAlerta(idAlvo, lim, tNotif);
                                    break;
                                case "4":
                                    System.out.print("Tipo (PDF/CSV): ");
                                    String tRel = scanner.nextLine();
                                    System.out.print("ID Usuário: ");
                                    String idRel = scanner.nextLine();
                                    fachada.gerarRelatorio(tRel, idRel);
                                    break;
                                case "5":
                                    System.out.print("ID Novo: ");
                                    String nId = scanner.nextLine();
                                    System.out.print("Nome: ");
                                    String nNome = scanner.nextLine();
                                    System.out.print("Senha: ");
                                    String nSenha = scanner.nextLine();
                                    System.out.print("Tipo (ADMIN/PADRAO): ");
                                    String nTipoStr = scanner.nextLine();
                                    TipoUsuario nTipo = "ADMIN".equalsIgnoreCase(nTipoStr) ? TipoUsuario.ADMIN
                                            : TipoUsuario.PADRAO;
                                    fachada.cadastrarUsuario(nId, nNome, nSenha, nTipo);
                                    break;
                                case "6":
                                    System.out.println("--- Lista de Usuários ---");
                                    for (com.cagepa.pmg.sgu.Usuario u : fachada.listarUsuarios()) {
                                        System.out.println(u.getId() + " - " + u.getNome() + " [" + u.getTipo() + "]");
                                    }
                                    break;
                                case "7":
                                    System.out.print("ID do Usuário a Atualizar: ");
                                    String atId = scanner.nextLine();
                                    System.out.print("Novo Nome: ");
                                    String atNome = scanner.nextLine();
                                    System.out.print("Nova Senha: ");
                                    String atSenha = scanner.nextLine();
                                    System.out.print("Novo Tipo (ADMIN/PADRAO): ");
                                    String atTipoStr = scanner.nextLine();
                                    TipoUsuario atTipo = "ADMIN".equalsIgnoreCase(atTipoStr) ? TipoUsuario.ADMIN
                                            : TipoUsuario.PADRAO;
                                    fachada.atualizarUsuario(atId, atNome, atSenha, atTipo);
                                    break;
                                case "8":
                                    System.out.print("ID do Usuário a Deletar: ");
                                    String delId = scanner.nextLine();
                                    fachada.deletarUsuario(delId);
                                    break;
                                case "9":
                                    System.out.println("--- Inspeção do Banco de Dados ---");
                                    System.out.println(fachada.listarUsuariosRaw());
                                    break;
                                case "0":
                                    logado = false;
                                    fachada.pararMonitoramento();
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
