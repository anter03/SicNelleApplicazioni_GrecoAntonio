package com.sicnelleapplicazioni.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // Example regex for a simple username (alphanumeric, 3-20 characters)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,20}$");

    /**
     * Validates a username against a predefined pattern.
     * @param username The username to validate.
     * @return true if the username is valid, false otherwise.
     */
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
}
