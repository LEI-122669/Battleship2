/**
 * 
 */
package battleship;

public class Main
{
	/**
	 * Main.
	 *
	 * @param args the args
	 */
	public static void main(String[] args)
    {
		System.out.println("***  Battleship  ***");

		PDFExporter.exportGameReport("relatorio_jogo.pdf", "Relatório de teste do jogo Batalha Naval.");

		Tasks.menu();

    }
}
