package com.cagepa.pmg.sgu;

import com.cagepa.pmg.infra.ConexaoDB;
import com.cagepa.pmg.infra.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SGU {

    public SGU() {
        ConexaoDB.inicializarBanco();
        // Ensure admin exists
        if (getUsuarioPorId("admin") == null) {
            cadastrarUsuario("admin", "Administrador", "admin", TipoUsuario.ADMIN);
        }
    }

    public void cadastrarUsuario(String id, String nome, String senha, TipoUsuario tipo) {
        String sql = "INSERT INTO usuarios(id, nome, senha, tipo) VALUES(?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, nome);
            pstmt.setString(3, senha);
            pstmt.setString(4, tipo.name());
            pstmt.executeUpdate();
            Logger.getInstance().logInfo("SGU: Usuário cadastrado com sucesso: " + nome + " (" + id + ")");
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao cadastrar usuário: " + e.getMessage());
        }
    }

    public void adicionarHidrometro(String idUsuario, String idHidrometro, String modelo) {
        String sql = "INSERT INTO hidrometros(id, id_usuario, modelo, consumo_atual) VALUES(?, ?, ?, 0.0)";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idHidrometro);
            pstmt.setString(2, idUsuario);
            pstmt.setString(3, modelo);
            pstmt.executeUpdate();
            Logger.getInstance().logInfo(
                    "SGU: Hidrômetro " + idHidrometro + " (" + modelo + ") adicionado ao usuário " + idUsuario);
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao adicionar hidrômetro: " + e.getMessage());
        }
    }

    public boolean autenticar(String id, String senha) {
        String sql = "SELECT senha FROM usuarios WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String senhaArmazenada = rs.getString("senha");
                if (senhaArmazenada.equals(senha)) {
                    Logger.getInstance().logInfo("SGU: Usuário autenticado: " + id);
                    return true;
                }
            }
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro na autenticação: " + e.getMessage());
        }
        Logger.getInstance().logInfo("SGU: Falha na autenticação para " + id);
        return false;
    }

    public TipoUsuario getTipoUsuario(String id) {
        Usuario u = getUsuarioPorId(id);
        return u != null ? u.getTipo() : null;
    }

    public void atualizarConsumo(String idHidrometro, double consumo) {
        String sql = "UPDATE hidrometros SET consumo_atual = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, consumo);
            pstmt.setString(2, idHidrometro);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // Logger.getInstance().logInfo("SGU: Consumo atualizado para hidrômetro " +
                // idHidrometro + ": " + consumo);
            } else {
                Logger.getInstance()
                        .logError("SGU: Hidrômetro não encontrado para atualização de consumo: " + idHidrometro);
            }
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao atualizar consumo: " + e.getMessage());
        }
    }

    public Usuario getUsuarioPorId(String id) {
        String sql = "SELECT id, nome, senha, tipo FROM usuarios WHERE id = ?";
        Usuario usuario = null;
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                usuario = new Usuario(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        TipoUsuario.valueOf(rs.getString("tipo")));
            }
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao buscar usuário: " + e.getMessage());
        }

        if (usuario != null) {
            carregarHidrometros(usuario);
        }
        return usuario;
    }

    private void carregarHidrometros(Usuario usuario) {
        String sql = "SELECT id, modelo, consumo_atual FROM hidrometros WHERE id_usuario = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                usuario.adicionarHidrometro(new Hidrometro(
                        rs.getString("id"),
                        usuario.getId(),
                        rs.getString("modelo"),
                        rs.getDouble("consumo_atual")));
            }
        } catch (SQLException e) {
            Logger.getInstance().logError(
                    "SGU: Erro ao carregar hidrômetros do usuário " + usuario.getId() + ": " + e.getMessage());
        }
    }

    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nome, senha, tipo FROM usuarios";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        TipoUsuario.valueOf(rs.getString("tipo")));
                carregarHidrometros(u);
                lista.add(u);
            }
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao listar usuários: " + e.getMessage());
        }
        return lista;
    }

    public String listarUsuariosRaw() {
        StringBuilder sb = new StringBuilder();
        List<Usuario> usuarios = listarUsuarios();

        sb.append(String.format("%-10s %-20s %-10s %-10s %-15s %-10s %-10s%n", "ID USER", "NOME", "SENHA", "TIPO",
                "ID HIDRO", "MODELO", "CONSUMO"));
        sb.append(
                "---------------------------------------------------------------------------------------------------------\n");

        for (Usuario u : usuarios) {
            List<Hidrometro> hidros = u.getHidrometros();
            if (hidros.isEmpty()) {
                sb.append(String.format("%-10s %-20s %-10s %-10s %-15s %-10s %-10s%n",
                        u.getId(), u.getNome(), u.getSenha(), u.getTipo(), "-", "-", "-"));
            } else {
                for (Hidrometro h : hidros) {
                    sb.append(String.format("%-10s %-20s %-10s %-10s %-15s %-10s %-10.2f%n",
                            u.getId(), u.getNome(), u.getSenha(), u.getTipo(), h.getId(), h.getModelo(),
                            h.getConsumoAtual()));
                }
            }
        }
        return sb.toString();
    }

    public void atualizarSenha(String id, String novaSenha) {
        String sql = "UPDATE usuarios SET senha = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, novaSenha);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            Logger.getInstance().logInfo("SGU: Senha atualizada para usuário: " + id);
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao atualizar senha: " + e.getMessage());
        }
    }

    public void deletarUsuario(String id) {
        // First delete associated hydrometers
        String sqlHidro = "DELETE FROM hidrometros WHERE id_usuario = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sqlHidro)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao deletar hidrômetros do usuário: " + e.getMessage());
        }

        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            Logger.getInstance().logInfo("SGU: Usuário deletado: " + id);
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao deletar usuário: " + e.getMessage());
        }
    }

    public Usuario getUsuarioPorHidrometro(String idHidrometro) {
        String sql = "SELECT id_usuario FROM hidrometros WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idHidrometro);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return getUsuarioPorId(rs.getString("id_usuario"));
            }
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao buscar usuário por hidrômetro: " + e.getMessage());
        }
        return null;
    }
}
