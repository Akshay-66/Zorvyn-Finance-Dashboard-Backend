package com.zorvyn.finance.security;

import com.zorvyn.finance.exception.ApiException;
import com.zorvyn.finance.model.Role;
import com.zorvyn.finance.model.User;

import java.util.EnumMap;
import java.util.Map;

public class AccessManager {

    private final Map<Role, AccessStrategy> strategies = new EnumMap<>(Role.class);

    public AccessManager() {
        strategies.put(Role.VIEWER, new ViewerAccessStrategy());
        strategies.put(Role.ANALYST, new AnalystAccessStrategy());
        strategies.put(Role.ADMIN, new AdminAccessStrategy());
    }

    public void check(User user, Permission permission) {
        AccessStrategy strategy = strategies.get(user.getRole());
        if (strategy == null || !strategy.allows(permission)) {
            throw new ApiException(403, "You do not have permission to do this action");
        }
    }
}
