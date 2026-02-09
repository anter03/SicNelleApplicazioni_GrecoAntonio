package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.User;

import java.sql.*;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    // Placeholder for database connection details - these should be loaded securely in a real application.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sic_db?useSSL=true&requireSSL=true";
    private static final String DB_USER = "sic_user";
    private static final String DB_PASSWORD = "PasswordSicura123!";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, hashed_password, salt, verification_token, email_verified) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setBytes(2, user.getHashedPassword());
            pstmt.setBytes(3, user.getSalt());
            pstmt.setString(4, user.getVerificationToken());
            pstmt.setBoolean(5, user.isEmailVerified());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            return user;

        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        // TODO: Implement with PreparedStatement
        return Optional.empty();
    }

    @Override
    public void incrementFailedLoginAttempts(String username) {
        // TODO: Implement with PreparedStatement
    }

    @Override
    public void resetFailedLoginAttempts(String username) {
        // TODO: Implement with PreparedStatement
    }

    @Override
    public void lockAccount(String username) {
        // TODO: Implement with PreparedStatement
    }

    @Override
    public void unlockAccount(String username) {
        // TODO: Implement with PreparedStatement
    }

    @Override
    public Optional<User> findByVerificationToken(String token) {
        // TODO: Implement with PreparedStatement
        return Optional.empty();
    }
}
