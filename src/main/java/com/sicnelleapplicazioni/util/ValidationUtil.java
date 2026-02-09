package com.sicnelleapplicazioni.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // Regex for email validation (a more robust and commonly used one)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
    );

    // Regex for a simple username (alphanumeric, 3-20 characters)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,20}$");

    /**
     * Validates an email address against a predefined pattern.
     * @param email The email to validate.
     * @return true if the email is valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

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