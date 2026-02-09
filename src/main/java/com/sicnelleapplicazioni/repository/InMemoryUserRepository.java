package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.User;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong();

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.incrementAndGet());
        }
        usersByUsername.put(user.getUsername(), user);
        usersByEmail.put(user.getEmail(), user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    @Override
    public void incrementFailedAttempts(String identifier) {
        // This is a simplified in-memory logic. In a real app,
        // you'd decide if 'identifier' refers to username or email
        // and update the corresponding user object. For simplicity,
        // we'll update based on email if found, else username.
        usersByEmail.computeIfPresent(identifier, (k, user) -> {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            return user;
        });
        usersByUsername.computeIfPresent(identifier, (k, user) -> {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            return user;
        });
    }

    @Override
    public void resetFailedAttempts(String identifier) {
        usersByEmail.computeIfPresent(identifier, (k, user) -> {
            user.setFailedAttempts(0);
            user.setLockoutUntil(null); // Unlock
            return user;
        });
        usersByUsername.computeIfPresent(identifier, (k, user) -> {
            user.setFailedAttempts(0);
            user.setLockoutUntil(null); // Unlock
            return user;
        });
    }

    @Override
    public void lockAccount(String identifier) {
        usersByEmail.computeIfPresent(identifier, (k, user) -> {
            user.setLockoutUntil(Instant.now().plusSeconds(30 * 60)); // Lock for 30 minutes
            return user;
        });
        usersByUsername.computeIfPresent(identifier, (k, user) -> {
            user.setLockoutUntil(Instant.now().plusSeconds(30 * 60)); // Lock for 30 minutes
            return user;
        });
    }

    @Override
    public void unlockAccount(String identifier) {
        usersByEmail.computeIfPresent(identifier, (k, user) -> {
            user.setLockoutUntil(null); // Unlock
            user.setFailedAttempts(0);
            return user;
        });
        usersByUsername.computeIfPresent(identifier, (k, user) -> {
            user.setLockoutUntil(null); // Unlock
            user.setFailedAttempts(0);
            return user;
        });
    }
    // Removed findByVerificationToken as it's no longer in the User model
}
