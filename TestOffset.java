import com.cagepa.pmg.sgu.SGU;
import com.cagepa.pmg.sgu.Usuario;
import com.cagepa.pmg.sgu.Hidrometro;
import com.cagepa.pmg.sgu.TipoUsuario;
import com.cagepa.pmg.infra.ConexaoDB;

public class TestOffset {
    public static void main(String[] args) {
        System.out.println("=== Testando Lógica de Offset ===");

        // Re-initialize DB to apply schema changes (if using in-memory or fresh file)
        // Note: ConexaoDB.inicializarBanco() creates tables if not exists.
        // Since we added a column, we might need to manually handle migration or rely
        // on the fact that we changed the CREATE statement.
        // For this test, let's assume we can just run SGU logic. If it fails due to
        // missing column, we know we need to recreate DB.

        SGU sgu = new SGU();

        // Create User and Hydrometer
        String userId = "test_user_offset";
        String hidroId = "H_OFFSET_1";

        sgu.deletarUsuario(userId); // Cleanup
        sgu.cadastrarUsuario(userId, "Test User", "123", TipoUsuario.PADRAO);
        sgu.adicionarHidrometro(userId, hidroId, "A");

        System.out.println("\n1. Leitura Inicial (100.0)");
        sgu.atualizarConsumo(hidroId, 100.0);
        printConsumo(sgu, hidroId); // Should be 100.0

        System.out.println("\n2. Leitura Incremental (150.0)");
        sgu.atualizarConsumo(hidroId, 150.0);
        printConsumo(sgu, hidroId); // Should be 150.0

        System.out.println("\n3. Reset do Simulador (Volta para 10.0)");
        // Logic: New (10.0) < Old (150.0) -> Offset += 150.0 -> Offset = 150.0
        // Total = 10.0 + 150.0 = 160.0
        sgu.atualizarConsumo(hidroId, 10.0);
        printConsumo(sgu, hidroId); // Should be 160.0

        System.out.println("\n4. Leitura pós-reset (20.0)");
        // Logic: New (20.0) > Old (10.0) -> Normal update
        // Total = 20.0 + 150.0 = 170.0
        sgu.atualizarConsumo(hidroId, 20.0);
        printConsumo(sgu, hidroId); // Should be 170.0

        System.out.println("\nTeste Concluído.");
    }

    private static void printConsumo(SGU sgu, String hidroId) {
        Usuario u = sgu.getUsuarioPorHidrometro(hidroId);
        for (Hidrometro h : u.getHidrometros()) {
            if (h.getId().equals(hidroId)) {
                System.out.println("   Leitura Atual: " + h.getConsumoAtual());
                System.out.println("   Offset: " + h.getOffset());
                System.out.println("   Consumo Total (Exibido): " + h.getConsumoTotal());
            }
        }
    }
}
