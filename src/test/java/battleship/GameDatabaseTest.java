package battleship;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários da classe GameDatabase.
 * <p>
 * Cada teste usa uma base de dados H2 em memória com nome aleatório,
 * para não mexer no ficheiro data/battleship e para os testes serem independentes.
 */
@DisplayName("GameDatabase")
class GameDatabaseTest {

    /** URL que faz o DriverManager lançar SQLException ("No suitable driver"). */
    private static final String INVALID_URL = "jdbc:naoexiste:teste";

    private String url;
    private GameDatabase db;

    private static class FixedStatsGame extends Game {
        FixedStatsGame() {
            super(new Fleet());
        }

        @Override
        public int getHits() {
            return 7;
        }

        @Override
        public int getSunkShips() {
            return 3;
        }

        @Override
        public int getRepeatedShots() {
            return 2;
        }

        @Override
        public int getInvalidShots() {
            return 1;
        }
    }

    @BeforeEach
    void setUp() {
        url = "jdbc:h2:mem:test" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        db = new GameDatabase(url);
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(url, "sa", "");
    }

    @Nested
    @DisplayName("Construtores")
    class Constructors {

        @Test
        @DisplayName("Construtor com URL cria as tabelas GAMES e SHOTS")
        void constructorCreatesTables() throws SQLException {
            try (Connection c = open();
                 ResultSet rs = c.getMetaData().getTables(null, "PUBLIC", "%", null)) {
                List<String> names = new ArrayList<>();
                while (rs.next()) {
                    names.add(rs.getString("TABLE_NAME"));
                }
                assertTrue(names.contains("GAMES"));
                assertTrue(names.contains("SHOTS"));
            }
        }

        @Test
        @DisplayName("Criar duas vezes sobre a mesma base de dados não falha (CREATE TABLE IF NOT EXISTS)")
        void constructorIsIdempotent() {
            assertDoesNotThrow(() -> new GameDatabase(url));
            assertTrue(new GameDatabase(url).saveGame("g1", "Ana"));
        }

        @Test
        @DisplayName("Construtor por omissão usa DEFAULT_URL e funciona")
        void defaultConstructor() {
            assertEquals("jdbc:h2:./data/battleship", GameDatabase.DEFAULT_URL);
            GameDatabase defaultDb = assertDoesNotThrow(() -> new GameDatabase());
            assertFalse(defaultDb.gameExists("id-que-nao-existe-" + UUID.randomUUID()));
        }

        @Test
        @DisplayName("Construtor com URL inválido não lança exceção (erro só vai para o log)")
        void constructorWithInvalidUrl() {
            assertDoesNotThrow(() -> new GameDatabase(INVALID_URL));
        }
    }

    @Nested
    @DisplayName("saveGame")
    class SaveGame {

        @Test
        @DisplayName("Grava um jogo novo e devolve true")
        void savesNewGame() throws SQLException {
            assertTrue(db.saveGame("g1", "Ana"));
            try (Connection c = open();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT PLAYER_NAME, STARTED_AT, FINISHED_AT FROM GAMES WHERE ID = ?")) {
                ps.setString(1, "g1");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("Ana", rs.getString(1));
                    assertNotNull(rs.getTimestamp(2));
                    assertNull(rs.getTimestamp(3));
                }
            }
        }

        @Test
        @DisplayName("Gravar o mesmo id duas vezes devolve false na segunda (chave primária)")
        void duplicateIdFails() {
            assertTrue(db.saveGame("g1", "Ana"));
            assertFalse(db.saveGame("g1", "Rui"));
        }

        @Test
        @DisplayName("Nome de jogador null devolve false (PLAYER_NAME NOT NULL)")
        void nullPlayerNameFails() {
            assertFalse(db.saveGame("g1", null));
        }

        @Test
        @DisplayName("Com URL inválido devolve false")
        void invalidUrlFails() {
            assertFalse(new GameDatabase(INVALID_URL).saveGame("g1", "Ana"));
        }
    }

    @Nested
    @DisplayName("saveMove")
    class SaveMove {

        @BeforeEach
        void createGame() {
            db.saveGame("g1", "Ana");
        }

        @Test
        @DisplayName("Grava todos os tiros de uma jogada, pela ordem, com o resultado de cada um")
        void savesAllShots() {
            IShip barge = new Barge(Compass.NORTH, new Position(0, 0));
            List<IPosition> shots = List.of(new Position('A', 1), new Position('B', 2), new Position('C', 3));
            List<IGame.ShotResult> results = List.of(
                    new IGame.ShotResult(true, false, barge, true),
                    new IGame.ShotResult(true, false, null, false),
                    new IGame.ShotResult(true, true, null, false));

            assertTrue(db.saveMove("g1", 1, shots, results));
            assertEquals(List.of(
                    "Jogada 1 - tiro 1: A1 -> SUNK",
                    "Jogada 1 - tiro 2: B2 -> MISS",
                    "Jogada 1 - tiro 3: C3 -> REPEATED"), db.findMoves("g1"));
        }

        @Test
        @DisplayName("Se houver menos resultados do que tiros, os tiros sem resultado ficam INVALID")
        void missingResultsAreInvalid() {
            List<IPosition> shots = List.of(new Position('A', 1), new Position('B', 2));
            List<IGame.ShotResult> results = List.of(new IGame.ShotResult(true, false, null, false));

            assertTrue(db.saveMove("g1", 1, shots, results));
            assertEquals(List.of(
                    "Jogada 1 - tiro 1: A1 -> MISS",
                    "Jogada 1 - tiro 2: B2 -> INVALID"), db.findMoves("g1"));
        }

        @Test
        @DisplayName("Lista de tiros vazia devolve true e não grava nada")
        void emptyShots() {
            assertTrue(db.saveMove("g1", 1, Collections.emptyList(), Collections.emptyList()));
            assertTrue(db.findMoves("g1").isEmpty());
        }

        @Test
        @DisplayName("Jogada de um jogo que não existe devolve false (chave estrangeira)")
        void unknownGameFails() {
            List<IPosition> shots = List.of(new Position('A', 1));
            List<IGame.ShotResult> results = List.of(new IGame.ShotResult(true, false, null, false));
            assertFalse(db.saveMove("nao-existe", 1, shots, results));
        }

        @Test
        @DisplayName("Com URL inválido devolve false")
        void invalidUrlFails() {
            GameDatabase bad = new GameDatabase(INVALID_URL);
            assertFalse(bad.saveMove("g1", 1, List.of(new Position('A', 1)), List.of()));
        }
    }

    @Nested
    @DisplayName("finishGame")
    class FinishGame {

        @Test
        @DisplayName("Grava o vencedor e as estatísticas do jogo e devolve true")
        void savesWinnerAndStats() throws SQLException {
            db.saveGame("g1", "Ana");
            assertTrue(db.finishGame("g1", "Ana", new FixedStatsGame()));

            try (Connection c = open();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT FINISHED_AT, WINNER, HITS, SUNK_SHIPS, REPEATED_SHOTS, INVALID_SHOTS "
                                 + "FROM GAMES WHERE ID = ?")) {
                ps.setString(1, "g1");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertNotNull(rs.getTimestamp(1));
                    assertEquals("Ana", rs.getString(2));
                    assertEquals(7, rs.getInt(3));
                    assertEquals(3, rs.getInt(4));
                    assertEquals(2, rs.getInt(5));
                    assertEquals(1, rs.getInt(6));
                }
            }
        }

        @Test
        @DisplayName("Terminar um jogo que não existe devolve false (0 linhas atualizadas)")
        void unknownGameReturnsFalse() {
            assertFalse(db.finishGame("nao-existe", "Ana", new Game(new Fleet())));
        }

        @Test
        @DisplayName("Com URL inválido devolve false")
        void invalidUrlFails() {
            assertFalse(new GameDatabase(INVALID_URL).finishGame("g1", "Ana", new Game(new Fleet())));
        }
    }

    @Nested
    @DisplayName("gameExists")
    class GameExists {

        @Test
        @DisplayName("Devolve true para um jogo gravado")
        void existingGame() {
            db.saveGame("g1", "Ana");
            assertTrue(db.gameExists("g1"));
        }

        @Test
        @DisplayName("Devolve false para um jogo que não existe")
        void missingGame() {
            assertFalse(db.gameExists("nao-existe"));
        }

        @Test
        @DisplayName("Com URL inválido devolve false")
        void invalidUrl() {
            assertFalse(new GameDatabase(INVALID_URL).gameExists("g1"));
        }
    }

    @Nested
    @DisplayName("findMoves")
    class FindMoves {

        @Test
        @DisplayName("Jogo sem jogadas devolve lista vazia")
        void noMoves() {
            db.saveGame("g1", "Ana");
            assertTrue(db.findMoves("g1").isEmpty());
        }

        @Test
        @DisplayName("Devolve as jogadas ordenadas por jogada e por tiro, mesmo gravadas fora de ordem")
        void orderedByMoveAndShot() {
            db.saveGame("g1", "Ana");
            IGame.ShotResult miss = new IGame.ShotResult(true, false, null, false);
            db.saveMove("g1", 2, List.of(new Position('J', 10)), List.of(miss));
            db.saveMove("g1", 1, List.of(new Position('A', 1), new Position('A', 2)), List.of(miss, miss));

            assertEquals(List.of(
                    "Jogada 1 - tiro 1: A1 -> MISS",
                    "Jogada 1 - tiro 2: A2 -> MISS",
                    "Jogada 2 - tiro 1: J10 -> MISS"), db.findMoves("g1"));
        }

        @Test
        @DisplayName("Só devolve as jogadas do jogo pedido")
        void onlyRequestedGame() {
            IGame.ShotResult miss = new IGame.ShotResult(true, false, null, false);
            db.saveGame("g1", "Ana");
            db.saveGame("g2", "Rui");
            db.saveMove("g1", 1, List.of(new Position('A', 1)), List.of(miss));
            db.saveMove("g2", 1, List.of(new Position('B', 2)), List.of(miss));

            assertEquals(List.of("Jogada 1 - tiro 1: A1 -> MISS"), db.findMoves("g1"));
        }

        @Test
        @DisplayName("Com URL inválido devolve lista vazia")
        void invalidUrl() {
            assertTrue(new GameDatabase(INVALID_URL).findMoves("g1").isEmpty());
        }
    }

    @Nested
    @DisplayName("outcomeOf")
    class OutcomeOf {

        private final IShip ship = new Barge(Compass.NORTH, new Position(0, 0));

        @Test
        @DisplayName("null -> INVALID")
        void nullIsInvalid() {
            assertEquals("INVALID", GameDatabase.outcomeOf(null));
        }

        @Test
        @DisplayName("Tiro inválido -> INVALID")
        void invalid() {
            assertEquals("INVALID", GameDatabase.outcomeOf(new IGame.ShotResult(false, false, null, false)));
        }

        @Test
        @DisplayName("Tiro repetido -> REPEATED")
        void repeated() {
            assertEquals("REPEATED", GameDatabase.outcomeOf(new IGame.ShotResult(true, true, null, false)));
        }

        @Test
        @DisplayName("Tiro na água -> MISS")
        void miss() {
            assertEquals("MISS", GameDatabase.outcomeOf(new IGame.ShotResult(true, false, null, false)));
        }

        @Test
        @DisplayName("Tiro num navio sem o afundar -> HIT")
        void hit() {
            assertEquals("HIT", GameDatabase.outcomeOf(new IGame.ShotResult(true, false, ship, false)));
        }

        @Test
        @DisplayName("Tiro que afunda um navio -> SUNK")
        void sunk() {
            assertEquals("SUNK", GameDatabase.outcomeOf(new IGame.ShotResult(true, false, ship, true)));
        }
    }
}