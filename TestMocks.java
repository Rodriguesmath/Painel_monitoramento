import com.cagepa.pmg.san.NotificadorEmail;
import com.cagepa.pmg.san.NotificadorSMS;
import com.cagepa.pmg.sgr.RelatorioCSV;
import com.cagepa.pmg.sgr.RelatorioPDF;

public class TestMocks {
    public static void main(String[] args) {
        System.out.println("=== Testando Mocks ===");

        System.out.println("\n1. Notificadores:");
        new NotificadorEmail().enviar("Teste de Email");
        new NotificadorSMS().enviar("Teste de SMS");

        System.out.println("\n2. Relatórios:");
        new RelatorioCSV().gerarRelatorio("mock_user");
        new RelatorioPDF().gerarRelatorio("mock_user");

        System.out.println("\nTeste Concluído.");
    }
}
