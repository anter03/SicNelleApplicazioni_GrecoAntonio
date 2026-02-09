package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.User;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    private static final String DB_URL = "jdbc:sqlserver://;serverName=localhost\\SQLEXPRESS;databaseName=sicurezzaNelleApplicazioni;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "SqlServerMio160625";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, salt, full_name, failed_attempts, lockout_until) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getFullName());
            pstmt.setInt(6, user.getFailedAttempts());
            pstmt.setTimestamp(7, user.getLockoutUntil() != null ? Timestamp.from(user.getLockoutUntil()) : null);

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

    private Optional<User> findUser(String query, String identifier) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, identifier);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setSalt(rs.getString("salt"));
                    user.setFullName(rs.getString("full_name"));
                    user.setFailedAttempts(rs.getInt("failed_attempts"));
                    Timestamp lockoutUntilTimestamp = rs.getTimestamp("lockout_until");
                    if (lockoutUntilTimestamp != null) {
                        user.setLockoutUntil(lockoutUntilTimestamp.toInstant());
                    }
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by identifier: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash, salt, full_name, failed_attempts, lockout_until FROM users WHERE username = ?";
        return findUser(sql, username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, username, email, password_hash, salt, full_name, failed_attempts, lockout_until FROM users WHERE email = ?";
        return findUser(sql, email);
    }

    private void updateUserAttemptsAndLockout(String sql, String identifier) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (sql.contains("lockout_until = ?")) { // For lockAccount
                Timestamp lockoutTime = Timestamp.from(Instant.now().plusSeconds(30 * 60)); // Lock for 30 minutes
                pstmt.setTimestamp(1, lockoutTime);
                pstmt.setString(2, identifier);
            } else {
                pstmt.setString(1, identifier);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user attempts/lockout for identifier " + identifier + ": " + e.getMessage());
        }
    }

    @Override
    public void incrementFailedAttempts(String identifier) {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE username = ? OR email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identifier);
            pstmt.setString(2, identifier);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error incrementing failed attempts for user " + identifier + ": " + e.getMessage());
        }
    }

    @Override
    public void resetFailedAttempts(String identifier) {
        String sql = "UPDATE users SET failed_attempts = 0, lockout_until = NULL WHERE username = ? OR email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identifier);
            pstmt.setString(2, identifier);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error resetting failed attempts for user " + identifier + ": " + e.getMessage());
        }
    }

    @Override
    public void lockAccount(String identifier) {
        String sql = "UPDATE users SET lockout_until = ? WHERE username = ? OR email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Timestamp lockoutTime = Timestamp.from(Instant.now().plusSeconds(30 * 60)); // Lock for 30 minutes
            pstmt.setTimestamp(1, lockoutTime);
            pstmt.setString(2, identifier);
            pstmt.setString(3, identifier);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error locking account for user " + identifier + ": " + e.getMessage());
        }
    }

    @Override
    public void unlockAccount(String identifier) {
        String sql = "UPDATE users SET lockout_until = NULL, failed_attempts = 0 WHERE username = ? OR email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identifier);
            pstmt.setString(2, identifier);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error unlocking account for user " + identifier + ": " + e.getMessage());
        }
    }
}