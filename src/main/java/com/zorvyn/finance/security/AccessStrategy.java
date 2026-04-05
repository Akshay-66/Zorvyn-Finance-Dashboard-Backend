package com.zorvyn.finance.security;

public interface AccessStrategy {

    boolean allows(Permission permission);
}

