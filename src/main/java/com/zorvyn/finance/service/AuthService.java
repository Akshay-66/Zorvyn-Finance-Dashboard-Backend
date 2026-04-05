package com.zorvyn.finance.service;

import com.zorvyn.finance.exception.ApiException;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.model.UserStatus;
import com.zorvyn.finance.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException(401, "X-Auth-Token header is required");
        }

        User user = userRepository.findByToken(token.trim())
                .orElseThrow(() -> new ApiException(401, "Invalid auth token"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(403, "This user is inactive");
        }

        return user;
    }
}

