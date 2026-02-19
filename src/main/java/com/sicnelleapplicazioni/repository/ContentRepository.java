package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.Content;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Import UUID

public interface ContentRepository {
    void save(Content content);
    Optional<Content> findById(UUID id);
    Optional<Content> findByInternalName(String internalName);
    List<Content> findByUserId(Long userId);
    List<Content> findAll();
    void delete(UUID id);
}