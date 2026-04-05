package com.zorvyn.finance.model;

public enum UserStatus {
    ACTIVE,
    INACTIVE;

    public static UserStatus from(String value) {
        return UserStatus.valueOf(value.trim().toUpperCase());
    }
}

