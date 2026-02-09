package com.sicnelleapplicazioni.service;

import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.security.PasswordPolicyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegistrationServiceTest {

    private RegistrationService registrationService;
    private UserRepository mockUserRepository;
    // EmailService is no longer a dependency for RegistrationService
    // private EmailService mockEmailService;

    @BeforeEach
    void setUp() {
        mockUserRepository = mock(UserRepository.class);
        // mockEmailService = mock(EmailService.class);
        registrationService = new RegistrationService(mockUserRepository); // Updated constructor
    }

    @Test
    void testRegister_Success() {
        String username = "testuser";
        String email = "testuser@example.com";
        char[] password = "Password123!".toCharArray(); // Meets policy
        String fullName = "Test User";

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(mockUserRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(mockUserRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID generation
            return user;
        });

        assertTrue(registrationService.register(username, email, password, fullName));

        verify(mockUserRepository, times(1)).findByUsername(username);
        verify(mockUserRepository, times(1)).findByEmail(email);
        verify(mockUserRepository, times(1)).save(any(User.class));
        // Removed emailService verification
    }

    @Test
    void testRegister_PasswordPolicyFailure() {
        String username = "testuser";
        String email = "testuser@example.com";
        char[] password = "short".toCharArray(); // Fails policy
        String fullName = "Test User";

        assertFalse(PasswordPolicyValidator.validate(password)); // Sanity check for policy
        assertFalse(registrationService.register(username, email, password, fullName));

        verify(mockUserRepository, never()).findByUsername(username);
        verify(mockUserRepository, never()).findByEmail(email);
        verify(mockUserRepository, never()).save(any(User.class));
        // Removed emailService verification
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        String username = "existinguser";
        String email = "testuser@example.com";
        char[] password = "Password123!".toCharArray();
        String fullName = "Test User";

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(new User())); // Username exists
        when(mockUserRepository.findByEmail(email)).thenReturn(Optional.empty()); // Email does not exist

        assertFalse(registrationService.register(username, email, password, fullName));

        verify(mockUserRepository, times(1)).findByUsername(username);
        verify(mockUserRepository, never()).save(any(User.class)); // Should not save
        // Removed emailService verification
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        String username = "testuser";
        String email = "existinguser@example.com";
        char[] password = "Password123!".toCharArray();
        String fullName = "Test User";

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.empty()); // Username does not exist
        when(mockUserRepository.findByEmail(email)).thenReturn(Optional.of(new User())); // Email exists

        assertFalse(registrationService.register(username, email, password, fullName));

        verify(mockUserRepository, times(1)).findByUsername(username);
        verify(mockUserRepository, times(1)).findByEmail(email);
        verify(mockUserRepository, never()).save(any(User.class)); // Should not save
        // Removed emailService verification
    }
}