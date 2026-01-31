package com.taskmanager.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Sends a verification email with a clickable link.
     */
    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        try {
            String verificationLink = "http://localhost:" + serverPort + "/api/auth/verify-email?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@taskflow.com");
            message.setTo(toEmail);
            message.setSubject("Verify Your TaskFlow Account");
            message.setText("Hello " + username + ",\n\n" +
                    "Welcome to TaskFlow!\n\n" +
                    "Please click the link below to verify your email address:\n\n" +
                    verificationLink + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "If you did not create an account, please ignore this email.\n\n" +
                    "Best regards,\nThe TaskFlow Team");

            mailSender.send(message);
            log.info("📧 Verification email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send verification email: {}", e.getMessage());
        }
    }

    /**
     * Sends an email notification when a task is created.
     */
    @Async
    public void sendTaskAssignmentEmail(String toEmail, String taskTitle, String dueDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@taskflow.com");
            message.setTo(toEmail);
            message.setSubject("New Task Created: " + taskTitle);
            message.setText("A new task has been created.\n\n" +
                    "Title: " + taskTitle + "\n" +
                    "Due Date: " + (dueDate != null ? dueDate : "Not specified") + "\n\n" +
                    "Please log in to the dashboard to view details.");

            mailSender.send(message);
            log.info("📧 Task email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send task email: {}", e.getMessage());
        }
    }
}
