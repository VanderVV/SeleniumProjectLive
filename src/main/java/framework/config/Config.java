package framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public final class Config {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
    }

    private Config() {
    }

    public static String get(String key, String defaultValue) {
        return System.getProperty(key, PROPS.getProperty(key, defaultValue));
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)).trim());
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)).trim());
    }


    public static String env() {
        return get("env", "default");
    }


    public static String baseUrl() {
        String override = System.getProperty("base.url");
        if (override != null && !override.isBlank()) {
            return stripTrailingSlash(override.trim());
        }
        String url = get("env." + env() + ".url");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("No URL for env '" + env() + "'. Add env." + env()
                    + ".url to config.properties or run with -Dbase.url=https://...");
        }
        return stripTrailingSlash(url.trim());
    }


    public static String urlOf(String envName) {
        String url = get("env." + envName + ".url");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("No env." + envName + ".url in config.properties");
        }
        return stripTrailingSlash(url.trim());
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
