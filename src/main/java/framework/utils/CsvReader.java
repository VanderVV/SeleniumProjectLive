package framework.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a CSV from the classpath into the Object[][] shape a TestNG @DataProvider expects.
 * The first line is a header and is skipped; blank lines and lines starting with # are ignored.
 * Keep passwords OUT of CSV files - put a secret KEY in the file and resolve it with Secrets.get(key).
 */
public final class CsvReader {

    private CsvReader() {
    }

    public static Object[][] read(String classpathFile) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathFile)) {
            if (in == null) {
                throw new IllegalArgumentException("Test data file not found on classpath: " + classpathFile);
            }
            List<Object[]> rows = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                reader.readLine(); // header
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String[] cells = line.split(",", -1);
                    for (int i = 0; i < cells.length; i++) cells[i] = cells[i].trim();
                    rows.add(cells);
                }
            }
            return rows.toArray(new Object[0][]);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + classpathFile, e);
        }
    }
}
