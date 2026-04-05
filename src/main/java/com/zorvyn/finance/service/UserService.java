package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.CreateUserRequest;
import com.zorvyn.finance.dto.UpdateUserRequest;
import com.zorvyn.finance.exception.ApiException;
import com.zorvyn.finance.model.Role;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.model.UserStatus;
import com.zorvyn.finance.repository.UserRepository;

import java.util.List;
import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "User not found"));
    }

    public User createUser(CreateUserRequest request) {
        validateCreateRequest(request);
        String email = request.getEmail().trim().toLowerCase();
        String token = request.getToken() == null || request.getToken().isBlank()
                ? UUID.randomUUID().toString()
                : request.getToken().trim();

        if (userRepository.emailExists(email, null)) {
            throw new ApiException(409, "Email already exists");
        }

        if (userRepository.findByToken(token).isPresent()) {
            throw new ApiException(409, "Token already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setRole(parseRole(request.getRole()));
        user.setStatus(request.getStatus() == null || request.getStatus().isBlank()
                ? UserStatus.ACTIVE
                : parseStatus(request.getStatus()));
        user.setAuthToken(token);

        return userRepository.create(user);
    }

    public User updateUser(UUID id, UpdateUserRequest request) {
        if (request == null) {
            throw new ApiException(400, "Request body is required");
        }

        User existingUser = getUser(id);

        if (request.getName() != null && request.getName().isBlank()) {
            throw new ApiException(400, "name cannot be blank");
        }

        if (request.getEmail() != null && request.getEmail().isBlank()) {
            throw new ApiException(400, "email cannot be blank");
        }

        String updatedEmail = request.getEmail() == null
                ? existingUser.getEmail()
                : request.getEmail().trim().toLowerCase();

        if (!isValidEmail(updatedEmail)) {
            throw new ApiException(400, "email is invalid");
        }

        if (userRepository.emailExists(updatedEmail, id)) {
            throw new ApiException(409, "Email already exists");
        }

        existingUser.setName(request.getName() == null ? existingUser.getName() : request.getName().trim());
        existingUser.setEmail(updatedEmail);
        existingUser.setRole(request.getRole() == null ? existingUser.getRole() : parseRole(request.getRole()));
        existingUser.setStatus(request.getStatus() == null ? existingUser.getStatus() : parseStatus(request.getStatus()));

        return userRepository.update(existingUser);
    }

    private void validateCreateRequest(CreateUserRequest request) {
        if (request == null) {
            throw new ApiException(400, "Request body is required");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ApiException(400, "name is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ApiException(400, "email is required");
        }

        if (!isValidEmail(request.getEmail().trim())) {
            throw new ApiException(400, "email is invalid");
        }

        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new ApiException(400, "role is required");
        }

        parseRole(request.getRole());

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            parseStatus(request.getStatus());
        }
    }

    private Role parseRole(String value) {
        try {
            return Role.from(value);
        } catch (Exception exception) {
            throw new ApiException(400, "role must be VIEWER, ANALYST, or ADMIN");
        }
    }

    private UserStatus parseStatus(String value) {
        try {
            return UserStatus.from(value);
        } catch (Exception exception) {
            throw new ApiException(400, "status must be ACTIVE or INACTIVE");
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}
