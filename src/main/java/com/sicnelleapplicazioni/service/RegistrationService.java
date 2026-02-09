package com.sicnelleapplicazioni.service;

import com.sicnelleapplicazioni.security.PasswordPolicyValidator;
import com.sicnelleapplicazioni.model.User;
import com.sicnelleapplicazioni.repository.UserRepository;
import com.sicnelleapplicazioni.security.PasswordUtil;

import java.util.Arrays;

public class RegistrationService {



    private final UserRepository userRepository;

    private final EmailService emailService;



    public RegistrationService(UserRepository userRepository, EmailService emailService) {

        this.userRepository = userRepository;

        this.emailService = emailService;

    }



    public boolean register(String username, char[] password) {

        try {

            if (!PasswordPolicyValidator.validate(password)) {

                return false;

            }



            if (userRepository.findByUsername(username).isPresent()) {

                // User already exists. To prevent timing attacks, we perform a dummy hash calculation.

                // The salt should be a constant dummy value.

                byte[] dummySalt = new byte[16];

                PasswordUtil.hashPassword(password, dummySalt);

                return true; // Return true to not reveal that the user already exists

            }



            byte[] salt = PasswordUtil.generateSalt();

            byte[] hashedPassword = PasswordUtil.hashPassword(password, salt);

            String verificationToken = generateVerificationToken();



            User user = new User();

            user.setUsername(username);

            user.setSalt(salt);

            user.setHashedPassword(hashedPassword);

            user.setVerificationToken(verificationToken);

            user.setEmailVerified(false);



            userRepository.save(user);

            emailService.sendVerificationEmail(user, verificationToken);

            return true;

        } finally {

            Arrays.fill(password, '\0');

        }

    }



    private String generateVerificationToken() {

        // In a real application, this would be a more robust, secure random token.

        return java.util.UUID.randomUUID().toString();

    }

}
