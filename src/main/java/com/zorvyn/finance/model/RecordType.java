package com.zorvyn.finance.model;

public enum RecordType {
    INCOME,
    EXPENSE;

    public static RecordType from(String value) {
        return RecordType.valueOf(value.trim().toUpperCase());
    }
}

