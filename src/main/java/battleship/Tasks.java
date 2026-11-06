package battleship;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The constant GOODBYE_MESSAGE.
	 */
	private static final String GOODBYE_MESSAGE = "Bons ventos!";

	/**
	 * Strings to be used by the user
	 */
	private static final String AJUDA = "ajuda";
	private static final String GERAFROTA = "gerafrota";
	private static final String LEFROTA = "lefrota";
	private static final String DESISTIR = "desisto";
	private static final String RAJADA = "rajada";
	private static final String TIROS = "tiros";
	private static final String MAPA = "mapa";
	private static final String STATUS = "estado";
	private static final String SIMULA = "simula";
	private static final String HISTORICO = "historico";
	private static final String WINNER_ENEMY = "INIMIGO";
	private static final String JANELA = "janela";
	private static final String PDF = "pdf";

	/**
	 * Main menu loop - Refactored to satisfy Qodana OverlyLongMethod inspection.
	 */
	public static void menu() {
		Game game = null;
		GameDatabase database = new GameDatabase();
		IFleet myFleet = null;
		menuHelp();

		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String command = in.next();
		while (!command.equals(DESISTIR)) {

			switch (command) {
				case GERAFROTA:
					myFleet = Fleet.createRandom();
					game = newGame(myFleet, database);
					game.printMyBoard(false, true);
					refreshBoardView(game);
					break;
				case LEFROTA:
					myFleet = buildFleet(in);
					game = newGame(myFleet, database);
					game.printMyBoard(false, true);
					refreshBoardView(game);
					break;
				case STATUS:
					if (myFleet != null)
						myFleet.printStatus();
					break;
				case MAPA:
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case RAJADA:
					handleRajadaCommand(game, myFleet, database, in);
					break;
				case PDF:
					handlePdfCommand(game);
					break;
				case SIMULA:
					handleSimulaCommand(game, myFleet, database);
					break;
				case TIROS:
					handleTirosCommand(game, in);
					break;
				case HISTORICO:
					printHistory(database, in.next());
					break;
				case JANELA:
					if (myFleet == null)
						System.out.println("Gere ou carregue uma frota primeiro ('" + GERAFROTA + "' ou '" + LEFROTA + "').");
					else
						BoardView.show(myFleet, game.getAlienMoves(), game.getAlienFleet(), game.getMyMoves());
					break;
				case AJUDA:
					menuHelp();
					break;
				default:
					System.out.println("Que comando é esse??? Repete ...");
			}
			System.out.print("> ");
			command = in.next();
		}
		System.out.println(GOODBYE_MESSAGE);
	}

	private static void handleRajadaCommand(Game game, IFleet myFleet, GameDatabase database, Scanner in) {
		if (game == null) return;

		game.startMoveTimer();
		try {
			game.readEnemyFire(in);
		} catch (IllegalArgumentException e) {
			System.out.println("Tiros inválidos: " + e.getMessage());
			return;
		}

		long elapsed = game.stopMoveTimer();
		System.out.println("Tempo demorado na jogada: " + MoveTimer.formatTime(elapsed));

		myFleet.printStatus();
		game.printMyBoard(true, false);

		if (game.getRemainingShips() == 0) {
			database.finishGame(game.getGameId(), WINNER_ENEMY, game);
			PDFExporter.exportGameReport("relatorio_jogo.pdf", game.generateReport());
			game.over();
			System.exit(0);
		}
	}

	private static void handlePdfCommand(Game game) {
		if (game != null) {
			String reportContent = game.generateReport();
			PDFExporter.exportGameReport("relatorio_jogo.pdf", reportContent);
		} else {
			System.out.println("Nenhum jogo em andamento! Crie ou leia uma frota primeiro.");
		}
	}

	private static void handleSimulaCommand(Game game, IFleet myFleet, GameDatabase database) {
		if (game == null) return;

		while (game.getRemainingShips() > 0) {
			game.randomEnemyFire();
			myFleet.printStatus();
			game.printMyBoard(true, false);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		if (game.getRemainingShips() == 0) {
			database.finishGame(game.getGameId(), WINNER_ENEMY, game);
			PDFExporter.exportGameReport("relatorio_jogo.pdf", game.generateReport());
			game.over();
			System.exit(0);
		}
	}

	private static void handleTirosCommand(Game game, Scanner in) {
		if (game == null) {
			System.out.println("Crie ou carregue uma frota primeiro.");
			return;
		}

		game.printAlienBoard(true, true);
		in.nextLine();
		System.out.println("Introduza 3 posições para atacar (ex.: A3 B5 C7):");

		game.startMoveTimer();
		try {
			String input = in.nextLine().trim();
			Scanner shotsInput = new Scanner(input);
			List<IPosition> shots = new java.util.ArrayList<>();

			while (shotsInput.hasNext()) {
				shots.add(readClassicPosition(shotsInput));
			}
			shotsInput.close();

			if (shots.size() != Game.NUMBER_SHOTS) {
				System.out.println("Tem de introduzir exatamente 3 posições.");
				game.stopMoveTimer();
				return;
			}

			game.fireShotsAtAlienFleet(shots);
			long elapsed = game.stopMoveTimer();
			System.out.println("Tempo da jogada: " + MoveTimer.formatTime(elapsed));
			game.printAlienBoard(true, true);

		} catch (IllegalArgumentException e) {
			game.stopMoveTimer();
			System.out.println("Posições inválidas: " + e.getMessage());
		}
	}

	private static void refreshBoardView(IGame game) {
		if (game == null)
			return;
		BoardView.update(game.getMyFleet(), game.getAlienMoves(), game.getAlienFleet(), game.getMyMoves());
	}

	public static void menuHelp() {
		System.out.println("======================= AJUDA DO MENU =========================");
		System.out.println("Digite um dos comandos abaixo para interagir com o jogo:");
		System.out.println("- " + GERAFROTA + ": Gera uma frota aleatória de navios.");
		System.out.println("- " + LEFROTA + ": Permite criar e carregar uma frota personalizada.");
		System.out.println("- " + STATUS + ": Mostra o status atual da frota.)");
		System.out.println("- " + MAPA + ": Exibe o mapa da frota.");
		System.out.println("- " + RAJADA + ": Realiza uma rajada de disparos.");
		System.out.println("- " + SIMULA + ": Simula um jogo completo.");
		System.out.println("- " + TIROS + ": Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água)");
		System.out.println("- " + HISTORICO + " <id>: Mostra as jogadas guardadas de um jogo.");
		System.out.println("- " + JANELA + ": Abre a janela gráfica com o tabuleiro do jogador.");
		System.out.println("- " + DESISTIR + ": Encerra o jogo.");
		System.out.println("- " + PDF + ": Exporta o relatório das jogadas para PDF.");
		System.out.println("===============================================================");
	}

	static Game newGame(IFleet fleet, GameDatabase database) {
		Game game = new Game(fleet);
		String gameId = UUID.randomUUID().toString();
		database.saveGame(gameId, "consola");
		game.setDatabase(database, gameId);
		System.out.println("Jogo guardado com o id: " + gameId);
		return game;
	}

	static void printHistory(GameDatabase database, String gameId) {
		if (!database.gameExists(gameId)) {
			System.out.println("Não existe nenhum jogo com o id " + gameId);
			return;
		}
		List<String> moves = database.findMoves(gameId);
		if (moves.isEmpty())
			System.out.println("O jogo " + gameId + " ainda não tem jogadas.");
		for (String line : moves)
			System.out.println(line);
	}

	public static Fleet buildFleet(Scanner in) {
		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0;
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	public static Ship readShip(Scanner in) {
		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	public static Position readPosition(Scanner in) {
		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	public static IPosition readClassicPosition(@NotNull Scanner in) {
		if (!in.hasNext()) {
			throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
		}

		String part1 = in.next();
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next();
		}

		String input = (part2 != null) ? part1 + part2 : part1;
		input = input.toUpperCase();

		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0);
			int row = Integer.parseInt(input.substring(1));
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0);
			int row = Integer.parseInt(part2);
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
		}
	}
}