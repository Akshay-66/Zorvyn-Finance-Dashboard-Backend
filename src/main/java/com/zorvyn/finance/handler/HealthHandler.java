package com.zorvyn.finance.handler;

import com.sun.net.httpserver.HttpExchange;
import com.zorvyn.finance.util.ResponseUtil;

import java.io.IOException;
import java.util.Map;

public class HealthHandler extends BaseHandler {

    public HealthHandler() {
        super(null);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            methodNotAllowed(exchange);
            return;
        }

        ResponseUtil.sendJson(exchange, 200, Map.of("status", "UP"));
    }
}

