package com.taskmanager.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
     * Sends an HTML verification email with a clickable button.
     */
    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        try {
            String verificationLink = "http://localhost:" + serverPort + "/api/auth/verify-email?token=" + token;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("noreply@taskflow.com");
            helper.setTo(toEmail);
            helper.setSubject("Verify Your TaskFlow Account");

            String htmlContent = """
                    <html>
                    <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                        <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                            <h1 style="color: #333; text-align: center;">Welcome to TaskFlow!</h1>
                            <p style="color: #666; font-size: 16px;">Hello <strong>%s</strong>,</p>
                            <p style="color: #666; font-size: 16px;">Thank you for registering! Please verify your email address by clicking the button below:</p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #4CAF50; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-size: 16px; display: inline-block;">Verify Email</a>
                            </div>
                            <p style="color: #999; font-size: 14px;">Or copy and paste this link in your browser:</p>
                            <p style="color: #4CAF50; font-size: 14px; word-break: break-all;"><a href="%s">%s</a></p>
                            <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                            <p style="color: #999; font-size: 12px;">This link will expire in 24 hours.</p>
                            <p style="color: #999; font-size: 12px;">If you did not create an account, please ignore this email.</p>
                        </div>
                    </body>
                    </html>
                    """
                    .formatted(username, verificationLink, verificationLink, verificationLink);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("📧 Verification email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
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
