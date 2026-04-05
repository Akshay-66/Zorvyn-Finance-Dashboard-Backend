package com.zorvyn.finance;

import com.sun.net.httpserver.HttpServer;
import com.zorvyn.finance.config.AppConfig;
import com.zorvyn.finance.db.Database;
import com.zorvyn.finance.handler.DashboardHandler;
import com.zorvyn.finance.handler.HealthHandler;
import com.zorvyn.finance.handler.ProfileHandler;
import com.zorvyn.finance.handler.RecordHandler;
import com.zorvyn.finance.handler.UserHandler;
import com.zorvyn.finance.repository.RecordRepository;
import com.zorvyn.finance.repository.UserRepository;
import com.zorvyn.finance.security.AccessManager;
import com.zorvyn.finance.service.AuthService;
import com.zorvyn.finance.service.DashboardService;
import com.zorvyn.finance.service.FinanceFacade;
import com.zorvyn.finance.service.RecordService;
import com.zorvyn.finance.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException {
        AppConfig config = AppConfig.fromEnvironment();
        Database database = new Database(config);
        database.waitUntilReady();

        UserRepository userRepository = new UserRepository(database);
        RecordRepository recordRepository = new RecordRepository(database);

        AuthService authService = new AuthService(userRepository);
        AccessManager accessManager = new AccessManager();
        UserService userService = new UserService(userRepository);
        RecordService recordService = new RecordService(recordRepository);
        DashboardService dashboardService = new DashboardService(recordRepository);

        FinanceFacade financeFacade = new FinanceFacade(
                authService,
                accessManager,
                userService,
                recordService,
                dashboardService
        );

        HttpServer server = HttpServer.create(new InetSocketAddress(config.port()), 0);
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/me", new ProfileHandler(financeFacade));
        server.createContext("/api/users", new UserHandler(financeFacade));
        server.createContext("/api/records", new RecordHandler(financeFacade));
        server.createContext("/api/dashboard/summary", new DashboardHandler(financeFacade));
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Finance backend started on port " + config.port());
    }
}

