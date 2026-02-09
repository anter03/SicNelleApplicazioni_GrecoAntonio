package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.Content;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Import UUID

public interface ContentRepository {
    void save(Content content); // Updated to void
    Optional<Content> findById(UUID id); // Changed parameter type
    Optional<Content> findByInternalName(String internalName); // Renamed from findByStoredFilename
    List<Content> findByUserId(Long userId); // Changed parameter type
    List<Content> findAll(); // Still needed for general listing
    void delete(UUID id); // Renamed from deleteById, changed parameter type
}