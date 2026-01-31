package com.taskmanager.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends an email notification when a task is created.
     * 
     * @Async ensures the API doesn't wait for the email to send (Performance
     *        Optimization)
     */
    @Async
    public void sendTaskAssignmentEmail(String toEmail, String taskTitle, String dueDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@taskflow.com");
            message.setTo(toEmail);
            message.setSubject("New Task Assigned: " + taskTitle);
            message.setText("You have been assigned a new task.\n\n" +
                    "Title: " + taskTitle + "\n" +
                    "Due Date: " + (dueDate != null ? dueDate : "Not specified") + "\n\n" +
                    "Please log in to the dashboard to view details.");

            mailSender.send(message);
            log.info("📧 Email sent successfully to {}", toEmail);
        } catch (Exception e) {
            // Log error but don't break the application flow
            log.error("❌ Failed to send email: {}", e.getMessage());
        }
    }
}
