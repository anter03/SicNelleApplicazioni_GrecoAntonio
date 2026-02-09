package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong();

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.incrementAndGet());
        }
        users.put(user.getUsername(), user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public void incrementFailedLoginAttempts(String username) {
        users.computeIfPresent(username, (k, user) -> {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            return user;
        });
    }

    @Override
    public void resetFailedLoginAttempts(String username) {
        users.computeIfPresent(username, (k, user) -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setLockoutTime(0);
            return user;
        });
    }

    @Override
    public void lockAccount(String username) {
        users.computeIfPresent(username, (k, user) -> {
            user.setAccountLocked(true);
            user.setLockoutTime(System.currentTimeMillis());
            return user;
        });
    }

    @Override
    public void unlockAccount(String username) {
        users.computeIfPresent(username, (k, user) -> {
            user.setAccountLocked(false);
            user.setLockoutTime(0);
            return user;
        });
    }

    @Override
    public Optional<User> findByVerificationToken(String token) {
        return users.values().stream()
                .filter(user -> token.equals(user.getVerificationToken()))
                .findFirst();
    }
}
