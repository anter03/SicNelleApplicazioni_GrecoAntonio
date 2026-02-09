package com.sicnelleapplicazioni.service;

import com.sicnelleapplicazioni.model.User;

public class EmailService {

    /**
     * Sends a verification email to the user.
     * In a real implementation, this method would use a mail server to send an email
     * containing a unique verification token.
     *
     * @param user The user to send the verification email to.
     * @param token The verification token.
     */
    public void sendVerificationEmail(User user, String token) {
        String verificationLink = "https://yourdomain.com/verify?id=" + user.getId() + "&token=" + token;
        String subject = "Verify your Email for SicNelleApplicazioni";
        String body = "Dear " + user.getUsername() + ",\n\n"
                    + "Thank you for registering with SicNelleApplicazioni. Please click on the link below to verify your email address:\n\n"
                    + verificationLink + "\n\n"
                    + "If you did not register for this service, please ignore this email.\n\n"
                    + "Sincerely,\n"
                    + "The SicNelleApplicazioni Team";

        System.out.println("--- Sending Email ---");
        System.out.println("To: " + user.getUsername() + " (assuming username is email for now)");
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
        System.out.println("--- Email Sent ---");
    }
}
