package com.sicnelleapplicazioni.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordPolicyValidator {

    // Minimum 12 characters, at least one uppercase letter, one number, and one special character.
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean validate(char[] password) {
        Matcher matcher = pattern.matcher(new String(password));
        return matcher.matches();
    }
}