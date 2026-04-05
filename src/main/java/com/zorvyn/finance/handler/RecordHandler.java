package com.zorvyn.finance.handler;

import com.sun.net.httpserver.HttpExchange;
import com.zorvyn.finance.dto.CreateRecordRequest;
import com.zorvyn.finance.dto.RecordFilter;
import com.zorvyn.finance.dto.UpdateRecordRequest;
import com.zorvyn.finance.exception.ApiException;
import com.zorvyn.finance.model.RecordType;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.security.Permission;
import com.zorvyn.finance.service.FinanceFacade;
import com.zorvyn.finance.util.JsonUtil;
import com.zorvyn.finance.util.RequestUtil;
import com.zorvyn.finance.util.ResponseUtil;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class RecordHandler extends BaseHandler {

    public RecordHandler(FinanceFacade financeFacade) {
        super(financeFacade);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        User currentUser = authenticatedUser(exchange);
        String method = exchange.getRequestMethod();
        String pathValue = RequestUtil.pathValue(exchange.getRequestURI().getPath(), "/api/records");

        if (pathValue.contains("/")) {
            routeNotFound();
            return;
        }

        if (pathValue.isBlank()) {
            if ("GET".equalsIgnoreCase(method)) {
                requirePermission(currentUser, Permission.VIEW_RECORDS);
                RecordFilter filter = buildFilter(exchange);
                ResponseUtil.sendJson(exchange, 200, financeFacade().recordService().getRecords(filter));
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                requirePermission(currentUser, Permission.MANAGE_RECORDS);
                CreateRecordRequest request = JsonUtil.read(exchange.getRequestBody(), CreateRecordRequest.class);
                ResponseUtil.sendJson(exchange, 201, financeFacade().recordService().createRecord(request, currentUser));
                return;
            }

            methodNotAllowed(exchange);
            return;
        }

        UUID recordId = RequestUtil.readUuid(pathValue, "recordId");

        if ("GET".equalsIgnoreCase(method)) {
            requirePermission(currentUser, Permission.VIEW_RECORDS);
            ResponseUtil.sendJson(exchange, 200, financeFacade().recordService().getRecord(recordId));
            return;
        }

        if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            requirePermission(currentUser, Permission.MANAGE_RECORDS);
            UpdateRecordRequest request = JsonUtil.read(exchange.getRequestBody(), UpdateRecordRequest.class);
            ResponseUtil.sendJson(exchange, 200, financeFacade().recordService().updateRecord(recordId, request));
            return;
        }

        if ("DELETE".equalsIgnoreCase(method)) {
            requirePermission(currentUser, Permission.MANAGE_RECORDS);
            financeFacade().recordService().deleteRecord(recordId);
            ResponseUtil.sendJson(exchange, 200, Map.of("message", "Record deleted successfully"));
            return;
        }

        methodNotAllowed(exchange);
    }

    private RecordFilter buildFilter(HttpExchange exchange) {
        Map<String, String> queryParams = RequestUtil.queryParams(exchange.getRequestURI().getRawQuery());
        RecordFilter filter = new RecordFilter();

        if (queryParams.containsKey("type") && !queryParams.get("type").isBlank()) {
            filter.setType(parseType(queryParams.get("type")));
        }

        if (queryParams.containsKey("category")) {
            filter.setCategory(queryParams.get("category"));
        }

        if (queryParams.containsKey("search")) {
            filter.setSearch(queryParams.get("search"));
        }

        if (queryParams.containsKey("fromDate") && !queryParams.get("fromDate").isBlank()) {
            filter.setFromDate(RequestUtil.readDate(queryParams.get("fromDate"), "fromDate"));
        }

        if (queryParams.containsKey("toDate") && !queryParams.get("toDate").isBlank()) {
            filter.setToDate(RequestUtil.readDate(queryParams.get("toDate"), "toDate"));
        }

        filter.setPage(RequestUtil.readInt(queryParams.get("page"), "page", 1));
        filter.setSize(RequestUtil.readInt(queryParams.get("size"), "size", 10));
        return filter;
    }

    private RecordType parseType(String value) {
        try {
            return RecordType.from(value);
        } catch (Exception exception) {
            throw new ApiException(400, "type must be INCOME or EXPENSE");
        }
    }
}
