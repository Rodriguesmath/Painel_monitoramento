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

    public Usuario getUsuarioPorId(String id) {
        String sql = "SELECT id, nome, senha, tipo FROM usuarios WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Usuario(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        TipoUsuario.valueOf(rs.getString("tipo")));
            }
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao buscar usuário: " + e.getMessage());
        }
        return null;
    }

    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nome, senha, tipo FROM usuarios";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Usuario(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        TipoUsuario.valueOf(rs.getString("tipo"))));
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
            sb.append(String.format("%-10s %-20s %-10s %-10s%n", "ID", "NOME", "SENHA", "TIPO"));
            sb.append("------------------------------------------------------------\n");
            while (rs.next()) {
                sb.append(String.format("%-10s %-20s %-10s %-10s%n",
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        rs.getString("tipo")));
            }
        } catch (SQLException e) {
            return "Erro ao inspecionar DB: " + e.getMessage();
        }
        return sb.toString();
    }

    public void atualizarUsuario(Usuario u) {
        String sql = "UPDATE usuarios SET nome = ?, senha = ?, tipo = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getNome());
            // Wait, Usuario doesn't expose getSenha. Let's fix this or assume we pass
            // fields.
            // For now, let's assume we can't update password easily without changing
            // Usuario.
            // Actually, let's just update Name and Type for now to be safe, or add getSenha
            // to Usuario.
            // Let's add getSenha to Usuario in a separate step if needed.
            // For this step, I will assume I need to pass the password explicitly or modify
            // Usuario.
            // Let's modify the method signature to take fields instead of object for
            // clarity.
            // But the interface said atualizarUsuario(Usuario u).
            // I will implement a helper method in SGU that takes fields.
            // Let's stick to the plan: atualizarUsuario(Usuario u). I need to add getSenha
            // to Usuario.
            // I'll assume I can add getSenha to Usuario in the next step.
            pstmt.setString(2, u.getSenha());
            pstmt.setString(3, u.getTipo().name());
            pstmt.setString(4, u.getId());
            pstmt.executeUpdate();
            Logger.getInstance().logInfo("SGU: Usuário atualizado: " + u.getId());
        } catch (SQLException e) {
            Logger.getInstance().logError("SGU: Erro ao atualizar usuário: " + e.getMessage());
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
