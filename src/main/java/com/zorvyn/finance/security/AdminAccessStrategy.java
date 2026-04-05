package com.zorvyn.finance.security;

public class AdminAccessStrategy implements AccessStrategy {

    @Override
    public boolean allows(Permission permission) {
        return true;
    }
}

