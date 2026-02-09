package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.Content;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.sql.Timestamp; // Import Timestamp

import java.util.Optional;
import java.util.List; // Import List
import java.util.ArrayList; // Import ArrayList


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class JdbcContentRepositoryTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private JdbcContentRepository jdbcContentRepository;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        jdbcContentRepository = new JdbcContentRepository() {
            // Override getConnection to return the mock connection
            @Override
            protected Connection getConnection() throws SQLException {
                return mockConnection;
            }
        };

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
    }

    @Test
    void testSave_Success() throws SQLException {
        Content content = new Content(null, 1L, "test.txt", "uuid-filename.txt", "Test content", LocalDateTime.now());

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(100L); // Simulate generated ID

        Content savedContent = jdbcContentRepository.save(content);

        assertNotNull(savedContent.getId());
        assertEquals(100L, savedContent.getId());
        verify(mockPreparedStatement, times(1)).setLong(1, content.getUserId());
        verify(mockPreparedStatement, times(1)).setString(2, content.getFilename());
        verify(mockPreparedStatement, times(1)).setString(3, content.getStoredFilename());
        verify(mockPreparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(content.getUploadTime()));
        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void testSave_NoRowsAffected() throws SQLException {
        Content content = new Content(null, 1L, "test.txt", "uuid-filename.txt", "Test content", LocalDateTime.now());

        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> jdbcContentRepository.save(content));
        assertTrue(thrown.getMessage().contains("Creating content failed, no rows affected."));

        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, never()).next(); // Should not try to get generated keys
    }

    @Test
    void testSave_NoIdObtained() throws SQLException {
        Content content = new Content(null, 1L, "test.txt", "uuid-filename.txt", "Test content", LocalDateTime.now());

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(false); // No generated ID

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> jdbcContentRepository.save(content));
        assertTrue(thrown.getMessage().contains("Creating content failed, no ID obtained."));

        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockResultSet, times(1)).next();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void testFindByStoredFilename_Success() throws SQLException {
        String storedFilename = "uuid-filename.txt";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getLong("user_id")).thenReturn(101L);
        when(mockResultSet.getString("filename")).thenReturn("original.txt");
        when(mockResultSet.getString("stored_filename")).thenReturn(storedFilename);
        when(mockResultSet.getTimestamp("upload_time")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        Optional<Content> foundContent = jdbcContentRepository.findByStoredFilename(storedFilename);

        assertTrue(foundContent.isPresent());
        assertEquals(storedFilename, foundContent.get().getStoredFilename());
        verify(mockPreparedStatement, times(1)).setString(1, storedFilename);
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void testFindByUserId_Success() throws SQLException {
        Long userId = 101L;
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Two results
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(userId, userId);
        when(mockResultSet.getString("filename")).thenReturn("file1.txt", "file2.txt");
        when(mockResultSet.getString("stored_filename")).thenReturn("uuid1.txt", "uuid2.txt");
        when(mockResultSet.getTimestamp("upload_time")).thenReturn(Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));

        List<Content> contentList = jdbcContentRepository.findByUserId(userId);

        assertFalse(contentList.isEmpty());
        assertEquals(2, contentList.size());
        assertEquals(userId, contentList.get(0).getUserId());
        assertEquals(userId, contentList.get(1).getUserId());
        verify(mockPreparedStatement, times(1)).setLong(1, userId);
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(3)).next(); // 2 true, 1 false
        verify(mockResultSet, times(2)).getLong("id");
    }

    @Test
    void testFindAll_Success() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // One result
        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getLong("user_id")).thenReturn(101L);
        when(mockResultSet.getString("filename")).thenReturn("file1.txt");
        when(mockResultSet.getString("stored_filename")).thenReturn("uuid1.txt");
        when(mockResultSet.getTimestamp("upload_time")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        List<Content> contentList = jdbcContentRepository.findAll();

        assertFalse(contentList.isEmpty());
        assertEquals(1, contentList.size());
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
        verify(mockResultSet, times(2)).next();
    }

    @Test
    void testDeleteById_Success() throws SQLException {
        Long contentId = 1L;
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        jdbcContentRepository.deleteById(contentId);

        verify(mockPreparedStatement, times(1)).setLong(1, contentId);
        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockConnection, times(1)).close();
        verify(mockPreparedStatement, times(1)).close();
    }
}
