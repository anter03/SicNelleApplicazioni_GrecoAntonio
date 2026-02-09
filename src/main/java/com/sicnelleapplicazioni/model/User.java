package com.sicnelleapplicazioni.model;

import java.time.Instant;

public class User {

    private Long id; // Corresponds to id INT IDENTITY(1,1) NOT NULL
    private String username; // Corresponds to username NVARCHAR(20)
    private String email; // Corresponds to email NVARCHAR(128)
    private String passwordHash; // Corresponds to password_hash NVARCHAR(255)
    private String salt; // Corresponds to salt NVARCHAR(64)
    private String fullName; // Corresponds to full_name NVARCHAR(100)
    private int failedAttempts; // Corresponds to failed_attempts INT DEFAULT 0
    private Instant lockoutUntil; // Corresponds to lockout_until DATETIME2 NULL
    private Instant lastLogin; // Corresponds to last_login TIMESTAMP NULL

    public User() {
        this.failedAttempts = 0;
    }

    // Getters and Setters for all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Instant getLockoutUntil() {
        return lockoutUntil;
    }

    public void setLockoutUntil(Instant lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isAccountLocked() {
        return lockoutUntil != null && lockoutUntil.isAfter(Instant.now());
    }
}
