package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.Content;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class JdbcContentRepositoryTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private ResultSetMetaData mockResultSetMetaData;

    private JdbcContentRepository jdbcContentRepository;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        jdbcContentRepository = new JdbcContentRepository() {
            @Override
            protected Connection getConnection() throws SQLException {
                return mockConnection;
            }
        };

        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockResultSetMetaData);
    }

    @Test
    void testSave_Success() throws SQLException {
        Content content = new Content(UUID.randomUUID(), 1L, "test.txt", UUID.randomUUID().toString(), "text/plain", 1024, "/path/to/file", LocalDateTime.now(), "Test content preview", "testuser");

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        jdbcContentRepository.save(content);

        verify(mockPreparedStatement).setString(1, content.getId().toString());
        verify(mockPreparedStatement).setLong(2, content.getUserId());
        verify(mockPreparedStatement).setString(3, content.getOriginalName());
        verify(mockPreparedStatement).setString(4, content.getInternalName());
        verify(mockPreparedStatement).setString(5, content.getMimeType());
        verify(mockPreparedStatement).setLong(6, content.getSize());
        verify(mockPreparedStatement).setString(7, content.getFilePath());
        verify(mockPreparedStatement).setTimestamp(8, Timestamp.valueOf(content.getCreatedAt()));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testFindById_Success() throws SQLException {
        UUID contentId = UUID.randomUUID();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(9);
        when(mockResultSet.getString("id")).thenReturn(contentId.toString());
        when(mockResultSet.getLong("user_id")).thenReturn(1L);
        when(mockResultSet.getString("original_name")).thenReturn("test.txt");
        when(mockResultSet.getString("internal_name")).thenReturn("internal.txt");
        when(mockResultSet.getString("mime_type")).thenReturn("text/plain");
        when(mockResultSet.getLong("file_size")).thenReturn(1024L);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/file");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getString("username")).thenReturn("testuser");

        Optional<Content> foundContent = jdbcContentRepository.findById(contentId);

        assertTrue(foundContent.isPresent());
        assertEquals(contentId, foundContent.get().getId());
        assertEquals("testuser", foundContent.get().getAuthorUsername());
    }

    @Test
    void testFindByInternalName_Success() throws SQLException {
        String internalName = "internal.txt";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(9);
        when(mockResultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
        when(mockResultSet.getLong("user_id")).thenReturn(1L);
        when(mockResultSet.getString("original_name")).thenReturn("test.txt");
        when(mockResultSet.getString("internal_name")).thenReturn(internalName);
        when(mockResultSet.getString("mime_type")).thenReturn("text/plain");
        when(mockResultSet.getLong("file_size")).thenReturn(1024L);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/file");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getString("username")).thenReturn("testuser");

        Optional<Content> foundContent = jdbcContentRepository.findByInternalName(internalName);

        assertTrue(foundContent.isPresent());
        assertEquals(internalName, foundContent.get().getInternalName());
        assertEquals("testuser", foundContent.get().getAuthorUsername());
    }

    @Test
    void testDelete_Success() throws SQLException {
        UUID contentId = UUID.randomUUID();
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        jdbcContentRepository.delete(contentId);

        verify(mockPreparedStatement).setString(1, contentId.toString());
        verify(mockPreparedStatement).executeUpdate();
    }
}
