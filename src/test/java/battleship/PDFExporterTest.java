package battleship;

import org.junit.jupiter.api.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PDFExporter.
 *
 * Author: LEI124359
 * Date: 2026-11-06
 */
public class PDFExporterTest {

    private static final String VALID_FILE_PATH = "test_report.pdf";
    private static final String INVALID_FILE_PATH = "/invalid_directory_path_12345/test_report.pdf";

    @AfterEach
    void tearDown() {
        // Cleanup temporary generated test file
        File file = new File(VALID_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("Test PDF export success with valid file path and content")
    void testExportGameReportSuccess() {
        String content = "Статистика гри: Перемога Gravets!\nКількість ходів: 15";

        // Execute method
        PDFExporter.exportGameReport(VALID_FILE_PATH, content);

        File pdfFile = new File(VALID_FILE_PATH);
        assertTrue(pdfFile.exists(), "PDF file should be created successfully");
        assertTrue(pdfFile.length() > 0, "Generated PDF file should not be empty");
    }

    @Test
    @DisplayName("Test PDF export exception handling with invalid file path")
    void testExportGameReportException() {
        String content = "Тестовий контент";

        // This will trigger the catch block due to invalid path/access
        assertDoesNotThrow(() -> PDFExporter.exportGameReport(INVALID_FILE_PATH, content),
                "exportGameReport should catch exception internally and not crash");
    }
}