package com.sicnelleapplicazioni.service;

import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.security.PasswordUtil;

import java.util.Arrays;
import java.util.Optional;

public class LoginService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String username, char[] password) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (user.isAccountLocked()) {
                    long timeSinceLockout = System.currentTimeMillis() - user.getLockoutTime();
                    if (timeSinceLockout > LOCKOUT_DURATION_MS) {
                        userRepository.unlockAccount(username);
                    } else {
                        // Account is still locked
                        return Optional.empty();
                    }
                }

                if (PasswordUtil.verifyPassword(password, user.getSalt(), user.getHashedPassword())) {
                    userRepository.resetFailedLoginAttempts(username);
                    return Optional.of(user);
                } else {
                    userRepository.incrementFailedLoginAttempts(username);
                    if (user.getFailedLoginAttempts() + 1 >= MAX_FAILED_ATTEMPTS) {
                        userRepository.lockAccount(username);
                    }
                    return Optional.empty();
                }
            }

            // To prevent timing attacks, we perform a dummy hash calculation when the user is not found.
            // The salt should be a constant dummy value.
            byte[] dummySalt = new byte[16]; // Dummy salt
            PasswordUtil.hashPassword(password, dummySalt); // Dummy hash
            return Optional.empty();
        } finally {
            // Securely clear the password from memory
            Arrays.fill(password, '\0');
        }
    }
}
