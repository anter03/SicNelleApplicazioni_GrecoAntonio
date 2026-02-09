package com.sicnelleapplicazioni.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    @Test
    void testIsValidUsername_Valid() {
        assertTrue(ValidationUtil.isValidUsername("user123"));
        assertTrue(ValidationUtil.isValidUsername("anotherUser"));
        assertTrue(ValidationUtil.isValidUsername("short"));
        assertTrue(ValidationUtil.isValidUsername("verylongusername123456")); // 20 chars
    }

    @Test
    void testIsValidUsername_Invalid() {
        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername("us")); // Too short
        assertFalse(ValidationUtil.isValidUsername("toolongusername123456789")); // Too long
        assertFalse(ValidationUtil.isValidUsername("user with space")); // Contains space
        assertFalse(ValidationUtil.isValidUsername("user!@#")); // Contains special characters
        assertFalse(ValidationUtil.isValidUsername("")); // Empty string
    }
}
