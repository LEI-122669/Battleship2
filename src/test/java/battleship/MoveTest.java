package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {


    /**
     * 创建一个模拟的 IShip 对象。
     * Move 类在 processEnemyFire 中主要依赖 getCategory() 来统计不同船只的击中和击沉数量。
     */
    private IShip createMockShip(String category) {
        if (category == null) return null;

        return new IShip() {
            @Override public String getCategory() { return category; }
            @Override public Integer getSize() { return 0; }
            @Override public List<IPosition> getPositions() { return Collections.emptyList(); }
            @Override public List<IPosition> getAdjacentPositions() { return Collections.emptyList(); }
            @Override public IPosition getPosition() { return null; }
            @Override public Compass getBearing() { return null; }
            @Override public boolean stillFloating() { return true; } // 默认浮动
            @Override public int getTopMostPos() { return 0; }
            @Override public int getBottomMostPos() { return 0; }
            @Override public int getLeftMostPos() { return 0; }
            @Override public int getRightMostPos() { return 0; }
            @Override public boolean occupies(IPosition pos) { return false; }
            @Override public boolean tooCloseTo(IShip other) { return false; }
            @Override public boolean tooCloseTo(IPosition pos) { return false; }
            @Override public boolean isInsideBoard() { return true; }
            @Override public void shoot(IPosition pos) {}
            @Override public void sink() {}
            @Override
            public void setPosition(IPosition position) {}
            @Override
            public int getHits() { return 0; }
            @Override
            public void hit() {}
        };
    }

    // ---------------------------------------------------------
    // 单元测试方法
    // ---------------------------------------------------------

    @Test
    @DisplayName("Testa a inicialização e os getters da classe Move")
    void testConstructorAndGetters() {
        List<IPosition> shots = Collections.emptyList();
        List<IGame.ShotResult> results = Collections.emptyList();
        IMove move = new Move(1, shots, results);

        assertEquals(1, move.getNumber(), "O número da jogada deve corresponder ao inicializado.");
        assertEquals(shots, move.getShots(), "A lista de tiros (IPosition) deve corresponder.");
        assertEquals(results, move.getShotResults(), "A lista de resultados de tiro deve corresponder.");
    }

    @Test
    @DisplayName("Testa a formatação padrão do método toString")
    void testToString() {
        IMove move = new Move(5, Collections.emptyList(), Collections.emptyList());
        String expected = "Move{number=5, shots=0, results=0}";
        assertEquals(expected, move.toString(), "O método toString deve formatar corretamente as propriedades do Move.");
    }

    @Test
    @DisplayName("Cobre a lógica de tiros inválidos (que são ignorados) e tiros fora do tabuleiro")
    void testProcessEnemyFire_InvalidAndOutside() {
        // Um tiro marcado como "não válido" (valid = false) e sem repetição
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, null, false)
        );
        IMove move = new Move(1, Collections.emptyList(), results);

        // verbose = true cobre também a verificação de output para outsideShots
        String json = move.processEnemyFire(true);

        assertNotNull(json);
        // Considerando Game.NUMBER_SHOTS = 3 (na tua classe Game), 3 - 0 (valid) - 0 (repeated) = 3 outside
        assertTrue(json.contains("\"outsideShots\" : 3"));
    }

    @Test
    @DisplayName("Cobre a ramificação de apenas tiros repetidos (0 tiros válidos)")
    void testProcessEnemyFire_OnlyRepeated() {
        // Dois tiros válidos em termos de mapa, mas repetidos
        List<IGame.ShotResult> results = Arrays.asList(
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        );
        IMove move = new Move(2, Collections.emptyList(), results);
        String json = move.processEnemyFire(true);

        // Cobre: if (validShots == 0 && repeatedShots > 0)
        assertTrue(json.contains("\"repeatedShots\" : 2"));
    }

    @Test
    @DisplayName("Cobre acertos em navio, tiros na água e tiros repetidos em conjunto")
    void testProcessEnemyFire_HitMissAndRepeated() {
        List<IGame.ShotResult> results = Arrays.asList(
                new IGame.ShotResult(true, false, createMockShip("Submarino"), false), // Acerto sem afundar
                new IGame.ShotResult(true, false, null, false),                        // Miss (Tiro na água)
                new IGame.ShotResult(true, true, null, false)                          // Tiro repetido
        );
        IMove move = new Move(3, Collections.emptyList(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 2"));
        assertTrue(json.contains("\"missedShots\" : 1"));
        assertTrue(json.contains("\"repeatedShots\" : 1"));
        assertTrue(json.contains("\"hits\" : 1")); // Confirma o acerto computado no Submarino
    }

    @Test
    @DisplayName("Cobre barcos afundados (testando pluralização e formatação da string)")
    void testProcessEnemyFire_SunkBoatsAndStringTrimming() {
        List<IGame.ShotResult> results = Arrays.asList(
                new IGame.ShotResult(true, false, createMockShip("Fragata"), true),  // Sunk = true
                new IGame.ShotResult(true, false, createMockShip("Fragata"), true),  // Sunk = true (Aciona plural)
                new IGame.ShotResult(true, false, createMockShip("Cruzador"), false) // Acerto simples
        );
        IMove move = new Move(4, Collections.emptyList(), results);

        // verbose = true testa a formatação e a lógica de limpeza do final da string (output.setLength)
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"type\" : \"Fragata\""));
        assertTrue(json.contains("\"count\" : 2"));
        assertTrue(json.contains("\"type\" : \"Cruzador\""));
        assertTrue(json.contains("\"hits\" : 1"));
    }
    @Test
    @DisplayName("Cobre formatação singular (1 válido, 1 na água, 1 fora, 1 repetido)")
    void testProcessEnemyFire_SingularFormat() {
        // Para testar a ausência do "s" (singular) no StringBuilder.
        // Se valid = 1 (miss), repeated = 1 => outside = Game.NUMBER_SHOTS(3) - 2 = 1.
        List<IGame.ShotResult> results = Arrays.asList(
                new IGame.ShotResult(true, false, null, false), // 1 missed (valid)
                new IGame.ShotResult(true, true, null, false)   // 1 repeated
        );
        IMove move = new Move(10, Collections.emptyList(), results);

        // Imprime: 1 tiro válido: 1 tiro na água, 1 tiro repetido, 1 tiro exterior
        move.processEnemyFire(true);
    }

    @Test
    @DisplayName("Cobre formatação plural para acertos (hits) e tiros na água (misses)")
    void testProcessEnemyFire_PluralFormat() {
        // Para testar a adição do "s" (plural) em hits e misses.
        List<IGame.ShotResult> results = Arrays.asList(
                new IGame.ShotResult(true, false, createMockShip("Submarino"), false), // hit 1
                new IGame.ShotResult(true, false, createMockShip("Submarino"), false), // hit 2 (>1 hits)
                new IGame.ShotResult(true, false, null, false), // miss 1
                new IGame.ShotResult(true, false, null, false)  // miss 2 (>1 misses)
        );
        Move move = new Move(11, Collections.emptyList(), results);

        move.processEnemyFire(true);
    }
    @Test
    @DisplayName("Cobre o caso em que verbose é false (ignora a construção de output)")
    void testProcessEnemyFire_VerboseFalse() {

        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, null, false)
        );
        IMove move = new Move(20, Collections.emptyList(), results);

        // Passar false garante que cobrimos a ramificação onde o if (verbose) falha
        String json = move.processEnemyFire(false);
        assertNotNull(json);
    }

    @Test
    @DisplayName("Cobre o uso da vírgula antes dos tiros exteriores (output não está vazio)")
    void testProcessEnemyFire_OutsideShotsWithComma() {
        // 确保 output 不为空（包含 1 个有效射击）并且存在界外射击（总射击数不足 3）
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, null, false) // 1 tiro válido na água
        );
        IMove move = new Move(21, Collections.emptyList(), results);

        // 运行方法，触发 output.append(", ") 的分支
        String json = move.processEnemyFire(true);

        // 检查返回的 JSON 字符串中是否正确包含了 outsideShots 字段
        assertTrue(json.contains("\"outsideShots\""));
    }
}