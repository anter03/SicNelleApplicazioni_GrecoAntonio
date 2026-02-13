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

    

        // Regex for full name (alphanumeric, spaces, hyphens, apostrophes, 2-100 characters)

        private static final Pattern FULLNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9'\\- ]{2,100}$");

    

        /**

         * Validates an email address against a predefined pattern.

         * @param email The email to validate.

         * @return true if the email is valid, false otherwise.

         */

        public static boolean isValidEmail(String email) {

            return isValidInput(email, EMAIL_PATTERN);

        }

    

        /**

         * Validates a username against a predefined pattern.

         * @param username The username to validate.

         * @return true if the username is valid, false otherwise.

         */

        public static boolean isValidUsername(String username) {

            return isValidInput(username, USERNAME_PATTERN);

        }

    

        /**

         * Validates a full name against a predefined pattern.

         * @param fullName The full name to validate.

         * @return true if the full name is valid, false otherwise.

         */

        public static boolean isValidFullName(String fullName) {

            return isValidInput(fullName, FULLNAME_PATTERN);

        }

    

        /**

         * Generic input validation method.

         * @param input The input string to validate.

         * @param pattern The pattern to match against.

         * @return true if the input matches the pattern, false otherwise.

         */

        private static boolean isValidInput(String input, Pattern pattern) {

            if (input == null) {

                return false;

            }

            return pattern.matcher(input).matches();

        }

    }

    