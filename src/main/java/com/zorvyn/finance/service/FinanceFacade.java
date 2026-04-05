package com.zorvyn.finance.service;

import com.zorvyn.finance.security.AccessManager;

public class FinanceFacade {

    private final AuthService authService;
    private final AccessManager accessManager;
    private final UserService userService;
    private final RecordService recordService;
    private final DashboardService dashboardService;

    public FinanceFacade(AuthService authService, AccessManager accessManager, UserService userService,
                         RecordService recordService, DashboardService dashboardService) {
        this.authService = authService;
        this.accessManager = accessManager;
        this.userService = userService;
        this.recordService = recordService;
        this.dashboardService = dashboardService;
    }

    public AuthService authService() {
        return authService;
    }

    public AccessManager accessManager() {
        return accessManager;
    }

    public UserService userService() {
        return userService;
    }

    public RecordService recordService() {
        return recordService;
    }

    public DashboardService dashboardService() {
        return dashboardService;
    }
}
