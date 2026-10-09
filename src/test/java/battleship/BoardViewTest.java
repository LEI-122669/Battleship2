package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da vista grafica dos tabuleiros ({@link BoardView}).
 *
 * <p>A suite corre com a propriedade {@code java.awt.headless=true} (ver o
 * {@code maven-surefire-plugin} no {@code pom.xml}), pelo que nunca abre janelas reais e pode
 * correr numa maquina de integracao continua. O caminho de codigo que reage a ambientes sem ecra
 * e verificado separadamente, numa JVM propria, por {@link #showIsSafeInHeadlessJvm()}.</p>
 */
class BoardViewTest {

	/**
	 * Confirma que a matriz de marcadores tem a dimensao correta e comeca toda em agua.
	 */
	@Test
	@DisplayName("buildMap devolve um tabuleiro 10x10 de agua quando nao ha frota nem jogadas")
	void buildMapStartsEmpty() {
		char[][] map = Game.buildMap(null, null);

		assertEquals(Game.BOARD_SIZE, map.length);
		for (char[] line : map) {
			assertEquals(Game.BOARD_SIZE, line.length);
			for (char marker : line)
				assertEquals(Game.EMPTY_MARKER, marker);
		}
	}

	/**
	 * Confirma que os navios da frota ficam marcados no tabuleiro.
	 */
	@Test
	@DisplayName("buildMap marca as posicoes ocupadas pelos navios")
	void buildMapMarksShips() {
		IFleet fleet = new Fleet();
		assertTrue(fleet.addShip(Ship.buildShip("barca", Compass.NORTH, new Position(0, 0))));

		char[][] map = Game.buildMap(fleet, null);

		assertEquals(Game.SHIP_MARKER, map[0][0]);
		assertEquals(Game.EMPTY_MARKER, map[9][9]);
	}

	/**
	 * Confirma que os tiros sobrepostos ao tabuleiro distinguem acerto de tiro na agua.
	 */
	@Test
	@DisplayName("buildMap distingue tiro certeiro de tiro na agua")
	void buildMapMarksShots() {
		IFleet fleet = new Fleet();
		assertTrue(fleet.addShip(Ship.buildShip("barca", Compass.NORTH, new Position(0, 0))));

		List<IPosition> shots = new ArrayList<>();
		shots.add(new Position(0, 0));  // acerta no navio
		shots.add(new Position(5, 5));  // cai na agua
		List<IMove> moves = new ArrayList<>();
		moves.add(new Move(1, shots, new ArrayList<>()));

		char[][] map = Game.buildMap(fleet, moves);

		assertEquals(Game.SHOT_SHIP_MARKER, map[0][0]);
		assertEquals(Game.SHOT_WATER_MARKER, map[5][5]);
	}

	/**
	 * Confirma que {@link BoardView#boardAsText} produz exatamente o mesmo que a consola.
	 *
	 * <p>Este e o teste que garante o requisito central da historia de utilizador: a vista
	 * grafica e a vista de consola mostram a mesma informacao.</p>
	 */
	@Test
	@DisplayName("boardAsText reproduz exatamente o tabuleiro impresso na consola")
	void boardAsTextMatchesConsole() {
		IFleet fleet = new Fleet();
		assertTrue(fleet.addShip(Ship.buildShip("barca", Compass.NORTH, new Position(0, 0))));

		List<IPosition> shots = new ArrayList<>();
		shots.add(new Position(0, 0));
		shots.add(new Position(3, 4));
		List<IMove> moves = new ArrayList<>();
		moves.add(new Move(1, shots, new ArrayList<>()));

		String fromView = BoardView.boardAsText(fleet, moves);
		String fromConsole = capturePrintBoard(fleet, moves, true);

		// A consola acrescenta uma linha em branco final e o PrintStream converte os fins de
		// linha; o que tem de coincidir exatamente e o tabuleiro desenhado.
		assertEquals(normalize(fromConsole), normalize(fromView));
	}

	/**
	 * Normaliza fins de linha e espacos em branco nas extremidades.
	 *
	 * @param text o texto a normalizar
	 * @return o texto normalizado
	 */
	private static String normalize(String text) {
		return text.replace(System.lineSeparator(), "\n").strip();
	}

	/**
	 * Confirma que a linha dos navios aparece com a etiqueta classica e o numero de colunas certo.
	 */
	@Test
	@DisplayName("renderBoard inclui as etiquetas A-J e as colunas 1-10")
	void renderBoardHasLabels() {
		String text = Game.renderBoard(Game.buildMap(null, null));

		assertTrue(text.contains("A |"), "falta a etiqueta da linha A");
		assertTrue(text.contains("J |"), "falta a etiqueta da linha J");
		assertTrue(text.contains("1 2 3 4 5 6 7 8 9 10"), "faltam as etiquetas das colunas");

		long rows = text.lines().filter(line -> line.endsWith(" |")).count();
		assertEquals(Game.BOARD_SIZE, rows, "o tabuleiro deve ter 10 linhas");
	}

	/**
	 * Confirma que o desenho para imagem em memoria funciona sem ecra e inclui as duas grelhas.
	 */
	@Test
	@DisplayName("renderToImage produz uma imagem com as dimensoes esperadas e nao nula")
	void renderToImageWorksHeadless() {
		IFleet fleet = new Fleet();
		assertTrue(fleet.addShip(Ship.buildShip("galeao", Compass.EAST, new Position(2, 2))));

		java.awt.image.BufferedImage image = BoardView.renderToImage(fleet, null, null, null);

		assertNotNull(image);
		assertTrue(image.getWidth() > 0);
		assertTrue(image.getHeight() > 0);
		// A imagem tem de ser larga o suficiente para as duas grelhas lado a lado.
		assertTrue(image.getWidth() > 2 * Game.BOARD_SIZE * 40,
				"a imagem devia conter duas grelhas lado a lado, mas tem " + image.getWidth() + " px");
	}

	/**
	 * Confirma que os marcadores publicos mantem os simbolos documentados no guiao.
	 */
	@Test
	@DisplayName("os marcadores publicos mantem os simbolos do guiao")
	void markersAreStable() {
		assertEquals('.', Game.EMPTY_MARKER);
		assertEquals('#', Game.SHIP_MARKER);
		assertEquals('*', Game.SHOT_SHIP_MARKER);
		assertEquals('o', Game.SHOT_WATER_MARKER);
		assertEquals('-', Game.SHIP_ADJACENT_MARKER);
	}

	/**
	 * Confirma que {@link BoardView#show} nao rebenta em ambiente sem ecra.
	 *
	 * <p>Este ambiente pode ter ecra, pelo que estes pedidos devem simplesmente atualizar o
	 * modelo sem lancar nada. O caminho realmente <i>headless</i> e verificado por
	 * {@link #showIsSafeInHeadlessJvm()}.</p>
	 */
	@Test
	@DisplayName("show e refresh nao lancam excecao neste ambiente")
	void showAndRefreshAreSafe() {
		BoardView.show(null, null, null, null);
		IFleet frota = Fleet.createRandom();
		BoardView.show(frota, new ArrayList<>(), Fleet.createRandom(), new ArrayList<>());
		BoardView.refresh();
		BoardView.update(frota, new ArrayList<>(), Fleet.createRandom(), new ArrayList<>());
	}

	/**
	 * Confirma que o interruptor de revelacao da frota inimiga pode ser ligado e desligado.
	 */
	@Test
	@DisplayName("setEnemyFleetRevealed altera o estado da grelha de ataque")
	void enemyFleetRevealCanBeToggled() {
		BoardView.setEnemyFleetRevealed(true);
		assertTrue(BoardView.isEnemyFleetRevealed());

		BoardView.setEnemyFleetRevealed(false);
		assertFalse(BoardView.isEnemyFleetRevealed());

		BoardView.setEnemyFleetRevealed(true);
	}

	/**
	 * Confirma, numa JVM separada com {@code -Djava.awt.headless=true}, que pedir a janela
	 * imprime um aviso e nao lanca excecao.
	 */
	@Test
	@DisplayName("show imprime aviso e nao lanca excecao numa JVM headless")
	void showIsSafeInHeadlessJvm() throws Exception {
		String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
		Path output = Path.of("target", "headless-probe.out");

		Process processo = new ProcessBuilder(java,
				"-Djava.awt.headless=true",
				"-Dfile.encoding=UTF-8",
				"-cp", System.getProperty("java.class.path"),
				"battleship.HeadlessBoardProbe")
				.redirectErrorStream(true)
				.redirectOutput(output.toFile())
				.start();

		assertTrue(processo.waitFor(120, TimeUnit.SECONDS), "a sonda headless excedeu o tempo limite");

		// As mensagens da sonda sao so em ASCII, mas a codificacao do ficheiro depende da JVM
		// filha; usar a codificacao predefinida da plataforma evita decodificacoes falhadas.
		String saida = new String(Files.readAllBytes(output), Charset.defaultCharset());
		assertEquals(0, processo.exitValue(), "a sonda headless falhou:\n" + saida);
		assertTrue(saida.contains("HEADLESS_OK"), "a sonda headless nao chegou ao fim:\n" + saida);
		assertTrue(saida.contains("headless") || saida.contains("sem ecra"),
				"falta a mensagem de aviso em ambiente sem ecra:\n" + saida);
	}

	/**
	 * Executa {@code Game.printBoard} e devolve o que seria escrito na consola.
	 *
	 * @param fleet      a frota a desenhar
	 * @param moves      as jogadas a sobrepor
	 * @param showShots  se os tiros devem ser mostrados
	 * @return o texto produzido na consola
	 */
	private static String capturePrintBoard(IFleet fleet, List<IMove> moves, boolean showShots) {
		PrintStream original = System.out;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
			Game.printBoard(fleet, moves, showShots, false);
		} finally {
			System.setOut(original);
		}
		return captured.toString(StandardCharsets.UTF_8);
	}

	/**
	 * Confirma que o tipo devolvido por {@code buildMap} e mesmo uma matriz de caracteres.
	 */
	@Test
	@DisplayName("buildMap devolve char[][]")
	void buildMapReturnsCharArray() {
		assertInstanceOf(char[][].class, Game.buildMap(null, null));
	}
}
