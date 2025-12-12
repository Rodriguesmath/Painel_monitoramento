package com.cagepa.pmg;

import com.cagepa.pmg.sgu.TipoUsuario;
import com.cagepa.pmg.sgu.Usuario;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PainelTUI {
    private static FachadaSistema fachada = new FachadaSistema();
    private static MultiWindowTextGUI gui;

    public static void main(String[] args) {
        try {
            // Setup terminal and screen
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();

            // Create GUI with Custom Theme
            gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            gui.setTheme(createCustomTheme());

            // Show Login Window
            showLoginWindow();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Theme createCustomTheme() {
        // Using the 8-argument makeTheme for better control over highlights and inputs
        // Args: isDark, normalFG, normalBG, selectedFG, selectedBG, editableFG,
        // editableBG, windowDecorationBG
        return SimpleTheme.makeTheme(
                true,
                TextColor.ANSI.WHITE, TextColor.ANSI.BLUE, // Normal
                TextColor.ANSI.BLACK, TextColor.ANSI.CYAN, // Selected
                TextColor.ANSI.BLACK, TextColor.ANSI.WHITE, // Editable (Input fields)
                TextColor.ANSI.BLUE // Window Decoration
        );
    }

    private static String getBanner() {
        return "   _______  _______  _______  _______  _______  _______ \n" +
                "  (  ____ \\(  ___  )(  ____ \\(  ____ \\(  ____ )(  ___  )\n" +
                "  | (    \\/| (   ) || (    \\/| (    \\/| (    )|| (   ) |\n" +
                "  | |      | (___) || |      | (__    | (____)|| (___) |\n" +
                "  | |      |  ___  || | ____ |  __)   |  _____)|  ___  |\n" +
                "  | |      | (   ) || | \\_  )| (      | (      | (   ) |\n" +
                "  | (____/\\| )   ( || (___) || (____/\\| )      | )   ( |\n" +
                "  (_______/|/     \\|(_______)(_______/|/       |/     \\|\n" +
                "            Painel de Monitoramento Global v3.1         ";
    }

    private static void showLoginWindow() {
        BasicWindow window = new BasicWindow("Login");
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        // Banner
        mainPanel.addComponent(new Label(getBanner()).setForegroundColor(TextColor.ANSI.CYAN));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        // Form Panel with Border
        Panel formPanel = new Panel(new GridLayout(2));
        formPanel.addComponent(new Label("ID do Usuário:"));
        TextBox txtUsuario = new TextBox();
        formPanel.addComponent(txtUsuario);

        formPanel.addComponent(new Label("Senha:"));
        TextBox txtSenha = new TextBox().setMask('*');
        formPanel.addComponent(txtSenha);

        mainPanel.addComponent(formPanel.withBorder(Borders.doubleLine("Credenciais")));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        // Buttons
        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Button btnLogin = new Button("Entrar", () -> {
            String usuario = txtUsuario.getText();
            String senha = txtSenha.getText();

            if ("debug".equals(usuario) && "debug".equals(senha)) {
                window.close();
                showMainMenu(usuario, true);
            } else if (fachada.autenticar(usuario, senha)) {
                window.close();
                showMainMenu(usuario, false);
            } else {
                MessageDialog.showMessageDialog(gui, "Erro", "Credenciais inválidas!", MessageDialogButton.OK);
            }
        });
        buttonPanel.addComponent(btnLogin);
        buttonPanel.addComponent(new Button("Sair", () -> System.exit(0)));

        mainPanel.addComponent(buttonPanel);

        window.setComponent(mainPanel);
        gui.addWindowAndWait(window);
    }

    private static void showMainMenu(String usuarioId, boolean isDebug) {
        if (isDebug) {
            showAdminMenu(usuarioId, true);
            return;
        }
        TipoUsuario tipo = fachada.getTipoUsuario(usuarioId);
        if (tipo == TipoUsuario.ADMIN) {
            showAdminMenu(usuarioId, false);
        } else {
            showStandardMenu(usuarioId);
        }
    }

    private static void showAdminMenu(String usuarioId, boolean isDebug) {
        BasicWindow window = new BasicWindow("Menu Principal (ADMIN)");
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.addComponent(
                new Label("Bem-vindo, Administrador " + usuarioId + "!").setForegroundColor(TextColor.ANSI.CYAN));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel menuPanel = new Panel(new GridLayout(2)); // 2 Columns for better layout

        menuPanel.addComponent(new Button("1. Monitoramento Real-Time", () -> showRealTimeMonitoring()));
        menuPanel.addComponent(new Button("2. Configurar Alerta", () -> showConfigurarAlerta(null)));
        menuPanel.addComponent(new Button("3. Gerar Relatório", () -> showGerarRelatorio(null)));
        menuPanel.addComponent(new Button("4. Gestão de Usuários", () -> showUserManagementMenu()));

        if (isDebug) {
            menuPanel.addComponent(new Button("5. Inspecionar BD", () -> showInspecionarBD()));
        }

        mainPanel.addComponent(menuPanel.withBorder(Borders.singleLine("Ações")));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        mainPanel.addComponent(new Button("Logout", window::close));

        window.setComponent(mainPanel);
        gui.addWindowAndWait(window);
        showLoginWindow();
    }

    private static void showUserManagementMenu() {
        BasicWindow window = new BasicWindow("Gestão de Usuários");
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.addComponent(new Label("Gerenciamento de Usuários").setForegroundColor(TextColor.ANSI.CYAN));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel menuPanel = new Panel(new GridLayout(2));

        menuPanel.addComponent(new Button("1. Cadastrar Usuário", () -> showCadastrarUsuario()));
        menuPanel.addComponent(new Button("2. Listar Usuários", () -> showListarUsuarios()));
        menuPanel.addComponent(new Button("3. Atualizar Senha", () -> showAtualizarSenha(null)));
        menuPanel.addComponent(new Button("4. Deletar Usuário", () -> showDeletarUsuario()));

        mainPanel.addComponent(menuPanel.withBorder(Borders.singleLine("Opções")));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        mainPanel.addComponent(new Button("Voltar", window::close));

        window.setComponent(mainPanel);
        gui.addWindowAndWait(window);
    }

    private static void showStandardMenu(String usuarioId) {
        BasicWindow window = new BasicWindow("Menu Principal");
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.addComponent(new Label("Bem-vindo, " + usuarioId + "!").setForegroundColor(TextColor.ANSI.CYAN));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel menuPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        menuPanel.addComponent(new Button("1. Configurar Alerta Pessoal", () -> showConfigurarAlerta(usuarioId)));
        menuPanel.addComponent(new Button("2. Gerar Relatório Pessoal", () -> showGerarRelatorio(usuarioId)));
        menuPanel.addComponent(new Button("3. Alterar Minha Senha", () -> showAtualizarSenha(usuarioId)));

        mainPanel.addComponent(menuPanel.withBorder(Borders.singleLine("Minhas Ações")));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        mainPanel.addComponent(new Button("Logout", window::close));

        window.setComponent(mainPanel);
        gui.addWindowAndWait(window);
        showLoginWindow();
    }

    private static void showRealTimeMonitoring() {
        fachada.iniciarMonitoramento();
        BasicWindow window = new BasicWindow("Monitoramento em Tempo Real");
        window.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.EXPANDED));

        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new Label("Monitorando Sensores...").setForegroundColor(TextColor.ANSI.YELLOW));
        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Label statusLabel = new Label("Carregando dados...");
        panel.addComponent(statusLabel);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        panel.addComponent(new Button("Voltar", window::close));

        window.setComponent(panel.withBorder(Borders.doubleLine()));

        Thread updateThread = new Thread(() -> {
            while (window.getTextGUI() != null) {
                try {
                    Thread.sleep(1000);
                    StringBuilder sb = new StringBuilder();
                    List<Usuario> users = fachada.listarUsuarios();
                    for (Usuario u : users) {
                        // Admin does not have a hydrometer to monitor
                        if (u.getTipo() == TipoUsuario.ADMIN)
                            continue;

                        String status = fachada.getStatusHidrometro(u.getId());
                        sb.append(String.format("Nome: %-20s | Modelo: %-2s | Status: %-12s | Consumo: %.2f\n",
                                u.getNome(), u.getModeloAdapter(), status, u.getConsumoAtual()));
                    }
                    String finalStatus = sb.toString();
                    if (finalStatus.isEmpty())
                        finalStatus = "Nenhum usuário padrão para monitorar.";

                    String statusToDisplay = finalStatus;
                    try {
                        window.getTextGUI().getGUIThread().invokeLater(() -> statusLabel.setText(statusToDisplay));
                    } catch (Exception e) {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateThread.start();

        gui.addWindowAndWait(window);
        updateThread.interrupt();
    }

    private static void showConfigurarAlerta(String preFilledId) {
        BasicWindow window = new BasicWindow("Configurar Alerta");
        window.setHints(Arrays.asList(Window.Hint.CENTERED));
        Panel panel = new Panel(new GridLayout(2));

        panel.addComponent(new Label("ID do Usuário:"));
        TextBox txtId = new TextBox(preFilledId != null ? preFilledId : "");
        if (preFilledId != null)
            txtId.setReadOnly(true);
        panel.addComponent(txtId);

        panel.addComponent(new Label("Limite de Consumo:"));
        TextBox txtLimite = new TextBox();
        panel.addComponent(txtLimite);

        panel.addComponent(new Label("Tipo (EMAIL/SMS):"));
        ComboBox<String> cbTipo = new ComboBox<>("EMAIL", "SMS");
        panel.addComponent(cbTipo);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel btnPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        btnPanel.addComponent(new Button("Salvar", () -> {
            try {
                double limite = Double.parseDouble(txtLimite.getText());
                fachada.configurarAlerta(txtId.getText(), limite, cbTipo.getSelectedItem());
                MessageDialog.showMessageDialog(gui, "Sucesso", "Alerta configurado!", MessageDialogButton.OK);
                window.close();
            } catch (NumberFormatException e) {
                MessageDialog.showMessageDialog(gui, "Erro", "Limite inválido!", MessageDialogButton.OK);
            }
        }));
        btnPanel.addComponent(new Button("Cancelar", window::close));

        Panel mainPanel = new Panel();
        mainPanel.addComponent(panel);
        mainPanel.addComponent(btnPanel);

        window.setComponent(mainPanel.withBorder(Borders.singleLine()));
        gui.addWindowAndWait(window);
    }

    private static void showGerarRelatorio(String preFilledId) {
        BasicWindow window = new BasicWindow("Gerar Relatório");
        window.setHints(Arrays.asList(Window.Hint.CENTERED));
        Panel panel = new Panel(new GridLayout(2));

        panel.addComponent(new Label("ID do Usuário:"));
        TextBox txtId = new TextBox(preFilledId != null ? preFilledId : "");
        if (preFilledId != null)
            txtId.setReadOnly(true);
        panel.addComponent(txtId);

        panel.addComponent(new Label("Formato:"));
        ComboBox<String> cbFormato = new ComboBox<>("PDF", "CSV");
        panel.addComponent(cbFormato);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel btnPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        btnPanel.addComponent(new Button("Gerar", () -> {
            fachada.gerarRelatorio(cbFormato.getSelectedItem(), txtId.getText());
            MessageDialog.showMessageDialog(gui, "Sucesso", "Relatório gerado!", MessageDialogButton.OK);
            window.close();
        }));
        btnPanel.addComponent(new Button("Cancelar", window::close));

        Panel mainPanel = new Panel();
        mainPanel.addComponent(panel);
        mainPanel.addComponent(btnPanel);

        window.setComponent(mainPanel.withBorder(Borders.singleLine()));
        gui.addWindowAndWait(window);
    }

    private static void showCadastrarUsuario() {
        BasicWindow window = new BasicWindow("Cadastrar Usuário");
        window.setHints(Arrays.asList(Window.Hint.CENTERED));
        Panel panel = new Panel(new GridLayout(2));

        panel.addComponent(new Label("ID:"));
        TextBox txtId = new TextBox();
        panel.addComponent(txtId);

        panel.addComponent(new Label("Nome:"));
        TextBox txtNome = new TextBox();
        panel.addComponent(txtNome);

        panel.addComponent(new Label("Senha:"));
        TextBox txtSenha = new TextBox();
        panel.addComponent(txtSenha);

        panel.addComponent(new Label("Tipo:"));
        ComboBox<TipoUsuario> cbTipo = new ComboBox<>(TipoUsuario.values());
        panel.addComponent(cbTipo);

        panel.addComponent(new Label("Modelo Adaptador:"));
        ComboBox<String> cbModelo = new ComboBox<>("A", "B", "C");
        panel.addComponent(cbModelo);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel btnPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        btnPanel.addComponent(new Button("Cadastrar", () -> {
            fachada.cadastrarUsuario(txtId.getText(), txtNome.getText(), txtSenha.getText(), cbTipo.getSelectedItem(),
                    cbModelo.getSelectedItem());
            MessageDialog.showMessageDialog(gui, "Sucesso", "Usuário cadastrado!", MessageDialogButton.OK);
            window.close();
        }));
        btnPanel.addComponent(new Button("Cancelar", window::close));

        Panel mainPanel = new Panel();
        mainPanel.addComponent(panel);
        mainPanel.addComponent(btnPanel);

        window.setComponent(mainPanel.withBorder(Borders.singleLine()));
        gui.addWindowAndWait(window);
    }

    private static void showListarUsuarios() {
        BasicWindow window = new BasicWindow("Lista de Usuários");
        window.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.EXPANDED));
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        List<Usuario> usuarios = fachada.listarUsuarios();
        for (Usuario u : usuarios) {
            panel.addComponent(new Label(String.format("Nome: %s | Modelo: %s | Consumo: %.2f",
                    u.getNome(), u.getModeloAdapter(), u.getConsumoAtual())));
        }

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        panel.addComponent(new Button("Fechar", window::close));

        window.setComponent(panel.withBorder(Borders.doubleLine()));
        gui.addWindowAndWait(window);
    }

    private static void showAtualizarSenha(String preFilledId) {
        BasicWindow window = new BasicWindow("Atualizar Senha");
        window.setHints(Arrays.asList(Window.Hint.CENTERED));
        Panel panel = new Panel(new GridLayout(2));

        panel.addComponent(new Label("ID do Usuário:"));
        TextBox txtId = new TextBox(preFilledId != null ? preFilledId : "");
        if (preFilledId != null)
            txtId.setReadOnly(true);
        panel.addComponent(txtId);

        panel.addComponent(new Label("Nova Senha:"));
        TextBox txtSenha = new TextBox();
        panel.addComponent(txtSenha);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel btnPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        btnPanel.addComponent(new Button("Atualizar", () -> {
            fachada.atualizarSenha(txtId.getText(), txtSenha.getText());
            MessageDialog.showMessageDialog(gui, "Sucesso", "Senha atualizada!", MessageDialogButton.OK);
            window.close();
        }));
        btnPanel.addComponent(new Button("Cancelar", window::close));

        Panel mainPanel = new Panel();
        mainPanel.addComponent(panel);
        mainPanel.addComponent(btnPanel);

        window.setComponent(mainPanel.withBorder(Borders.singleLine()));
        gui.addWindowAndWait(window);
    }

    private static void showDeletarUsuario() {
        BasicWindow window = new BasicWindow("Deletar Usuário");
        window.setHints(Arrays.asList(Window.Hint.CENTERED));
        Panel panel = new Panel(new GridLayout(2));

        panel.addComponent(new Label("ID do Usuário:"));
        TextBox txtId = new TextBox();
        panel.addComponent(txtId);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Panel btnPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        btnPanel.addComponent(new Button("Deletar", () -> {
            fachada.deletarUsuario(txtId.getText());
            MessageDialog.showMessageDialog(gui, "Sucesso", "Usuário deletado!", MessageDialogButton.OK);
            window.close();
        }));
        btnPanel.addComponent(new Button("Cancelar", window::close));

        Panel mainPanel = new Panel();
        mainPanel.addComponent(panel);
        mainPanel.addComponent(btnPanel);

        window.setComponent(mainPanel.withBorder(Borders.singleLine()));
        gui.addWindowAndWait(window);
    }

    private static void showInspecionarBD() {
        BasicWindow window = new BasicWindow("Inspecionar Banco de Dados");
        window.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.EXPANDED));
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        String rawData = fachada.listarUsuariosRaw();
        for (String line : rawData.split("\n")) {
            panel.addComponent(new Label(line));
        }

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        panel.addComponent(new Button("Fechar", window::close));

        window.setComponent(panel.withBorder(Borders.doubleLine()));
        gui.addWindowAndWait(window);
    }
}
