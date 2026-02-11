package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.Content;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Import UUID

public class JdbcContentRepository implements ContentRepository {

     private static final String DB_URL = "jdbc:sqlserver://;serverName=localhost\\SQLEXPRESS;databaseName=sicurezzaNelleApplicazioni;trustServerCertificate=true";
     private static final String DB_USER = "sa";
     private static final String DB_PASSWORD = "SqlServerMio160625";
    //private static final String DB_PASSWORD = "root_password";
    //private static final String DB_URL = "jdbc:mysql://localhost:3306/sic_db?useSSL=false&allowPublicKeyRetrieval=true";
    //private static final String DB_USER = "root";

    protected Connection getConnection() throws SQLException {
        try {
            //Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    @Override
    public synchronized void save(Content content) { // Return type changed to void
        String sql = "INSERT INTO contents (id, user_id, original_name, internal_name, mime_type, file_size, file_path, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, content.getId().toString()); // Set UUID as String
            pstmt.setLong(2, content.getUserId()); // Changed to Long
            pstmt.setString(3, content.getOriginalName());
            pstmt.setString(4, content.getInternalName());
            pstmt.setString(5, content.getMimeType());
            pstmt.setLong(6, content.getSize());
            pstmt.setString(7, content.getFilePath());
            pstmt.setTimestamp(8, Timestamp.valueOf(content.getCreatedAt()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating content failed, no rows affected.");
            }
            // No need to retrieve generated keys for UUID, as it's generated in Content constructor
        } catch (SQLException e) {
            System.err.println("Error saving content: " + e.getMessage());
            throw new RuntimeException("Error saving content", e);
        }
    }

    @Override
    public synchronized Optional<Content> findById(UUID id) { // Parameter type changed to UUID
        String sql = "SELECT c.id, c.user_id, c.original_name, c.internal_name, c.mime_type, c.file_size, c.file_path, c.created_at, u.username " +
                     "FROM contents c JOIN users u ON c.user_id = u.id WHERE c.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id.toString()); // Set UUID as String
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
    public synchronized Optional<Content> findByInternalName(String internalName) { // Renamed method
        String sql = "SELECT c.id, c.user_id, c.original_name, c.internal_name, c.mime_type, c.file_size, c.file_path, c.created_at, u.username " +
                     "FROM contents c JOIN users u ON c.user_id = u.id WHERE c.internal_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, internalName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToContent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding content by internal name: " + e.getMessage());
            throw new RuntimeException("Error finding content by internal name", e);
        }
        return Optional.empty();
    }

    @Override
    public synchronized List<Content> findByUserId(Long userId) { // Parameter type changed to Long
        String sql = "SELECT c.id, c.user_id, c.original_name, c.internal_name, c.mime_type, c.file_size, c.file_path, c.created_at, u.username " +
                     "FROM contents c JOIN users u ON c.user_id = u.id WHERE c.user_id = ? ORDER BY c.created_at DESC";
        List<Content> contentList = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId); // Set Long
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
    public synchronized List<Content> findAll() {
        String sql = "SELECT c.id, c.user_id, c.original_name, c.internal_name, c.mime_type, c.file_size, c.file_path, c.created_at, u.username " +
                     "FROM contents c JOIN users u ON c.user_id = u.id ORDER BY c.created_at DESC";
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
    public synchronized void delete(UUID id) { // Renamed method, changed parameter type
        String sql = "DELETE FROM contents WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id.toString()); // Set UUID as String
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting content by ID: " + e.getMessage());
            throw new RuntimeException("Error deleting content by ID", e);
        }
    }

    private Content mapResultSetToContent(ResultSet rs) throws SQLException {
        Content content = new Content();
        content.setId(UUID.fromString(rs.getString("id"))); // Get UUID from String
        content.setUserId(rs.getLong("user_id")); // Changed to getLong
        content.setOriginalName(rs.getString("original_name"));
        content.setInternalName(rs.getString("internal_name"));
        content.setMimeType(rs.getString("mime_type"));
        content.setSize(rs.getLong("file_size"));
        content.setFilePath(rs.getString("file_path"));
        content.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getMetaData().getColumnCount() > 8) { // If username is present in the result set
            content.setAuthorUsername(rs.getString("username"));
        }
        return content;
    }
}
