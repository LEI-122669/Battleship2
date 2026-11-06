package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PDFExporterTest {

    private static final String VALID_FILE_PATH = "test_report.pdf";
    private static final String INVALID_FILE_PATH = "/invalid_directory_path_12345/test_report.pdf";

    @AfterEach
    void tearDown() {
        File file = new File(VALID_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("Test PDF export success")
    void testExportGameReportSuccess() {
        PDFExporter.exportGameReport(VALID_FILE_PATH, "Game stats");
        File pdfFile = new File(VALID_FILE_PATH);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    @DisplayName("Test PDF export exception handling")
    void testExportGameReportException() {
        assertDoesNotThrow(() -> PDFExporter.exportGameReport(INVALID_FILE_PATH, "Test content"));
    }
}