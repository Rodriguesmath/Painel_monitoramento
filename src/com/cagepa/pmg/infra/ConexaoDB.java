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
        return DriverManager.getConnection(URL);
    }

    public static void inicializarBanco() {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id TEXT PRIMARY KEY," +
                "nome TEXT NOT NULL," +
                "senha TEXT NOT NULL," +
                "tipo TEXT NOT NULL," +
                "modelo_adapter TEXT" + // New column
                ");";

        try (Connection conn = conectar();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Migration for existing tables without the column
            try {
                stmt.execute("ALTER TABLE usuarios ADD COLUMN modelo_adapter TEXT");
            } catch (SQLException e) {
                // Column likely already exists, ignore
            }

            try {
                stmt.execute("ALTER TABLE usuarios ADD COLUMN consumo_atual REAL DEFAULT 0.0");
            } catch (SQLException e) {
                // Column likely already exists, ignore
            }

            Logger.getInstance().logInfo("Banco de dados inicializado com sucesso.");
        } catch (SQLException e) {
            Logger.getInstance().logError("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace(); // Debugging
        }
    }
}
