package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.User;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Level; // Import Level
import java.util.logging.Logger; // Import Logger

public class JdbcUserRepository implements UserRepository {

    private static final Logger LOGGER = Logger.getLogger(JdbcUserRepository.class.getName()); // Add Logger

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

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password_hash = ?, salt = ?, full_name = ?, failed_attempts = ?, lockout_until = ?, last_login = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getFullName());
            pstmt.setInt(6, user.getFailedAttempts());
            pstmt.setTimestamp(7, user.getLockoutUntil() != null ? Timestamp.from(user.getLockoutUntil()) : null);
            pstmt.setTimestamp(8, user.getLastLogin() != null ? Timestamp.from(user.getLastLogin()) : null);
            pstmt.setLong(9, user.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected for user ID: " + user.getId());
            }
            return user;

        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            throw new RuntimeException("Error updating user", e);
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
                    try { // Robust retrieval of last_login
                        Timestamp lastLoginTimestamp = rs.getTimestamp("last_login");
                        if (lastLoginTimestamp != null) {
                            user.setLastLogin(lastLoginTimestamp.toInstant());
                        }
                    } catch (SQLException sqle) {
                        LOGGER.log(Level.WARNING, "Column 'last_login' not found or invalid in ResultSet. This might indicate a schema mismatch. Error: " + sqle.getMessage());
                        user.setLastLogin(null); // Default to null if column is missing
                    }
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by identifier: " + e.getMessage(), e); // Log full stack trace
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash, salt, full_name, failed_attempts, lockout_until, last_login FROM users WHERE username = ? COLLATE Latin1_General_CI_AS";
        return findUser(sql, username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, username, email, password_hash, salt, full_name, failed_attempts, lockout_until, last_login FROM users WHERE email = ? COLLATE Latin1_General_CI_AS";
        return findUser(sql, email);
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
        String sql = "UPDATE users SET failed_attempts = 0, lockout_until = NULL, last_login = ? WHERE username = ? OR email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.from(Instant.now())); // Set current time for last_login on successful reset
            pstmt.setString(2, identifier);
            pstmt.setString(3, identifier);
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