package com.sicnelleapplicazioni.service;

import com.sicnelleapplicazioni.security.PasswordPolicyValidator;
import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.security.PasswordUtil;

import java.util.Arrays;
import java.time.Instant;

public class RegistrationService {

    private final UserRepository userRepository;
    // EmailService no longer needed for verification, but might be for other purposes
    // private final EmailService emailService;

    public RegistrationService(UserRepository userRepository) { // Removed EmailService from constructor
        this.userRepository = userRepository;
        // this.emailService = emailService;
    }

    public boolean register(String username, String email, char[] password, String fullName) {
        try {
            if (!PasswordPolicyValidator.validate(password)) {
                return false;
            }

            // Check if username or email already exists
            if (userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
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
            // Removed email verification logic
            return true;
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}