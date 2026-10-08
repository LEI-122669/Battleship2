package battleship;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Utility class responsible for exporting game reports to PDF documents
 * using the iText library.
 */
public class PDFExporter {

    /**
     * Exports the provided game report content into a PDF file at the specified file path.
     *
     * @param filePath the path where the generated PDF file will be saved.
     * @param content  the text content (statistics, history of moves, etc.) to be included in the PDF body.
     */
    public static void exportGameReport(String filePath, String content) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.add(new Paragraph("=========================================="));
            document.add(new Paragraph("      RELATÓRIO DE JOGO - BATALHA NAVAL   "));
            document.add(new Paragraph("=========================================="));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(content));
            document.close();
            System.out.println("Relatório PDF gerado com sucesso em: " + filePath);
        } catch (Exception e) {
            System.err.println("Erro ao gerar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}