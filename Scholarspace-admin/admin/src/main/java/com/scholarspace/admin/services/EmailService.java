package com.scholarspace.admin.services;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final String fromEmail = "noreply@scholarspace.com";

    /**
     * Sends an email with the given subject and body to the recipient.
     * For development, this just logs the email to the console.
     *
     * @param to The recipient's email address
     * @param subject The email subject
     * @param body The email body
     */
    public void sendEmail(String to, String subject, String body) {
        // In development mode, just log the email
        System.out.println("========== EMAIL WOULD BE SENT ===========");
        System.out.println("From: " + fromEmail);
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: ");
        System.out.println(body);
        System.out.println("=========================================");
    }
}
