package com.sicnelleapplicazioni.service;

import com.sicnelleapplicazioni.security.PasswordPolicyValidator;
import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.security.PasswordUtil;

import java.util.Arrays;
import java.time.Instant;
// Removed Logger imports

public class RegistrationService {

    // Removed Logger field

    private final UserRepository userRepository;

    public RegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(String username, String email, char[] password, String fullName) {
        try {
            if (!PasswordPolicyValidator.validate(password)) {
                return false;
            }

            // Check if username or email already exists
            if (userRepository.findByUsername(username).isPresent()) {
                return false;
            }
            if (userRepository.findByEmail(email).isPresent()) {
                return false;
            }

            String salt = PasswordUtil.generateSalt();
            String passwordHash = PasswordUtil.hashPassword(password, salt);

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setSalt(salt);
            user.setFullName(fullName);
            user.setFailedAttempts(0); // Default value
            user.setLockoutUntil(null); // Default to not locked

            userRepository.save(user);
            return true;
        } catch (Exception e) {
            // Re-throw as RuntimeException or handle more gracefully if needed
            throw new RuntimeException("Error during registration for username: " + username + ", email: " + email, e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
