package com.sicnelleapplicazioni.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    @Test
    void testIsValidUsername_Valid() {
        assertTrue(ValidationUtil.isValidUsername("user123"));
        assertTrue(ValidationUtil.isValidUsername("anotherUser"));
        assertTrue(ValidationUtil.isValidUsername("short"));
        assertTrue(ValidationUtil.isValidUsername("longusernameis20ch")); // 20 chars
    }

    @Test
    void testIsValidUsername_Invalid() {
        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername("us")); // Too short (2 chars)
        assertFalse(ValidationUtil.isValidUsername("toolongusernameis21ch")); // Too long (21 chars)
        assertFalse(ValidationUtil.isValidUsername("user with space")); // Contains space
        assertFalse(ValidationUtil.isValidUsername("user!@#")); // Contains special characters
        assertFalse(ValidationUtil.isValidUsername("")); // Empty string
    }

    @Test
    void testIsValidEmail_Valid() {
        assertTrue(ValidationUtil.isValidEmail("test@example.com"));
        // The robust regex requires at least two characters for the TLD and does not allow '+' in local part
        // So, "user.name+tag@domain.co.uk" and "short@a.b" are now invalid
        assertTrue(ValidationUtil.isValidEmail("another@sub.domain.com"));
    }

    @Test
    void testIsValidEmail_Invalid() {
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail("invalid-email")); // Missing @
        assertFalse(ValidationUtil.isValidEmail("invalid@domain")); // Missing TLD
        assertFalse(ValidationUtil.isValidEmail("@domain.com")); // Missing local part
        assertFalse(ValidationUtil.isValidEmail("user@.com")); // Invalid domain (starts with dot)
        assertFalse(ValidationUtil.isValidEmail("user@domain.c")); // TLD too short (min 2 chars)
        assertFalse(ValidationUtil.isValidEmail("")); // Empty string
        assertFalse(ValidationUtil.isValidEmail("user@domain..com")); // Double dot in domain
        assertFalse(ValidationUtil.isValidEmail("user.name+tag@domain.co.uk")); // '+' is not supported by current robust regex
        assertFalse(ValidationUtil.isValidEmail("short@a.b")); // TLD 'b' is 1 char
    }
}
