package com.zorvyn.finance.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zorvyn.finance.dto.ApiError;
import com.zorvyn.finance.exception.ApiException;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.security.Permission;
import com.zorvyn.finance.service.FinanceFacade;
import com.zorvyn.finance.util.ResponseUtil;

import java.io.IOException;

public abstract class BaseHandler implements HttpHandler {

    private final FinanceFacade financeFacade;

    protected BaseHandler(FinanceFacade financeFacade) {
        this.financeFacade = financeFacade;
    }

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        ResponseUtil.addCommonHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            ResponseUtil.sendNoContent(exchange);
            return;
        }

        try {
            handleRequest(exchange);
        } catch (ApiException exception) {
            ResponseUtil.sendJson(
                    exchange,
                    exception.getStatusCode(),
                    new ApiError(exception.getStatusCode(), exception.getMessage())
            );
        } catch (Exception exception) {
            exception.printStackTrace();
            ResponseUtil.sendJson(exchange, 500, new ApiError(500, "Internal server error"));
        }
    }

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    protected User authenticatedUser(HttpExchange exchange) {
        if (financeFacade == null) {
            throw new IllegalStateException("FinanceFacade is not available");
        }

        String token = exchange.getRequestHeaders().getFirst("X-Auth-Token");
        return financeFacade.authService().authenticate(token);
    }

    protected void requirePermission(User user, Permission permission) {
        if (financeFacade == null) {
            throw new IllegalStateException("FinanceFacade is not available");
        }

        financeFacade.accessManager().check(user, permission);
    }

    protected FinanceFacade financeFacade() {
        return financeFacade;
    }

    protected void methodNotAllowed(HttpExchange exchange) {
        throw new ApiException(405, "Method not allowed");
    }

    protected void routeNotFound() {
        throw new ApiException(404, "Route not found");
    }
}

