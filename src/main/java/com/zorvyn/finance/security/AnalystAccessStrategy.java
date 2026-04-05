package com.zorvyn.finance.security;

public class AnalystAccessStrategy implements AccessStrategy {

    @Override
    public boolean allows(Permission permission) {
        return permission == Permission.VIEW_SUMMARY || permission == Permission.VIEW_RECORDS;
    }
}

