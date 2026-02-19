package com.sicnelleapplicazioni.service;

import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.security.PasswordUtil;

import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger; // Import Logger
import java.util.logging.Level;  // Import Level
import java.util.Arrays; // Import Arrays for clearing password

public class LoginService {

    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName()); // Add Logger

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String identifier, char[] password) {
        LOGGER.log(Level.INFO, "Attempting authentication for identifier: {0}", identifier);
        if (identifier == null || identifier.trim().isEmpty() || password == null || password.length == 0) {
            LOGGER.log(Level.WARNING, "Authentication failed for identifier {0}: Invalid input.", identifier);
            return false;
        }

        Optional<User> userOptional = userRepository.findByEmail(identifier);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByUsername(identifier);
        }

        try {
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                LOGGER.log(Level.INFO, "User found for identifier: {0}. User ID: {1}", new Object[]{identifier, user.getId()});
                LOGGER.log(Level.INFO, "Stored Salt: {0}", user.getSalt());
                LOGGER.log(Level.INFO, "Stored Password Hash: {0}", user.getPasswordHash());

                // Check if account is locked
                if (user.isAccountLocked()) {
                    LOGGER.log(Level.WARNING, "Authentication failed for user {0}: Account is locked.", user.getUsername());
                    return false;
                }

                // Assuming PasswordUtil has a method to verify password
                if (PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
                    LOGGER.log(Level.INFO, "Authentication successful for user: {0}", user.getUsername());
                    userRepository.resetFailedAttempts(identifier);
                    user.setLastLogin(Instant.now());
                    userRepository.update(user);
                    return true;
                } else {
                    LOGGER.log(Level.WARNING, "Authentication failed for user {0}: Invalid password.", user.getUsername());
                    userRepository.incrementFailedAttempts(identifier);
                    Optional<User> updatedUser = userRepository.findByEmail(identifier);
                    if (updatedUser.isEmpty()) {
                        updatedUser = userRepository.findByUsername(identifier);
                    }

                    if (updatedUser.isPresent() && updatedUser.get().getFailedAttempts() >= 5) {
                        LOGGER.log(Level.WARNING, "Account locked for user {0} due to too many failed attempts.", updatedUser.get().getUsername());
                        userRepository.lockAccount(identifier);
                    }
                    return false;
                }
            } else {
                LOGGER.log(Level.WARNING, "Authentication failed for identifier {0}: User not found.", identifier);
                userRepository.incrementFailedAttempts(identifier); // Increment attempts even for non-existent user
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during authentication for identifier {0}", identifier);
            LOGGER.log(Level.SEVERE, "Exception: ", e);
            return false;
        } finally {
            Arrays.fill(password, '\0'); // Clear password from memory
        }
    }
}
