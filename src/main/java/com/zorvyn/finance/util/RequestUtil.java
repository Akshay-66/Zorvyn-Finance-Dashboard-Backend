package com.zorvyn.finance.util;

import com.zorvyn.finance.exception.ApiException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RequestUtil {

    private RequestUtil() {
    }

    public static Map<String, String> queryParams(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return params;
        }

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            params.put(key, value);
        }

        return params;
    }

    public static UUID readUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (Exception exception) {
            throw new ApiException(400, fieldName + " must be a valid UUID");
        }
    }

    public static LocalDate readDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value);
        } catch (Exception exception) {
            throw new ApiException(400, fieldName + " must be in yyyy-MM-dd format");
        }
    }

    public static int readInt(String value, String fieldName, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ApiException(400, fieldName + " must be a number");
        }
    }

    public static String pathValue(String fullPath, String basePath) {
        if (!fullPath.startsWith(basePath)) {
            return "";
        }

        String remaining = fullPath.substring(basePath.length());
        if (remaining.startsWith("/")) {
            remaining = remaining.substring(1);
        }
        if (remaining.endsWith("/")) {
            remaining = remaining.substring(0, remaining.length() - 1);
        }
        return remaining;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}

