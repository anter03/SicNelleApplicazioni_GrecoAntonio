package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.Content;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList; // Import ArrayList
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JdbcContentRepository implements ContentRepository {

    // Placeholder for database connection details - these should be loaded securely in a real application.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sic_db?useSSL=true&requireSSL=true";
    private static final String DB_USER = "sic_user";
    private static final String DB_PASSWORD = "PasswordSicura123!";

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    @Override
    public Content save(Content content) {
        String sql = "INSERT INTO content (user_id, filename, stored_filename, upload_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, content.getUserId());
            pstmt.setString(2, content.getFilename());
            pstmt.setString(3, content.getStoredFilename()); // Store stored_filename
            pstmt.setTimestamp(4, Timestamp.valueOf(content.getUploadTime()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating content failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    content.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating content failed, no ID obtained.");
                }
            }
            return content;

        } catch (SQLException e) {
            System.err.println("Error saving content: " + e.getMessage());
            throw new RuntimeException("Error saving content", e);
        }
    }

    @Override
    public Optional<Content> findById(Long id) {
        String sql = "SELECT id, user_id, filename, stored_filename, upload_time FROM content WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToContent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding content by ID: " + e.getMessage());
            throw new RuntimeException("Error finding content by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Content> findByStoredFilename(String storedFilename) {
        String sql = "SELECT id, user_id, filename, stored_filename, upload_time FROM content WHERE stored_filename = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, storedFilename);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToContent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding content by stored filename: " + e.getMessage());
            throw new RuntimeException("Error finding content by stored filename", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Content> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, filename, stored_filename, upload_time FROM content WHERE user_id = ?";
        List<Content> contentList = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    contentList.add(mapResultSetToContent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding content by user ID: " + e.getMessage());
            throw new RuntimeException("Error finding content by user ID", e);
        }
        return contentList;
    }

    @Override
    public List<Content> findAll() {
        String sql = "SELECT id, user_id, filename, stored_filename, upload_time FROM content";
        List<Content> contentList = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                contentList.add(mapResultSetToContent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all content: " + e.getMessage());
            throw new RuntimeException("Error finding all content", e);
        }
        return contentList;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM content WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting content by ID: " + e.getMessage());
            throw new RuntimeException("Error deleting content by ID", e);
        }
    }

    private Content mapResultSetToContent(ResultSet rs) throws SQLException {
        Content content = new Content();
        content.setId(rs.getLong("id"));
        content.setUserId(rs.getLong("user_id"));
        content.setFilename(rs.getString("filename"));
        content.setStoredFilename(rs.getString("stored_filename"));
        content.setUploadTime(rs.getTimestamp("upload_time").toLocalDateTime());
        return content;
    }
}
