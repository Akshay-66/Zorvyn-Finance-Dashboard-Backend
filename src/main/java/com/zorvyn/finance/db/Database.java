package com.zorvyn.finance.db;

import com.zorvyn.finance.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private final AppConfig config;

    public Database(AppConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                config.dbUrl(),
                config.dbUsername(),
                config.dbPassword()
        );
    }

    public void waitUntilReady() {
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= config.dbRetryAttempts(); attempt++) {
            try (Connection ignored = getConnection()) {
                return;
            } catch (SQLException exception) {
                lastError = new RuntimeException("Database connection failed", exception);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Database retry interrupted", interruptedException);
                }
            }
        }

        throw lastError == null ? new RuntimeException("Database connection failed") : lastError;
    }
}

