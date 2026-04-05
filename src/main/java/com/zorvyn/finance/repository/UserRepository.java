package com.zorvyn.finance.repository;

import com.zorvyn.finance.db.Database;
import com.zorvyn.finance.model.Role;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.model.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {

    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public Optional<User> findByToken(String token) {
        String sql = """
                SELECT id, name, email, role, status, auth_token, created_at, updated_at
                FROM users
                WHERE auth_token = ?
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch user by token", exception);
        }
    }

    public List<User> findAll() {
        String sql = """
                SELECT id, name, email, role, status, auth_token, created_at, updated_at
                FROM users
                ORDER BY created_at DESC
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch users", exception);
        }
    }

    public Optional<User> findById(UUID id) {
        String sql = """
                SELECT id, name, email, role, status, auth_token, created_at, updated_at
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch user", exception);
        }
    }

    public boolean emailExists(String email, UUID excludeId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)");
        if (excludeId != null) {
            sql.append(" AND id <> ?");
        }

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setString(1, email);
            if (excludeId != null) {
                statement.setObject(2, excludeId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to check email", exception);
        }
    }

    public User create(User user) {
        String sql = """
                INSERT INTO users (id, name, email, role, status, auth_token)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id, name, email, role, status, auth_token, created_at, updated_at
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, user.getId());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getRole().name());
            statement.setString(5, user.getStatus().name());
            statement.setString(6, user.getAuthToken());

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return mapUser(resultSet);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to create user", exception);
        }
    }

    public User update(User user) {
        String sql = """
                UPDATE users
                SET name = ?, email = ?, role = ?, status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                RETURNING id, name, email, role, status, auth_token, created_at, updated_at
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getRole().name());
            statement.setString(4, user.getStatus().name());
            statement.setObject(5, user.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return mapUser(resultSet);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to update user", exception);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("name"),
                resultSet.getString("email"),
                Role.from(resultSet.getString("role")),
                UserStatus.from(resultSet.getString("status")),
                resultSet.getString("auth_token"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
