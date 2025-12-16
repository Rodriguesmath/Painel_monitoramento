import com.cagepa.pmg.infra.ConexaoDB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class InspectDB {
    public static void main(String[] args) {
        System.out.println("=== Inspeção da Tabela HIDROMETROS ===");
        try (Connection conn = ConexaoDB.conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM hidrometros")) {

            System.out.printf("%-15s %-15s %-10s %-10s %-10s%n", "ID", "ID_USUARIO", "MODELO", "CONSUMO", "OFFSET");
            System.out.println("----------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-15s %-15s %-10s %-10.2f %-10.2f%n",
                        rs.getString("id"),
                        rs.getString("id_usuario"),
                        rs.getString("modelo"),
                        rs.getDouble("consumo_atual"),
                        rs.getDouble("offset"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
