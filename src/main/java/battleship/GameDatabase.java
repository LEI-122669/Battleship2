package battleship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Armazena os jogos e as jogadas numa base de dados H2 (acedida por JDBC).
 * <p>
 * Se a base de dados falhar, o erro é registado no log e o jogo continua.
 */
public class GameDatabase {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Base de dados em ficheiro, na pasta data/ do projeto. */
    public static final String DEFAULT_URL = "jdbc:h2:./data/battleship";

    private final String url;

    /**
     * Cria a ligação à base de dados por omissão (ficheiro data/battleship.mv.db).
     */
    public GameDatabase() {
        this(DEFAULT_URL);
    }

    /**
     * Cria a ligação a uma base de dados específica (útil nos testes,
     * por exemplo "jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1").
     *
     * @param url o URL JDBC da base de dados
     */
    public GameDatabase(String url) {
        this.url = url;
        createTables();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, "sa", "");
    }

    /**
     * Cria as tabelas GAMES e SHOTS, caso ainda não existam.
     */
    private void createTables() {
        String games = "CREATE TABLE IF NOT EXISTS GAMES ("
                + "ID VARCHAR(36) PRIMARY KEY, "
                + "PLAYER_NAME VARCHAR(100) NOT NULL, "
                + "STARTED_AT TIMESTAMP NOT NULL, "
                + "FINISHED_AT TIMESTAMP, "
                + "WINNER VARCHAR(50), "
                + "HITS INT, SUNK_SHIPS INT, REPEATED_SHOTS INT, INVALID_SHOTS INT)";
        String shots = "CREATE TABLE IF NOT EXISTS SHOTS ("
                + "ID BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "GAME_ID VARCHAR(36) NOT NULL, "
                + "MOVE_NUMBER INT NOT NULL, "
                + "SHOT_INDEX INT NOT NULL, "
                + "SHOT_ROW CHAR(1) NOT NULL, "
                + "SHOT_COLUMN INT NOT NULL, "
                + "OUTCOME VARCHAR(10) NOT NULL, "
                + "CREATED_AT TIMESTAMP NOT NULL, "
                + "FOREIGN KEY (GAME_ID) REFERENCES GAMES(ID))";
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.execute(games);
            st.execute(shots);
        } catch (SQLException e) {
            LOGGER.error("Erro ao criar as tabelas: {}", e.getMessage());
        }
    }

    /**
     * Regista um novo jogo (US2).
     *
     * @param gameId     identificador do jogo
     * @param playerName nome do jogador
     * @return true se o jogo foi gravado
     */
    public boolean saveGame(String gameId, String playerName) {
        String sql = "INSERT INTO GAMES (ID, PLAYER_NAME, STARTED_AT) VALUES (?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, gameId);
            ps.setString(2, playerName);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Erro ao gravar o jogo {}: {}", gameId, e.getMessage());
            return false;
        }
    }

    /**
     * Grava os tiros de uma jogada (US1).
     *
     * @param gameId     identificador do jogo
     * @param moveNumber número da jogada
     * @param shots      posições dos tiros
     * @param results    resultado de cada tiro (mesma ordem que shots)
     * @return true se a jogada foi gravada
     */
    public boolean saveMove(String gameId, int moveNumber, List<IPosition> shots, List<IGame.ShotResult> results) {
        String sql = "INSERT INTO SHOTS (GAME_ID, MOVE_NUMBER, SHOT_INDEX, SHOT_ROW, SHOT_COLUMN, OUTCOME, CREATED_AT) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < shots.size(); i++) {
                IPosition pos = shots.get(i);
                ps.setString(1, gameId);
                ps.setInt(2, moveNumber);
                ps.setInt(3, i + 1);
                ps.setString(4, String.valueOf(pos.getClassicRow()));
                ps.setInt(5, pos.getClassicColumn());
                ps.setString(6, outcomeOf(i < results.size() ? results.get(i) : null));
                ps.setTimestamp(7, now);
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Erro ao gravar a jogada {} do jogo {}: {}", moveNumber, gameId, e.getMessage());
            return false;
        }
    }

    /**
     * Regista o fim do jogo, com o vencedor e as estatísticas finais (US2).
     *
     * @param gameId identificador do jogo
     * @param winner quem ganhou
     * @param game   o jogo, de onde se leem as estatísticas
     * @return true se foi gravado
     */
    public boolean finishGame(String gameId, String winner, IGame game) {
        String sql = "UPDATE GAMES SET FINISHED_AT = ?, WINNER = ?, HITS = ?, SUNK_SHIPS = ?, "
                + "REPEATED_SHOTS = ?, INVALID_SHOTS = ? WHERE ID = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setString(2, winner);
            ps.setInt(3, game.getHits());
            ps.setInt(4, game.getSunkShips());
            ps.setInt(5, game.getRepeatedShots());
            ps.setInt(6, game.getInvalidShots());
            ps.setString(7, gameId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.error("Erro ao terminar o jogo {}: {}", gameId, e.getMessage());
            return false;
        }
    }

    /**
     * Indica se existe um jogo com este identificador.
     *
     * @param gameId identificador do jogo
     * @return true se existir
     */
    public boolean gameExists(String gameId) {
        String sql = "SELECT 1 FROM GAMES WHERE ID = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao procurar o jogo {}: {}", gameId, e.getMessage());
            return false;
        }
    }

    /**
     * Devolve os tiros de um jogo, por ordem, no formato
     * "Jogada 1 - tiro 1: A3 -> HIT" (US3).
     *
     * @param gameId identificador do jogo
     * @return lista de linhas (vazia se o jogo não tiver jogadas)
     */
    public List<String> findMoves(String gameId) {
        List<String> lines = new ArrayList<>();
        String sql = "SELECT MOVE_NUMBER, SHOT_INDEX, SHOT_ROW, SHOT_COLUMN, OUTCOME FROM SHOTS "
                + "WHERE GAME_ID = ? ORDER BY MOVE_NUMBER, SHOT_INDEX";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add("Jogada " + rs.getInt(1) + " - tiro " + rs.getInt(2) + ": "
                            + rs.getString(3) + rs.getInt(4) + " -> " + rs.getString(5));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao ler as jogadas do jogo {}: {}", gameId, e.getMessage());
        }
        return lines;
    }

    /**
     * Converte o resultado de um tiro no texto guardado na base de dados.
     *
     * @param r resultado do tiro
     * @return INVALID, REPEATED, MISS, HIT ou SUNK
     */
    static String outcomeOf(IGame.ShotResult r) {
        if (r == null || !r.valid())
            return "INVALID";
        if (r.repeated())
            return "REPEATED";
        if (r.ship() == null)
            return "MISS";
        return r.sunk() ? "SUNK" : "HIT";
    }
}
