package com.hotelbooking.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central place for configuration.
 * Lookup order: environment variable, then JVM system property (-Dkey=value), then config.properties.
 */
public final class ConfigManager {

    private static final Properties PROPS = load();

    private ConfigManager() {
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = ConfigManager.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read config.properties", e);
        }
        return props;
    }

    private static String get(String envKey, String propKey) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String sys = System.getProperty(propKey);
        if (sys != null && !sys.isBlank()) {
            return sys.trim();
        }
        return PROPS.getProperty(propKey);
    }

    public static String baseUrl() {
        return get("BASE_URL", "base.url");
    }

    public static String username() {
        return get("BOOKER_USERNAME", "auth.username");
    }

    public static String password() {
        return get("BOOKER_PASSWORD", "auth.password");
    }

    public static int connectTimeoutMs() {
        return Integer.parseInt(get("CONNECT_TIMEOUT_MS", "timeout.connect.ms"));
    }

    public static int readTimeoutMs() {
        return Integer.parseInt(get("READ_TIMEOUT_MS", "timeout.read.ms"));
    }
}
