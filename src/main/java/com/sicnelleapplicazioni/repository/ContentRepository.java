package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.Content;
import java.util.List;
import java.util.Optional;

public interface ContentRepository {
    Content save(Content content);
    Optional<Content> findById(Long id);
    Optional<Content> findByStoredFilename(String storedFilename); // New method
    List<Content> findByUserId(Long userId);
    List<Content> findAll();
    void deleteById(Long id);
}
