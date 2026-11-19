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

    /**
     * Same as {@link #read(String)} but keeps only the rows picked by {@code rows}. Row numbers are 1-based and
     * do not count the header. Accepts "5", "2,5", "3-5", "1,4-6", or text that appears in the first column
     * (e.g. "Error account"). Blank/null = every row.
     */
    public static Object[][] read(String classpathFile, String rows) {
        Object[][] all = read(classpathFile);
        if (rows == null || rows.isBlank()) return all;

        java.util.Set<Integer> wanted = new java.util.TreeSet<>();
        boolean numeric = rows.matches("[\\d,\\-\\s]+");
        if (numeric) {
            for (String part : rows.split(",")) {
                String[] range = part.trim().split("-");
                int from = Integer.parseInt(range[0].trim());
                int to = range.length > 1 ? Integer.parseInt(range[1].trim()) : from;
                for (int i = from; i <= to; i++) wanted.add(i);
            }
        }
        List<Object[]> picked = new ArrayList<>();
        for (int i = 0; i < all.length; i++) {
            boolean hit = numeric ? wanted.contains(i + 1)
                    : String.valueOf(all[i][0]).toLowerCase().contains(rows.trim().toLowerCase());
            if (hit) picked.add(all[i]);
        }
        if (picked.isEmpty()) {
            throw new IllegalArgumentException("csv.rows='" + rows + "' matched nothing in " + classpathFile
                    + " (it has " + all.length + " data rows)");
        }
        return picked.toArray(new Object[0][]);
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
