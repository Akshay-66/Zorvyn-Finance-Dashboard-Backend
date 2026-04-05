package com.zorvyn.finance.config;

public record AppConfig(
        int port,
        String dbUrl,
        String dbUsername,
        String dbPassword,
        int dbRetryAttempts
) {

    public static AppConfig fromEnvironment() {
        return new AppConfig(
                readInt("APP_PORT", 8080),
                readString("DB_URL", "jdbc:postgresql://localhost:5432/finance_dashboard"),
                readString("DB_USERNAME", "postgres"),
                readString("DB_PASSWORD", "postgres"),
                readInt("DB_RETRY_ATTEMPTS", 20)
        );
    }

    private static String readString(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static int readInt(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }
}

