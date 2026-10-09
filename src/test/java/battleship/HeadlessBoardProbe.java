package battleship;

import java.util.ArrayList;

/**
 * Sonda executada num processo Java separado em modo <i>headless</i>.
 *
 * <p>Serve para verificar o criterio de aceitacao "em ambiente sem ecra, ao executar
 * {@code janela} e impressa uma mensagem de aviso e nao e lancada nenhuma excecao". Como o
 * ambiente de desenvolvimento pode ter ecra, a unica forma honesta de testar este caminho e
 * arrancar uma JVM propria com {@code -Djava.awt.headless=true}.</p>
 *
 * <p>Esta classe nao e um teste JUnit: e invocada por {@link BoardViewTest}. Termina com codigo
 * de saida 0 se o comportamento estiver correto e 1 caso contrario.</p>
 */
public final class HeadlessBoardProbe {

	/** Construtor privado: classe utilitaria. */
	private HeadlessBoardProbe() {
	}

	/**
	 * Executa a sonda.
	 *
	 * <p>Todas as mensagens sao deliberadamente so em ASCII, para que a leitura do ficheiro de
	 * saida nao dependa do {@code file.encoding} da JVM filha.</p>
	 *
	 * @param args nao utilizados
	 */
	public static void main(String[] args) {
		java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();

		if (!java.awt.GraphicsEnvironment.isHeadless()) {
			System.err.println("FAIL: the child JVM is not in headless mode.");
			System.exit(1);
		}
		if (!BoardView.isUnavailable()) {
			System.err.println("FAIL: BoardView.isUnavailable() should return true.");
			System.exit(1);
		}

		try {
			// Nenhuma destas chamadas pode lancar excecao nem abrir janela.
			BoardView.show(null, null, null, null);
			BoardView.show(Fleet.createRandom(), new ArrayList<>(), Fleet.createRandom(), new ArrayList<>());
			BoardView.refresh();
			BoardView.update(Fleet.createRandom(), new ArrayList<>(), Fleet.createRandom(), new ArrayList<>());
		} catch (Throwable t) {
			System.err.println("FAIL: an exception was thrown in a headless environment: " + t);
			System.exit(1);
		}

		System.out.println("HEADLESS_OK");
	}
}
