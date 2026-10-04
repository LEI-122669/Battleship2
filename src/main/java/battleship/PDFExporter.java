package battleship; // Перевір, щоб назва пакета збігалася з іншими файлами у цій папці!

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

public class PDFExporter {

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