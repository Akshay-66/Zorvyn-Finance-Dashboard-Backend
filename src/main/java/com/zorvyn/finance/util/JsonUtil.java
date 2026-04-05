package com.zorvyn.finance.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zorvyn.finance.exception.ApiException;

import java.io.IOException;
import java.io.InputStream;

public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonUtil() {
    }

    public static <T> T read(InputStream inputStream, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, type);
        } catch (IOException exception) {
            throw new ApiException(400, "Invalid JSON body");
        }
    }

    public static byte[] write(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(value);
        } catch (IOException exception) {
            throw new RuntimeException("Unable to write JSON response", exception);
        }
    }
}

