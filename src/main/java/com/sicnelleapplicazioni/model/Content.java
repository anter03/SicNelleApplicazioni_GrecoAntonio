package com.sicnelleapplicazioni.model;

import java.time.LocalDateTime;

public class Content {
    private Long id;
    private Long userId; // Link to the User who uploaded the content
    private String filename; // Original filename
    private String storedFilename; // UUID filename on disk
    private String contentText; // Re-introduced for display purposes
    private LocalDateTime uploadTime;

    // Constructors
    public Content() {
    }

    public Content(Long id, Long userId, String filename, String storedFilename, String contentText, LocalDateTime uploadTime) {
        this.id = id;
        this.userId = userId;
        this.filename = filename;
        this.storedFilename = storedFilename;
        this.contentText = contentText;
        this.uploadTime = uploadTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getContentText() { // Getter for contentText
        return contentText;
    }

    public void setContentText(String contentText) { // Setter for contentText
        this.contentText = contentText;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
}
