package battleship;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários da classe MoveTimer.
 */
@DisplayName("MoveTimer")
class MoveTimerTest {

    private MoveTimer timer;

    @BeforeEach
    void setUp() {
        timer = new MoveTimer();
    }

    @Nested
    @DisplayName("start / stop")
    class StartStop {

        @Test
        @DisplayName("stop() sem start() lança IllegalStateException")
        void stopWithoutStart() {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> timer.stop());
            assertEquals("No move is currently being timed.", e.getMessage());
        }

        @Test
        @DisplayName("start() seguido de stop() devolve o tempo decorrido (>= tempo de espera)")
        void startThenStop() throws InterruptedException {
            timer.start();
            Thread.sleep(20);
            long elapsed = timer.stop();
            assertTrue(elapsed >= 20, "tempo medido: " + elapsed);
        }

        @Test
        @DisplayName("stop() chamado duas vezes seguidas lança IllegalStateException na segunda")
        void stopTwice() {
            timer.start();
            timer.stop();
            assertThrows(IllegalStateException.class, () -> timer.stop());
        }

        @Test
        @DisplayName("Depois de parar, start() permite medir uma nova jogada")
        void restartAfterStop() {
            timer.start();
            timer.stop();
            timer.start();
            assertTrue(timer.stop() >= 0);
        }
    }

    @Nested
    @DisplayName("formatTime")
    class FormatTime {

        private Locale previous;

        @BeforeEach
        void fixLocale() {
            // String.format usa o Locale por omissão (em pt-PT o separador decimal é vírgula)
            previous = Locale.getDefault();
            Locale.setDefault(Locale.US);
        }

        @AfterEach
        void restoreLocale() {
            Locale.setDefault(previous);
        }

        @Test
        @DisplayName("0 ms -> \"0.00 seconds\"")
        void zero() {
            assertEquals("0.00 seconds", MoveTimer.formatTime(0));
        }

        @Test
        @DisplayName("1500 ms -> \"1.50 seconds\"")
        void oneAndHalf() {
            assertEquals("1.50 seconds", MoveTimer.formatTime(1500));
        }

        @Test
        @DisplayName("Arredonda a duas casas decimais (1234 ms -> \"1.23 seconds\")")
        void rounding() {
            assertEquals("1.23 seconds", MoveTimer.formatTime(1234));
        }

        @Test
        @DisplayName("Usa o Locale por omissão (pt-PT -> vírgula decimal)")
        void portugueseLocale() {
            Locale.setDefault(Locale.forLanguageTag("pt-PT"));
            assertEquals("2,50 seconds", MoveTimer.formatTime(2500));
        }
    }
}
