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
            cadastrarUsuario("admin", "Administrador", "admin", TipoUsuario.ADMIN, "A"); // Default admin to A
        }
    }

    public void cadastrarUsuario(String id, String nome, String senha, TipoUsuario tipo, String modeloAdapter) {
        String sql = "INSERT INTO usuarios(id, nome, senha, tipo, modelo_adapter) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, nome);
            pstmt.setString(3, senha);
            pstmt.setString(4, tipo.name());
            pstmt.setString(5, modeloAdapter);
            pstmt.executeUpdate();
            Logger.getInstance().logInfo(
                    "SGU: Usuário cadastrado com sucesso: " + nome + " (" + id + ") - Modelo: " + modeloAdapter);
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao cadastrar usuário: " + e.getMessage());
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

    public void atualizarConsumo(String id, double consumo) {
        String sql = "UPDATE usuarios SET consumo_atual = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, consumo);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            // Logger.getInstance().logInfo("SGU: Consumo atualizado para usuário " + id +
            // ": " + consumo);
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao atualizar consumo: " + e.getMessage());
        }
    }

    public Usuario getUsuarioPorId(String id) {
        String sql = "SELECT id, nome, senha, tipo, modelo_adapter, consumo_atual FROM usuarios WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Usuario(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        TipoUsuario.valueOf(rs.getString("tipo")),
                        rs.getString("modelo_adapter"),
                        rs.getDouble("consumo_atual"));
            }
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao buscar usuário: " + e.getMessage());
        }
        return null;
    }

    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nome, senha, tipo, modelo_adapter, consumo_atual FROM usuarios";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Usuario(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        TipoUsuario.valueOf(rs.getString("tipo")),
                        rs.getString("modelo_adapter"),
                        rs.getDouble("consumo_atual")));
            }
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao listar usuários: " + e.getMessage());
        }
        return lista;
    }

    public String listarUsuariosRaw() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM usuarios";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            sb.append(String.format("%-10s %-20s %-10s %-10s %-10s %-10s%n", "ID", "NOME", "SENHA", "TIPO", "MODELO",
                    "CONSUMO"));
            sb.append("------------------------------------------------------------------------------------------\n");
            while (rs.next()) {
                sb.append(String.format("%-10s %-20s %-10s %-10s %-10s %-10.2f%n",
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        rs.getString("tipo"),
                        rs.getString("modelo_adapter"),
                        rs.getDouble("consumo_atual")));
            }
        } catch (SQLException e) {
            return "Erro ao inspecionar DB: " + e.getMessage();
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

    // Overloaded for convenience if needed, but let's stick to the object one.
    // Actually, I need to update Usuario class to have getSenha.

    public void deletarUsuario(String id) {
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

    public Usuario getUsuarioPorSha(String idSha) {
        // First check if the SHA matches a User ID (automatic association)
        Usuario u = getUsuarioPorId(idSha);
        if (u != null) {
            return u;
        }

        // If we had a table for SHAs, we would query it here.
        // For now, since we only persist User ID as SHA, this is sufficient.
        return null;
    }
}
