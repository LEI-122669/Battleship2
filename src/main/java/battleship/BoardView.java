package battleship;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Graphical (Swing) view of the two game boards.
 *
 * <p>Ponto B do guião da Ficha n.º 2: <i>visualização gráfica dos tabuleiros</i>. A janela
 * apresenta os dois tabuleiros do jogo, tal como na folha de registo em papel:</p>
 *
 * <ul>
 *   <li><b>Grelha de Defesa</b> (esquerda) — a nossa frota e os tiros que o adversário disparou
 *       contra ela. O que aparece é exatamente o que {@code mapa} e {@code tiros} mostram na
 *       consola.</li>
 *   <li><b>Grelha de Ataque</b> (direita) — a nossa folha de registo dos tiros que disparámos.
 *       Mostra as jogadas que fizemos e, quando {@link #isEnemyFleetRevealed()} está ativo,
 *       também a posição real dos navios adversários.</li>
 * </ul>
 *
 * <p>A marcação de cada grelha é obtida a partir de {@link Game#buildMap}, pelo que não existe
 * duplicação da lógica do jogo: a vista gráfica e a vista de consola mostram sempre a mesma
 * informação.</p>
 *
 * <p><b>Restrições respeitadas:</b></p>
 * <ul>
 *   <li>Usa apenas {@code javax.swing} (biblioteca gráfica do próprio JDK). O FlatLaf
 *       ({@code com.formdev:flatlaf}) é a biblioteca obtida no Maven Central.</li>
 *   <li>Único ponto de entrada: {@link #show(IFleet, List, IFleet, List)}. Nenhuma outra classe
 *       instancia componentes Swing.</li>
 *   <li>Guarda de ambiente sem ecrã: se {@link GraphicsEnvironment#isHeadless()} for verdadeiro,
 *       é impressa uma mensagem de aviso e nada é lançado.</li>
 *   <li>A janela vive numa <i>thread</i> própria, pelo que o ciclo de comandos da consola
 *       continua a aceitar input normalmente.</li>
 *   <li>O desenho pode ser feito para uma imagem em memória ({@link #renderToImage}), o que
 *       permite capturar os tabuleiros sem ecrã (material para o vídeo do ponto B11).</li>
 * </ul>
 *
 * @author LEI-122669
 */
public final class BoardView {

	// ------------------------------------------------------------------
	// Dimensões e aspeto

	/** Lado de cada célula do tabuleiro, em pixéis. */
	private static final int CELL = 48;

	/** Largura da faixa onde são desenhadas as etiquetas de linha (A..J). */
	private static final int LABEL_W = 30;

	/** Altura da faixa onde são desenhadas as etiquetas de coluna (1..10). */
	private static final int LABEL_H = 26;

	/** Altura da faixa com o título de cada grelha. */
	private static final int CARD_TITLE_H = 30;

	/** Altura da faixa inferior com as estatísticas de cada grelha. */
	private static final int CARD_FOOTER_H = 24;

	/** Espaço horizontal entre as duas grelhas, em pixéis. */
	private static final int GAP_BOARDS = 24;

	/** Altura da faixa com o título geral da janela. */
	private static final int WINDOW_TITLE_H = 34;

	/** Folga horizontal de cada grelha em relação à margem do painel. */
	private static final int CARD_PAD = 12;

	/** Espaço, em pixéis, entre o símbolo da legenda e a sua descrição. */
	private static final int GAP_SYMBOL_TEXT = 4;

	/** Espaço, em pixéis, entre duas entradas da legenda. */
	private static final int GAP_ENTRIES = 18;

	// ------------------------------------------------------------------
	// Cores

	/** Cor das posições de água ainda não atingidas. */
	private static final Color WATER = new Color(0xD6, 0xEC, 0xF8);

	/** Cor de contorno da grelha. */
	private static final Color GRID_LINE = new Color(0x90, 0xB4, 0xC8);

	/** Cor das posições de navio ainda a flutuar. */
	private static final Color HULL = new Color(0x37, 0x47, 0x4F);

	/** Cor de um navio atingido (o símbolo {@code *}). */
	private static final Color HIT = new Color(0xC0, 0x39, 0x2B);

	/** Cor de um tiro na água (o símbolo {@code o}) e das posições adjacentes a navio afundado. */
	private static final Color MISS = new Color(0x8A, 0x9A, 0xA5);

	/** Cor do título de cada grelha. */
	private static final Color CARD_TITLE = new Color(0x1B, 0x2A, 0x33);

	/** Cor dos textos secundários. */
	private static final Color MUTED = new Color(0x55, 0x66, 0x70);

	// ------------------------------------------------------------------
	// Tipos de letra

	/** Fonte usada para as etiquetas e para os símbolos do tabuleiro. */
	private static final Font MONO = new Font(Font.MONOSPACED, Font.BOLD, 16);

	/** Fonte usada nas descrições e nas estatísticas. */
	private static final Font TEXT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	/** Fonte usada nos títulos. */
	private static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 15);

	/** Fonte usada no título geral da janela. */
	private static final Font WINDOW_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 19);

	// ------------------------------------------------------------------
	// Estado partilhado com a thread da consola

	/** A nossa frota (grelha de defesa). */
	private static IFleet myFleet;

	/** As jogadas do adversário contra a nossa frota (grelha de defesa). */
	private static List<IMove> alienMoves;

	/** A frota adversária (grelha de ataque). */
	private static IFleet alienFleet;

	/** As nossas jogadas (grelha de ataque). */
	private static List<IMove> myMoves;

	/** Painel que desenha os tabuleiros, ou {@code null} se ainda não foi criado. */
	private static BoardCanvas canvas;

	/** Verdadeiro a partir do momento em que a criação da janela é pedida. */
	private static volatile boolean windowRequested;

	/** Verdadeiro quando a frota adversária deve ser revelada na grelha de ataque. */
	private static volatile boolean enemyFleetRevealed = true;

	/** Construtor privado: esta classe é apenas um ponto de entrada estático. */
	private BoardView() {
	}

	// ------------------------------------------------------------------
	// API pública

	/**
	 * Verifica se o ambiente atual permite abrir janelas.
	 *
	 * @return {@code true} se a JVM estiver em modo <i>headless</i> ou se não existir nenhum
	 *         ecrã disponível; {@code false} caso contrário
	 */
	public static boolean isUnavailable() {
		return GraphicsEnvironment.isHeadless();
	}

	/**
	 * Indica se a frota adversária está a ser revelada na grelha de ataque.
	 *
	 * @return {@code true} se as posições reais dos navios adversários forem visíveis
	 */
	public static boolean isEnemyFleetRevealed() {
		return enemyFleetRevealed;
	}

	/**
	 * Define se a frota adversária deve ser revelada na grelha de ataque.
	 *
	 * <p>Numa partida a sério contra outro jogador, a grelha de ataque deve mostrar apenas os
	 * tiros que já disparámos, e não os navios que ainda não descobrimos — caso contrário a
	 * janela estaria a revelar informação que o jogador não tem.</p>
	 *
	 * @param revealed {@code true} para revelar a frota adversária
	 */
	public static void setEnemyFleetRevealed(boolean revealed) {
		enemyFleetRevealed = revealed;
		refresh();
	}

	/**
	 * Abre (ou reutiliza) a janela dos tabuleiros e atualiza o seu conteúdo.
	 *
	 * <p>Este é o <b>único</b> ponto de entrada da vista gráfica. Em ambiente sem ecrã imprime
	 * um aviso e regressa imediatamente, sem lançar exceções.</p>
	 *
	 * @param myFleet    a nossa frota; se for {@code null} é impresso um aviso e nada é feito
	 * @param alienMoves as jogadas do adversário contra nós, ou {@code null}
	 * @param alienFleet a frota adversária, ou {@code null}
	 * @param myMoves    as nossas jogadas, ou {@code null}
	 */
	public static void show(IFleet myFleet, List<IMove> alienMoves, IFleet alienFleet, List<IMove> myMoves) {
		if (isUnavailable()) {
			System.out.println("AVISO: ambiente sem ecra (headless) - a janela grafica nao pode ser aberta.");
			return;
		}

		if (myFleet == null) {
			System.out.println("AVISO: ainda nao existe frota. Execute 'gerafrota'"
					+ " ou 'lefrota' antes de abrir a janela.");
			return;
		}

		update(myFleet, alienMoves, alienFleet, myMoves);

		if (windowRequested) {
			// A janela ja foi pedida: se ja existir, basta redesenhar.
			refresh();
			return;
		}

		System.out.println("Janela grafica dos tabuleiros aberta. Comandos da consola continuam ativos.");

		Thread gui = new Thread(BoardView::buildWindow, "battleship-board-view");
		gui.setDaemon(true);
		gui.start();
	}

	/**
	 * Regista as frotas e as jogadas a apresentar e atualiza a janela, caso esta exista.
	 *
	 * @param myFleet    a nossa frota
	 * @param alienMoves as jogadas do adversário contra nós
	 * @param alienFleet a frota adversária
	 * @param myMoves    as nossas jogadas
	 */
	public static void update(IFleet myFleet, List<IMove> alienMoves, IFleet alienFleet, List<IMove> myMoves) {
		synchronized (BoardView.class) {
			BoardView.myFleet = myFleet;
			BoardView.alienMoves = alienMoves;
			BoardView.alienFleet = alienFleet;
			BoardView.myMoves = myMoves;
		}
		refresh();
	}

	/**
	 * Atualiza a janela com os dados mais recentes. Não faz nada se a janela ainda não existir
	 * ou se o ambiente for <i>headless</i>, pelo que pode ser chamado sem risco a cada jogada.
	 */
	public static void refresh() {
		BoardCanvas target = canvas;
		if (target == null) {
			return;
		}
		SwingUtilities.invokeLater(target::repaint);
	}

	/**
	 * Desenha os dois tabuleiros para uma imagem em memória, sem necessitar de ecrã.
	 *
	 * <p>Útil para gerar capturas de imagem automáticas (por exemplo, para o vídeo do ponto B11)
	 * a partir de um ambiente de integração contínua.</p>
	 *
	 * @param myFleet    a nossa frota
	 * @param alienMoves as jogadas do adversário contra nós
	 * @param alienFleet a frota adversária
	 * @param myMoves    as nossas jogadas
	 * @return uma imagem com os dois tabuleiros desenhados
	 */
	public static BufferedImage renderToImage(IFleet myFleet, List<IMove> alienMoves,
			IFleet alienFleet, List<IMove> myMoves) {
		int width = totalWidth();
		int height = totalHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		try {
			paintBoards(g, width, height, myFleet, alienMoves, alienFleet, myMoves);
		} finally {
			g.dispose();
		}
		return image;
	}

	/**
	 * Devolve o tabuleiro em forma de texto, com exatamente o mesmo aspeto que
	 * {@link Game#printBoard} produz na consola.
	 *
	 * <p>Existe sobretudo para efeitos de teste: permite verificar que a vista gráfica e a vista
	 * de consola representam a mesma informação.</p>
	 *
	 * @param fleet a frota a desenhar
	 * @param moves as jogadas a sobrepor ao tabuleiro
	 * @return o tabuleiro em forma de texto
	 */
	public static String boardAsText(IFleet fleet, List<IMove> moves) {
		return Game.renderBoard(Game.buildMap(fleet, moves));
	}

	// ------------------------------------------------------------------
	// Construção da janela

	/**
	 * Cria e mostra a janela. Executado na <i>thread</i> da interface gráfica.
	 */
	private static void buildWindow() {
		try {
			FlatLightLaf.setup();

			JFrame janela = new JFrame("Battleship - Visualizacao grafica dos tabuleiros");
			janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			janela.setResizable(false);

			BoardCanvas painel = new BoardCanvas();
			painel.setPreferredSize(new Dimension(totalWidth(), totalHeight()));
			painel.setBackground(Color.WHITE);
			janela.setContentPane(painel);
			janela.pack();
			janela.setLocationRelativeTo(null);

			canvas = painel;

			janela.setVisible(true);
			windowRequested = true;
		} catch (Throwable t) {
			// Nunca deixar uma falha da interface grafica derrubar o jogo na consola.
			System.out.println("AVISO: nao foi possivel abrir a janela grafica (" + t.getClass().getSimpleName()
					+ ": " + t.getMessage() + "). O jogo continua na consola.");
			windowRequested = false;
		}
	}

	// ------------------------------------------------------------------
	// Desenho

	/**
	 * Painel Swing responsável por desenhar os dois tabuleiros.
	 */
	private static final class BoardCanvas extends JPanel {

		private static final long serialVersionUID = 1L;

		BoardCanvas() {
			setOpaque(true);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			IFleet defenseFleet;
			List<IMove> defenseMoves;
			IFleet attackFleet;
			List<IMove> attackMoves;
			synchronized (BoardView.class) {
				defenseFleet = myFleet;
				defenseMoves = alienMoves;
				attackFleet = alienFleet;
				attackMoves = myMoves;
			}

			paintBoards((Graphics2D) g, getWidth(), getHeight(),
					defenseFleet, defenseMoves, attackFleet, attackMoves);
		}
	}

	/**
	 * Desenha a janela completa: título geral e as duas grelhas.
	 *
	 * @param g          a superfície de desenho
	 * @param width      a largura disponível
	 * @param height     a altura disponível
	 * @param myFleet    a nossa frota
	 * @param alienMoves as jogadas do adversário contra nós
	 * @param alienFleet a frota adversária
	 * @param myMoves    as nossas jogadas
	 */
	private static void paintBoards(Graphics2D g, int width, int height,
			IFleet myFleet, List<IMove> alienMoves, IFleet alienFleet, List<IMove> myMoves) {

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		g.setColor(CARD_TITLE);
		g.setFont(WINDOW_TITLE);
		g.drawString("Batalha Naval Quinhentista", CARD_PAD, 24);

		g.setFont(TEXT);
		g.setColor(MUTED);
		g.drawString(enemyFleetRevealed
				? "Grelha de Defesa: a nossa frota | Grelha de Ataque: os nossos tiros (frota inimiga revelada)"
				: "Grelha de Defesa: a nossa frota | Grelha de Ataque: os nossos tiros",
				CARD_PAD, 42);

		int left = CARD_PAD;
		paintBoard(g, left, WINDOW_TITLE_H, "Grelha de Defesa",
				Game.buildMap(myFleet, alienMoves));

		int right = left + cardWidth() + GAP_BOARDS;
		paintBoard(g, right, WINDOW_TITLE_H, "Grelha de Ataque",
				Game.buildMap(enemyFleetRevealed ? alienFleet : null, myMoves));
	}

	/**
	 * Desenha uma grelha completa, com o seu título, eixos e estatísticas.
	 *
	 * @param g     a superfície de desenho
	 * @param x     a abcissa da margem esquerda da grelha
	 * @param y     a ordenada do topo da grelha
	 * @param title o título da grelha
	 * @param map   a matriz de marcadores produzida por {@link Game#buildMap}
	 */
	private static void paintBoard(Graphics2D g, int x, int y, String title, char[][] map) {
		g.setColor(CARD_TITLE);
		g.setFont(TITLE);
		g.drawString(title, x + LABEL_W, y + 20);

		int gridX = x + LABEL_W;
		int gridY = y + CARD_TITLE_H + LABEL_H;

		paintColumnLabels(g, gridX, gridY);
		paintRowLabels(g, x, gridX, gridY);
		paintCells(g, gridX, gridY, map);
		paintFooter(g, x, gridY, map);
	}

	/**
	 * Desenha as etiquetas das colunas (1..10).
	 *
	 * @param g     a superfície de desenho
	 * @param gridX a abcissa da primeira coluna da grelha
	 * @param gridY a ordenada da primeira linha da grelha
	 */
	private static void paintColumnLabels(Graphics2D g, int gridX, int gridY) {
		g.setFont(MONO);
		g.setColor(new Color(0x33, 0x44, 0x50));
		for (int col = 0; col < Game.BOARD_SIZE; col++) {
			String label = String.valueOf(new Position(0, col).getClassicColumn());
			int textWidth = g.getFontMetrics().stringWidth(label);
			g.drawString(label, gridX + col * CELL + (CELL - textWidth) / 2, gridY - 8);
		}
	}

	/**
	 * Desenha as etiquetas das linhas (A..J), à esquerda da grelha.
	 *
	 * @param g     a superfície de desenho
	 * @param x     a abcissa da margem esquerda
	 * @param gridX a abcissa da primeira coluna da grelha
	 * @param gridY a ordenada da primeira linha da grelha
	 */
	private static void paintRowLabels(Graphics2D g, int x, int gridX, int gridY) {
		g.setFont(MONO);
		g.setColor(new Color(0x33, 0x44, 0x50));
		for (int row = 0; row < Game.BOARD_SIZE; row++) {
			String label = String.valueOf(new Position(row, 0).getClassicRow());
			int textWidth = g.getFontMetrics().stringWidth(label);
			g.drawString(label, gridX - textWidth - 8, gridY + row * CELL + CELL / 2 + 6);
		}
	}

	/**
	 * Desenha as células de uma grelha.
	 *
	 * @param g     a superfície de desenho
	 * @param gridX a abcissa da primeira coluna da grelha
	 * @param gridY a ordenada da primeira linha da grelha
	 * @param map   a matriz de marcadores
	 */
	private static void paintCells(Graphics2D g, int gridX, int gridY, char[][] map) {
		for (int row = 0; row < Game.BOARD_SIZE; row++) {
			for (int col = 0; col < Game.BOARD_SIZE; col++) {
				int x = gridX + col * CELL;
				int y = gridY + row * CELL;
				char marker = map[row][col];

				g.setColor(WATER);
				g.fillRect(x, y, CELL, CELL);

				if (marker == Game.SHIP_MARKER || marker == Game.SHOT_SHIP_MARKER) {
					// Corpo do navio: retângulo arredondado, para se distinguir da água.
					g.setColor(marker == Game.SHOT_SHIP_MARKER ? HIT : HULL);
					g.fillRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 12, 12);
				} else if (marker == Game.SHOT_WATER_MARKER) {
					g.setColor(MISS);
					g.fillOval(x + CELL / 2 - 6, y + CELL / 2 - 6, 12, 12);
				} else if (marker == Game.SHIP_ADJACENT_MARKER) {
					// Marca discreta de "posição descartada" à volta de um navio afundado.
					g.setColor(MISS);
					g.fillRect(x + CELL / 2 - 2, y + CELL / 2 - 2, 4, 4);
				}

				g.setColor(GRID_LINE);
				g.setStroke(new BasicStroke(1f));
				g.drawRect(x, y, CELL, CELL);
			}
		}
		g.setColor(GRID_LINE);
		g.setStroke(new BasicStroke(1f));
		g.drawRect(gridX, gridY, Game.BOARD_SIZE * CELL, Game.BOARD_SIZE * CELL);
	}

	/**
	 * Desenha as estatísticas por baixo de uma grelha.
	 *
	 * @param g     a superfície de desenho
	 * @param x     a abcissa da margem esquerda
	 * @param gridY a ordenada da primeira linha da grelha
	 * @param map   a matriz de marcadores
	 */
	private static void paintFooter(Graphics2D g, int x, int gridY, char[][] map) {
		int visibleShips = 0;
		int hits = 0;
		int misses = 0;
		for (int row = 0; row < Game.BOARD_SIZE; row++) {
			for (int col = 0; col < Game.BOARD_SIZE; col++) {
				char marker = map[row][col];
				if (marker == Game.SHIP_MARKER) {
					visibleShips++;
				} else if (marker == Game.SHOT_SHIP_MARKER) {
					visibleShips++;
					hits++;
				} else if (marker == Game.SHOT_WATER_MARKER) {
					misses++;
				}
			}
		}
		int width = cardWidth();
		int y = gridY + Game.BOARD_SIZE * CELL + 18;

		g.setFont(TEXT);
		g.setColor(MUTED);
		String line = "Navios visiveis: " + visibleShips
				+ "   Tiros certeiros: " + hits
				+ "   Tiros na agua: " + misses;
		g.drawString(line, x + LABEL_W, y);

		// Legenda compacta por baixo das estatísticas.
		g.setColor(GRID_LINE);
		g.drawLine(x + LABEL_W, y + 6, x + LABEL_W + width - 2 * LABEL_W, y + 6);
		paintLegend(g, x + LABEL_W, y + 22,
				new LegendItem(Game.SHIP_MARKER, HULL, "navio"),
				new LegendItem(Game.SHOT_SHIP_MARKER, HIT, "certeiro"));
		paintLegend(g, x + LABEL_W, y + 40,
				new LegendItem(Game.SHOT_WATER_MARKER, MISS, "agua"),
				new LegendItem(Game.SHIP_ADJACENT_MARKER, MISS, "adjacente a afundado"));
	}

	/**
	 * Desenha uma linha da legenda.
	 *
	 * @param g     a superfície de desenho
	 * @param x     a abcissa onde começa a linha
	 * @param y     a ordenada do texto
	 * @param items as entradas da legenda
	 */
	private static void paintLegend(Graphics2D g, int x, int y, LegendItem... items) {
		FontMetrics monoMetrics = g.getFontMetrics(MONO);
		FontMetrics textMetrics = g.getFontMetrics(TEXT);

		for (LegendItem item : items) {
			String symbol = String.valueOf(item.symbol);

			g.setFont(MONO);
			g.setColor(item.color);
			g.drawString(symbol, x, y);

			g.setFont(TEXT);
			g.setColor(MUTED);
			g.drawString(item.text, x + monoMetrics.stringWidth(symbol) + GAP_SYMBOL_TEXT, y);

			x += monoMetrics.stringWidth(symbol) + GAP_SYMBOL_TEXT + textMetrics.stringWidth(item.text)
					+ GAP_ENTRIES;
		}
	}

	/**
	 * Uma entrada da legenda: o símbolo e a sua descrição.
	 */
	private static final class LegendItem {

		/** O símbolo tal como aparece no tabuleiro. */
		private final char symbol;

		/** A cor com que o símbolo é desenhado. */
		private final Color color;

		/** A descrição do símbolo. */
		private final String text;

		/**
		 * Cria uma entrada da legenda.
		 *
		 * @param symbol o símbolo do tabuleiro
		 * @param color  a cor do símbolo
		 * @param text   a descrição do símbolo
		 */
		private LegendItem(char symbol, Color color, String text) {
			this.symbol = symbol;
			this.color = color;
			this.text = text;
		}
	}

	// ------------------------------------------------------------------
	// Auxiliares de dimensão

	/**
	 * Devolve a largura de uma grelha, incluindo as duas margens das etiquetas.
	 *
	 * @return a largura de uma grelha, em pixéis
	 */
	private static int cardWidth() {
		return 2 * CARD_PAD + LABEL_W + Game.BOARD_SIZE * CELL + LABEL_W;
	}

	/**
	 * Devolve a largura total da janela.
	 *
	 * @return a largura necessária para as duas grelhas, em pixéis
	 */
	private static int totalWidth() {
		return 2 * CARD_PAD + 2 * cardWidth() + GAP_BOARDS;
	}

	/**
	 * Devolve a altura total da janela.
	 *
	 * @return a altura necessária, em pixéis
	 */
	private static int totalHeight() {
		int card = CARD_TITLE_H + LABEL_H + Game.BOARD_SIZE * CELL + CARD_FOOTER_H + 46;
		return WINDOW_TITLE_H + card + CARD_PAD;
	}
}
