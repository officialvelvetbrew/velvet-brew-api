package com.cafe.velvetbrew.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset your Velvet Brew password");
        message.setText("""
                We received a request to reset your Velvet Brew account password.

                Click the link below to choose a new password. This link expires in 30 minutes:
                %s

                If you didn't request this, you can safely ignore this email.
                """.formatted(resetLink));

        mailSender.send(message);

        log.info("Password reset email sent to {}", toEmail);
    }
}
