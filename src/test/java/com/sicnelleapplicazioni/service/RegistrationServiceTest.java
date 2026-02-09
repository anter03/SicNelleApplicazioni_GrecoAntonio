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
    private EmailService mockEmailService;

    @BeforeEach
    void setUp() {
        mockUserRepository = mock(UserRepository.class);
        mockEmailService = mock(EmailService.class);
        registrationService = new RegistrationService(mockUserRepository, mockEmailService);
    }

    @Test
    void testRegister_Success() {
        String username = "testuser";
        char[] password = "Password123!".toCharArray(); // Meets policy

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(mockUserRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID generation
            return user;
        });

        assertTrue(registrationService.register(username, password));

        verify(mockUserRepository, times(1)).findByUsername(username);
        verify(mockUserRepository, times(1)).save(any(User.class));
        verify(mockEmailService, times(1)).sendVerificationEmail(any(User.class), anyString());
    }

    @Test
    void testRegister_PasswordPolicyFailure() {
        String username = "testuser";
        char[] password = "short".toCharArray(); // Fails policy

        assertFalse(PasswordPolicyValidator.validate(password)); // Sanity check for policy
        assertFalse(registrationService.register(username, password));

        verify(mockUserRepository, never()).findByUsername(username);
        verify(mockUserRepository, never()).save(any(User.class));
        verify(mockEmailService, never()).sendVerificationEmail(any(User.class), anyString());
    }

    @Test
    void testRegister_UserAlreadyExists() {
        String username = "existinguser";
        char[] password = "Password123!".toCharArray();

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(new User())); // User exists

        assertTrue(registrationService.register(username, password)); // Still returns true to prevent enumeration

        verify(mockUserRepository, times(1)).findByUsername(username);
        verify(mockUserRepository, never()).save(any(User.class)); // Should not save
        verify(mockEmailService, never()).sendVerificationEmail(any(User.class), anyString()); // Should not send email
    }
}
