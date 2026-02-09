package com.sicnelleapplicazioni.service;

import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.security.PasswordUtil;

import java.time.Instant;
import java.util.Optional;

public class LoginService {

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String identifier, char[] password) { // 'identifier' can be username or email
        if (identifier == null || identifier.trim().isEmpty() || password == null || password.length == 0) {
            return false;
        }

        Optional<User> userOptional = userRepository.findByEmail(identifier);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByUsername(identifier);
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if account is locked
            if (user.isAccountLocked()) { // Using the isAccountLocked() helper from User model
                return false;
            }

            // Assuming PasswordUtil has a method to verify password
            if (PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
                // Reset failed login attempts on successful login
                userRepository.resetFailedAttempts(identifier);
                user.setLastLogin(Instant.now()); // Update last login time
                userRepository.save(user); // Explicitly save the user to persist lastLogin
                return true;
            } else {
                // Increment failed login attempts on failed password
                userRepository.incrementFailedAttempts(identifier);
                // Optionally, call lockAccount if failed attempts exceed a threshold
                // This threshold logic should be implemented in the UserRepository.incrementFailedAttempts or a separate service
                Optional<User> updatedUser = userRepository.findByEmail(identifier); // Re-fetch user to get updated failedAttempts
                if (updatedUser.isEmpty()) { // Try by username if not found by email
                    updatedUser = userRepository.findByUsername(identifier);
                }

                if (updatedUser.isPresent() && updatedUser.get().getFailedAttempts() >= 5) { // Example threshold
                    userRepository.lockAccount(identifier);
                }
                return false;
            }
        }
        // Increment failed login attempts for non-existent user to prevent enumeration attacks
        userRepository.incrementFailedAttempts(identifier);
        return false;
    }
}
