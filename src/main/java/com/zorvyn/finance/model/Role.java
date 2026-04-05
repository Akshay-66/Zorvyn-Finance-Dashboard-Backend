package com.zorvyn.finance.model;

public enum Role {
    VIEWER,
    ANALYST,
    ADMIN;

    public static Role from(String value) {
        return Role.valueOf(value.trim().toUpperCase());
    }
}

