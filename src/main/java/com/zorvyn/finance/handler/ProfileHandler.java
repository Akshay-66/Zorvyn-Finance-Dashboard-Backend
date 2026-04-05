package com.zorvyn.finance.handler;

import com.sun.net.httpserver.HttpExchange;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.util.ResponseUtil;

import java.io.IOException;

public class ProfileHandler extends BaseHandler {

    public ProfileHandler(com.zorvyn.finance.service.FinanceFacade financeFacade) {
        super(financeFacade);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            methodNotAllowed(exchange);
            return;
        }

        User user = authenticatedUser(exchange);
        ResponseUtil.sendJson(exchange, 200, user);
    }
}

