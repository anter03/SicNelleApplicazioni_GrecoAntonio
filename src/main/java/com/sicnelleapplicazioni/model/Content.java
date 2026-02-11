package com.sicnelleapplicazioni.model;

import java.time.LocalDateTime;
import java.util.UUID; // Import UUID

public class Content {
    private UUID id;
    private Long userId; // Changed from UUID to Long
    private String originalName;
    private String internalName;
    private String mimeType;
    private long size;
    private String filePath;
    private LocalDateTime createdAt;
    private String contentText; // Re-introduced for display purposes
    private String authorUsername;

    public Content() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    public Content(UUID id, Long userId, String originalName, String internalName, String mimeType, long size, String filePath, LocalDateTime createdAt, String contentText, String authorUsername) {
        this.id = id;
        this.userId = userId;
        this.originalName = originalName;
        this.internalName = internalName;
        this.mimeType = mimeType;
        this.size = size;
        this.filePath = filePath;
        this.createdAt = createdAt;
        this.contentText = contentText;
        this.authorUsername = authorUsername;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public UUID getId() {
        return id;
    }


    public void setId(UUID id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }
}