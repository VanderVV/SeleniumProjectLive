package framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Usernames, passwords and tokens. Looked up in this order:
 * 1. environment variable  SAUCEDEMO_PASSWORD   (key upper-cased, '.' -> '_'; use this in CI)
 * 2. system property       -Dsaucedemo.password=...
 * 3. secrets.properties    (git-ignored file next to config.properties)
 *
 * Every value that is read is remembered, and {@link #mask(String)} replaces it with ******** so it can
 * never reach the Allure or Extent report, even if it is typed through a normal method by mistake.
 */
public final class Secrets {

    public static final String MASK = "********";

    private static final Properties FILE = new Properties();
    private static final Set<String> KNOWN_VALUES = ConcurrentHashMap.newKeySet();

    static {
        try (InputStream in = Secrets.class.getClassLoader().getResourceAsStream("secrets.properties")) {
            if (in != null) {
                FILE.load(in);
                FILE.stringPropertyNames().forEach(k -> remember(FILE.getProperty(k)));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load secrets.properties", e);
        }
    }

    private Secrets() {
    }

    public static String get(String key) {
        String value = System.getenv(key.toUpperCase().replace('.', '_'));
        if (value == null || value.isBlank()) value = System.getProperty(key);
        if (value == null || value.isBlank()) value = FILE.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Secret '" + key + "' not found. Add it to secrets.properties "
                    + "(see secrets.example.properties) or set env var " + key.toUpperCase().replace('.', '_'));
        }
        remember(value);
        return value;
    }


    public static String mask(String text) {
        if (text == null) return null;
        String masked = text;
        for (String secret : KNOWN_VALUES) {
            masked = masked.replace(secret, MASK);
        }
        return masked;
    }

    private static void remember(String value) {
        if (value != null && value.length() >= 4) {
            KNOWN_VALUES.add(value.trim());
        }
    }
}
