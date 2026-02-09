package com.sicnelleapplicazioni.repository;

import com.sicnelleapplicazioni.model.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByUsername(String username);

    void incrementFailedLoginAttempts(String username);

    void resetFailedLoginAttempts(String username);

    void lockAccount(String username);

    void unlockAccount(String username);

    Optional<User> findByVerificationToken(String token);
}
