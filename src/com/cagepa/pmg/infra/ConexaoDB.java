package com.cagepa.pmg.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoDB {
    private static final String URL = "jdbc:sqlite:database.db";

    public static Connection conectar() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite não encontrado: " + e.getMessage());
        }
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            // Configurar timeout para evitar "database is locked" (30 segundos)
            stmt.execute("PRAGMA busy_timeout = 30000;");
            // Ativar modo WAL para melhor concorrência
            stmt.execute("PRAGMA journal_mode = WAL;");
        }
        return conn;
    }

    public static void inicializarBanco() {
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id TEXT PRIMARY KEY," +
                "nome TEXT NOT NULL," +
                "senha TEXT NOT NULL," +
                "tipo TEXT NOT NULL" +
                ");";

        String sqlHidrometros = "CREATE TABLE IF NOT EXISTS hidrometros (" +
                "id TEXT PRIMARY KEY," +
                "id_usuario TEXT NOT NULL," +
                "modelo TEXT NOT NULL," +
                "consumo_atual REAL DEFAULT 0.0," +
                "offset REAL DEFAULT 0.0," +
                "FOREIGN KEY(id_usuario) REFERENCES usuarios(id)" +
                ");";

        try (Connection conn = conectar();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlHidrometros);

            Logger.getInstance().logInfo("Banco de dados inicializado com sucesso.");
        } catch (SQLException e) {
            Logger.getInstance().logError("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace(); // Debugging
        }
    }

    // Métodos auxiliares para transações
    public static void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    public static void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true); // Restaurar padrão
        }
    }

    public static void rollbackTransaction(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
                // Não restauramos autoCommit aqui pois a conexão provavelmente será fechada ou
                // inutilizada
            }
        } catch (SQLException e) {
            Logger.getInstance().logError("Erro ao realizar rollback: " + e.getMessage());
        }
    }
}
