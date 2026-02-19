package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    void incrementFailedAttempts(String identifier);
    void resetFailedAttempts(String identifier);
    void lockAccount(String identifier);
    void unlockAccount(String identifier);

    User update(User user);
}
