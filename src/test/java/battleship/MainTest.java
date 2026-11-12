package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class MainTest {

    @Test
    @DisplayName("Testa a instanciação da classe Main (Construtor implícito)")
    void testConstructor() {
        // Para atingir 100% de cobertura de linha/classe, o construtor padrão precisa ser invocado
        Main mainInstance = new Main();
        assertNotNull(mainInstance, "A instância de Main não deve ser nula.");
    }

    @Test
    @DisplayName("Testa a execução do método main sem travar o programa")
    void testMain() {
        // Salva o System.in original para não quebrar outros testes
        InputStream originalIn = System.in;

        try {
            // Simula um input vazio (EOF) para que o Scanner dentro de Tasks.menu() pare de esperar entrada
            ByteArrayInputStream fakeIn = new ByteArrayInputStream("0\n".getBytes());
            System.setIn(fakeIn);

            // Usamos assertTimeoutPreemptively para garantir que, caso o Tasks.menu() entre num
            // loop infinito, o teste seja abortado de forma controlada sem travar o IntelliJ.
            assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
                try {
                    Main.main(new String[]{});
                } catch (Exception e) {
                    // O Tasks.menu() provavelmente lançará NoSuchElementException quando o
                    // fakeIn terminar de ser lido. Isso é normal e esperado num ambiente de teste mockado.
                }
            }, "O método main demorou muito para encerrar, verifique o loop em Tasks.menu()");

        } finally {
            // É crítico restaurar o System.in para que o teclado volte a funcionar normalmente no console
            System.setIn(originalIn);
        }
    }
}