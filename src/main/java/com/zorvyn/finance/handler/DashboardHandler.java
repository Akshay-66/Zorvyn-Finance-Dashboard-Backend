package com.zorvyn.finance.handler;

import com.sun.net.httpserver.HttpExchange;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.security.Permission;
import com.zorvyn.finance.service.FinanceFacade;
import com.zorvyn.finance.util.RequestUtil;
import com.zorvyn.finance.util.ResponseUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public class DashboardHandler extends BaseHandler {

    public DashboardHandler(FinanceFacade financeFacade) {
        super(financeFacade);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            methodNotAllowed(exchange);
            return;
        }

        User currentUser = authenticatedUser(exchange);
        requirePermission(currentUser, Permission.VIEW_SUMMARY);

        Map<String, String> queryParams = RequestUtil.queryParams(exchange.getRequestURI().getRawQuery());
        LocalDate fromDate = queryParams.containsKey("fromDate") && !queryParams.get("fromDate").isBlank()
                ? RequestUtil.readDate(queryParams.get("fromDate"), "fromDate")
                : null;
        LocalDate toDate = queryParams.containsKey("toDate") && !queryParams.get("toDate").isBlank()
                ? RequestUtil.readDate(queryParams.get("toDate"), "toDate")
                : null;

        ResponseUtil.sendJson(exchange, 200, financeFacade().dashboardService().getSummary(fromDate, toDate));
    }
}
