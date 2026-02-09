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

        // Inizializziamo il repository sovrascrivendo il metodo di connessione
        // se il repository non accetta la connessione nel costruttore.
        jdbcContentRepository = new JdbcContentRepository() {
            @Override
            protected Connection getConnection() throws SQLException {
                return mockConnection;
            }
        };

        // Configurazione base dei mock per evitare ripetizioni nei test
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
        when(mockResultSet.getLong(1)).thenReturn(100L);

        Content savedContent = jdbcContentRepository.save(content);

        assertNotNull(savedContent.getId());
        assertEquals(100L, savedContent.getId());

        // Verifiche comportamentali
        verify(mockPreparedStatement).setLong(eq(1), anyLong());
        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection).close();
    }



    @Test
    void testFindByStoredFilename_Success() throws SQLException {
        String storedFilename = "uuid-filename.txt";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        // Setup dei dati nel ResultSet
        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getLong("user_id")).thenReturn(101L);
        when(mockResultSet.getString("filename")).thenReturn("original.txt");
        when(mockResultSet.getString("stored_filename")).thenReturn(storedFilename);
        when(mockResultSet.getTimestamp("upload_time")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        Optional<Content> foundContent = jdbcContentRepository.findByStoredFilename(storedFilename);

        assertTrue(foundContent.isPresent());
        assertEquals(storedFilename, foundContent.get().getStoredFilename());
        verify(mockPreparedStatement).setString(1, storedFilename);
        verify(mockConnection).close();
    }

    @Test
    void testDeleteById_Success() throws SQLException {
        Long contentId = 1L;
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        jdbcContentRepository.deleteById(contentId);

        verify(mockPreparedStatement).setLong(1, contentId);
        verify(mockConnection).close();
    }
}