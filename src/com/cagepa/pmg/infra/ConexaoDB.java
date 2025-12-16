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
}
