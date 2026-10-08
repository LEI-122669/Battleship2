package battleship;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Sonda de demonstracao: gera uma imagem PNG das duas grelhas, sem abrir janela.
 *
 * <p>Serve para inspecionar visualmente o resultado da vista grafica. Nao faz parte do
 * produto: vive em {@code src/test} e apenas escreve um ficheiro em {@code target/}.</p>
 */
public final class BoardPreview {

	private BoardPreview() {
	}

	/**
	 * Gera {@code target/board-preview.png} com uma partida em curso.
	 *
	 * @param args nao utilizados
	 * @throws Exception se a escrita da imagem falhar
	 */
	public static void main(String[] args) throws Exception {
		IFleet minhaFrota = Fleet.createRandom();
		Game game = new Game(minhaFrota);

		// A grelha de ataque so tem interesse se a frota adversaria nao estiver vazia.
		IFleet frotaInimiga = Fleet.createRandom();

		// --- Grelha de defesa: o adversario dispara contra nos -------------------
		List<IPosition> usadas = new ArrayList<>();
		IPosition alvoDefesa = primeiraPosicaoDeNavio(minhaFrota, usadas);
		usadas.add(alvoDefesa);
		game.fireShots(List.of(alvoDefesa, proximaAguaLivre(minhaFrota, usadas),
				proximaAguaLivre(minhaFrota, usadas)));

		// Afundar uma barca, para aparecerem as marcas de posicao adjacente.
		IShip barca = primeiraDeTamanho(minhaFrota, 1);
		if (barca != null) {
			List<IPosition> jogada = new ArrayList<>(barca.getPositions());
			while (jogada.size() < Game.NUMBER_SHOTS) {
				IPosition agua = proximaAguaLivre(minhaFrota, jogada);
				jogada.add(agua);
			}
			game.fireShots(jogada);
		}

		// --- Grelha de ataque: nos disparamos contra o adversario ---------------
		List<IPosition> usadasAtaque = new ArrayList<>();
		IPosition alvoAtaque = primeiraPosicaoDeNavio(frotaInimiga, usadasAtaque);
		usadasAtaque.add(alvoAtaque);
		game.fireShotsAtAlienFleet(List.of(alvoAtaque,
				proximaAguaLivre(frotaInimiga, usadasAtaque),
				proximaAguaLivre(frotaInimiga, usadasAtaque)));

		// Uma jogada que afunda uma barca inimiga, para se ver o efeito completo.
		IShip barcaInimiga = primeiraDeTamanho(frotaInimiga, 1);
		if (barcaInimiga != null) {
			List<IPosition> jogada = new ArrayList<>(barcaInimiga.getPositions());
			while (jogada.size() < Game.NUMBER_SHOTS) {
				IPosition agua = proximaAguaLivre(frotaInimiga, jogada);
				jogada.add(agua);
			}
			game.fireShotsAtAlienFleet(jogada);
		}

		ImageIO.write(BoardView.renderToImage(minhaFrota, game.getAlienMoves(),
				frotaInimiga, game.getMyMoves()), "png", new File("target/board-preview.png"));

		// Mostrar tambem o equivalente em texto, para comparacao direta.
		System.out.println("--- GRELHA DE DEFESA ---");
		System.out.println(BoardView.boardAsText(minhaFrota, game.getAlienMoves()));
		System.out.println("--- GRELHA DE ATAQUE ---");
		System.out.println(BoardView.boardAsText(frotaInimiga, game.getMyMoves()));
		System.out.println("PNG escrito em target/board-preview.png");
	}

	/**
	 * Devolve a primeira posicao ocupada por um navio da frota.
	 *
	 * @param fleet a frota a procurar
	 * @param evitar posicoes a ignorar
	 * @return uma posicao ocupada por navio
	 */
	private static IPosition primeiraPosicaoDeNavio(IFleet fleet, List<IPosition> evitar) {
		for (IShip s : fleet.getFloatingShips())
			for (IPosition p : s.getPositions())
				if (!evitar.contains(p))
					return p;
		throw new IllegalStateException("nao ha posicoes de navio livres");
	}

	/**
	 * Devolve o primeiro navio da frota com o tamanho indicado.
	 *
	 * @param fleet a frota a procurar
	 * @param tamanho o tamanho pretendido
	 * @return o navio encontrado, ou {@code null} se nao existir
	 */
	private static IShip primeiraDeTamanho(IFleet fleet, int tamanho) {
		for (IShip s : fleet.getShips())
			if (s.getSize() == tamanho)
				return s;
		return null;
	}

	/**
	 * Encontra uma posicao de agua ainda livre de navios e ainda nao usada nesta jogada.
	 *
	 * @param fleet a frota a evitar
	 * @param jaUsadas as posicoes ja escolhidas para esta jogada
	 * @return uma posicao sem navio
	 */
	private static IPosition proximaAguaLivre(IFleet fleet, List<IPosition> jaUsadas) {
		for (int r = Game.BOARD_SIZE - 1; r >= 0; r--)
			for (int c = Game.BOARD_SIZE - 1; c >= 0; c--) {
				IPosition p = new Position(r, c);
				if (fleet.shipAt(p) == null && !jaUsadas.contains(p))
					return p;
			}
		throw new IllegalStateException("nao ha agua livre");
	}
}
