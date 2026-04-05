package com.zorvyn.finance.handler;

import com.sun.net.httpserver.HttpExchange;
import com.zorvyn.finance.dto.CreateUserRequest;
import com.zorvyn.finance.dto.UpdateUserRequest;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.security.Permission;
import com.zorvyn.finance.service.FinanceFacade;
import com.zorvyn.finance.util.JsonUtil;
import com.zorvyn.finance.util.RequestUtil;
import com.zorvyn.finance.util.ResponseUtil;

import java.io.IOException;
import java.util.UUID;

public class UserHandler extends BaseHandler {

    public UserHandler(FinanceFacade financeFacade) {
        super(financeFacade);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        User currentUser = authenticatedUser(exchange);
        requirePermission(currentUser, Permission.MANAGE_USERS);

        String method = exchange.getRequestMethod();
        String pathValue = RequestUtil.pathValue(exchange.getRequestURI().getPath(), "/api/users");

        if (pathValue.contains("/")) {
            routeNotFound();
            return;
        }

        if (pathValue.isBlank()) {
            if ("GET".equalsIgnoreCase(method)) {
                ResponseUtil.sendJson(exchange, 200, financeFacade().userService().getUsers());
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                CreateUserRequest request = JsonUtil.read(exchange.getRequestBody(), CreateUserRequest.class);
                ResponseUtil.sendJson(exchange, 201, financeFacade().userService().createUser(request));
                return;
            }

            methodNotAllowed(exchange);
            return;
        }

        UUID userId = RequestUtil.readUuid(pathValue, "userId");

        if ("GET".equalsIgnoreCase(method)) {
            ResponseUtil.sendJson(exchange, 200, financeFacade().userService().getUser(userId));
            return;
        }

        if ("PATCH".equalsIgnoreCase(method)) {
            UpdateUserRequest request = JsonUtil.read(exchange.getRequestBody(), UpdateUserRequest.class);
            ResponseUtil.sendJson(exchange, 200, financeFacade().userService().updateUser(userId, request));
            return;
        }

        methodNotAllowed(exchange);
    }
}

