package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.Content;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Import UUID

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
        Content content = new Content(UUID.randomUUID(), 1L, "test.txt", UUID.randomUUID().toString(), "text/plain", 1024, "/path/to/file", LocalDateTime.now(), "Test content preview");

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        // For UUIDs, generatedKeys might not be used if ID is pre-generated
        // For simplicity, we won't mock generatedKeys for save, as save returns void now.

        jdbcContentRepository.save(content); // save returns void

        // Verifiche comportamentali
        verify(mockPreparedStatement).setString(eq(1), anyString()); // ID
        verify(mockPreparedStatement).setLong(eq(2), anyLong()); // UserId
        verify(mockPreparedStatement).setString(eq(3), anyString()); // OriginalName
        verify(mockPreparedStatement).setString(eq(4), anyString()); // InternalName
        verify(mockPreparedStatement).setString(eq(5), anyString()); // MimeType
        verify(mockPreparedStatement).setLong(eq(6), anyLong()); // Size
        verify(mockPreparedStatement).setString(eq(7), anyString()); // FilePath
        verify(mockPreparedStatement).setTimestamp(eq(8), any(Timestamp.class)); // CreatedAt

        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection).close();
    }

    @Test
    void testFindById_Success() throws SQLException {
        UUID contentId = UUID.randomUUID();
        String internalName = UUID.randomUUID().toString();
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getString("id")).thenReturn(contentId.toString());
        when(mockResultSet.getLong("user_id")).thenReturn(101L);
        when(mockResultSet.getString("original_name")).thenReturn("original.txt");
        when(mockResultSet.getString("internal_name")).thenReturn(internalName);
        when(mockResultSet.getString("mime_type")).thenReturn("text/plain");
        when(mockResultSet.getLong("file_size")).thenReturn(1024L);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/file");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getString("contentText")).thenReturn("Content Preview"); // Mock contentText

        Optional<Content> foundContent = jdbcContentRepository.findById(contentId);

        assertTrue(foundContent.isPresent());
        assertEquals(contentId, foundContent.get().getId());
        verify(mockPreparedStatement).setString(1, contentId.toString());
        verify(mockConnection).close();
    }


    @Test
    void testFindByInternalName_Success() throws SQLException {
        UUID contentId = UUID.randomUUID();
        String internalName = UUID.randomUUID().toString() + ".txt";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getString("id")).thenReturn(contentId.toString());
        when(mockResultSet.getLong("user_id")).thenReturn(101L);
        when(mockResultSet.getString("original_name")).thenReturn("original.txt");
        when(mockResultSet.getString("internal_name")).thenReturn(internalName);
        when(mockResultSet.getString("mime_type")).thenReturn("text/plain");
        when(mockResultSet.getLong("file_size")).thenReturn(1024L);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/file");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getString("contentText")).thenReturn("Content Preview");

        Optional<Content> foundContent = jdbcContentRepository.findByInternalName(internalName); // Changed method name

        assertTrue(foundContent.isPresent());
        assertEquals(internalName, foundContent.get().getInternalName()); // Changed getter
        verify(mockPreparedStatement).setString(1, internalName);
        verify(mockConnection).close();
    }

    @Test
    void testDelete_Success() throws SQLException {
        UUID contentId = UUID.randomUUID();
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        jdbcContentRepository.delete(contentId); // Changed method name and parameter type

        verify(mockPreparedStatement).setString(1, contentId.toString());
        verify(mockConnection).close();
    }
}
