package com.john.ledger.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationEmailService.class);

    private final JavaMailSender mailSender;
    private final String mailUsername;

    public EmailVerificationEmailService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername != null ? mailUsername : "";
    }

    @Async("emailTaskExecutor")
    public void sendVerificationEmail(String email, String token, String frontendUrl) {
        if (mailSender != null && !mailUsername.isBlank()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                
                helper.setFrom(mailUsername);
                helper.setTo(email);
                helper.setSubject("Verify Your Email Address - My Ledger");
                
                String verificationLink = frontendUrl + "/verify-email?token=" + token;
                String htmlContent = buildVerificationEmailHtml(email, verificationLink, token);
                helper.setText(htmlContent, true);
                
                mailSender.send(mimeMessage);
                log.info("Verification email sent successfully to {}", email);
            } catch (MessagingException e) {
                log.error("Failed to send verification email to {}: {}", email, e.getMessage(), e);
            } catch (Exception e) {
                log.error("Failed to send verification email to {}: {}", email, e.getMessage(), e);
            }
        } else {
            log.info("Mail not configured - Verification link for {} (dev): {}", email, frontendUrl + "/verify-email?token=" + token);
        }
    }

    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String email, String token, String frontendUrl) {
        if (mailSender != null && !mailUsername.isBlank()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(mailUsername);
                helper.setTo(email);
                helper.setSubject("Reset Your Password - My Ledger");
                String resetLink = frontendUrl + "/reset-password?token=" + token;
                String htmlContent = buildPasswordResetEmailHtml(resetLink);
                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                log.info("Password reset email sent successfully to {}", email);
            } catch (Exception e) {
                log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
            }
        } else {
            log.info("Mail not configured - Password reset link for {} (dev): {}", email, frontendUrl + "/reset-password?token=" + token);
        }
    }

    private String buildPasswordResetEmailHtml(String resetLink) {
        return """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Reset Password - My Ledger</title></head>
            <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 20px; line-height: 1.6;">
            <div style="max-width: 500px; margin: 0 auto; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 32px;">
            <h1 style="margin: 0 0 16px 0; font-size: 20px; color: #18181b;">Reset your password</h1>
            <p style="color: #52525b; margin: 0 0 24px 0;">Click the link below to set a new password. This link expires in 1 hour.</p>
            <p style="margin: 0 0 24px 0;"><a href="%s" style="display: inline-block; background: #18181b; color: #fff; padding: 12px 24px; border-radius: 8px; text-decoration: none;">Reset password</a></p>
            <p style="font-size: 13px; color: #71717a;">If you didn't request this, you can ignore this email.</p>
            </div></body></html>
            """.formatted(resetLink);
    }

    private String buildVerificationEmailHtml(String email, String verificationLink, String token) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <title>Verify Your Email - My Ledger</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        margin: 0;
                        padding: 20px;
                        line-height: 1.6;
                    }
                    .email-wrapper {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        border-radius: 12px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.15);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: #ffffff;
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .header-icon {
                        font-size: 48px;
                        margin-bottom: 15px;
                        display: block;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                        letter-spacing: -0.5px;
                    }
                    .content {
                        padding: 50px 40px;
                        color: #333333;
                    }
                    .greeting {
                        font-size: 18px;
                        font-weight: 500;
                        color: #2c3e50;
                        margin-bottom: 20px;
                    }
                    .message {
                        font-size: 16px;
                        color: #555555;
                        margin-bottom: 30px;
                        line-height: 1.8;
                    }
                    .button-container {
                        text-align: center;
                        margin: 40px 0;
                    }
                    .verify-button {
                        display: inline-block;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: #ffffff !important;
                        text-decoration: none;
                        padding: 16px 40px;
                        border-radius: 8px;
                        font-size: 16px;
                        font-weight: 600;
                        letter-spacing: 0.5px;
                        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                        transition: transform 0.2s, box-shadow 0.2s;
                    }
                    .verify-button:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
                    }
                    .link-fallback {
                        margin-top: 30px;
                        padding: 20px;
                        background-color: #f8f9fa;
                        border-radius: 8px;
                        border-left: 4px solid #667eea;
                    }
                    .link-fallback p {
                        margin: 0 0 10px 0;
                        font-size: 14px;
                        color: #666666;
                    }
                    .link-fallback code {
                        background-color: #ffffff;
                        padding: 8px 12px;
                        border-radius: 4px;
                        font-family: 'Courier New', monospace;
                        font-size: 12px;
                        word-break: break-all;
                        color: #667eea;
                        display: block;
                        border: 1px solid #e0e0e0;
                    }
                    .warning {
                        background: linear-gradient(135deg, #fff3cd 0%, #ffe69c 100%);
                        border-left: 4px solid #ffc107;
                        padding: 20px;
                        margin: 30px 0;
                        border-radius: 8px;
                        color: #856404;
                    }
                    .warning-icon {
                        font-size: 20px;
                        margin-right: 8px;
                        vertical-align: middle;
                    }
                    .warning strong {
                        display: block;
                        margin-bottom: 8px;
                        font-size: 15px;
                    }
                    .warning p {
                        margin: 0;
                        font-size: 14px;
                        line-height: 1.6;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 30px;
                        text-align: center;
                        color: #6c757d;
                        border-top: 1px solid #e9ecef;
                    }
                    .footer-logo {
                        font-size: 20px;
                        font-weight: 600;
                        color: #667eea;
                        margin-bottom: 10px;
                    }
                    .footer p {
                        margin: 8px 0;
                        font-size: 13px;
                        line-height: 1.6;
                    }
                    .footer a {
                        color: #667eea;
                        text-decoration: none;
                    }
                    .footer a:hover {
                        text-decoration: underline;
                    }
                    @media only screen and (max-width: 600px) {
                        .content {
                            padding: 30px 20px;
                        }
                        .header {
                            padding: 30px 20px;
                        }
                        .header h1 {
                            font-size: 24px;
                        }
                        .verify-button {
                            padding: 14px 30px;
                            font-size: 15px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-wrapper">
                    <div class="header">
                        <span class="header-icon">✉️</span>
                        <h1>Verify Your Email</h1>
                    </div>
                    <div class="content">
                        <div class="greeting">Hello!</div>
                        <div class="message">
                            Thank you for signing up with <strong>My Ledger</strong>! We're excited to have you on board.
                            <br><br>
                            To complete your registration and start using your account, please verify your email address by clicking the button below:
                        </div>
                        <div class="button-container">
                            <a href="%s" class="verify-button">Verify Email Address</a>
                        </div>
                        <div class="link-fallback">
                            <p><strong>Button not working?</strong> Copy and paste this link into your browser:</p>
                            <code>%s</code>
                        </div>
                        <div class="warning">
                            <strong><span class="warning-icon">⚠️</span> Important Security Notice</strong>
                            <p>
                                This verification link will expire in <strong>24 hours</strong> for security reasons.
                                If you didn't create an account with My Ledger, please ignore this email or contact our support team.
                            </p>
                        </div>
                    </div>
                    <div class="footer">
                        <div class="footer-logo">My Ledger</div>
                        <p>This is an automated message. Please do not reply to this email.</p>
                        <p>
                            If you have any questions, please contact us at 
                            <a href="mailto:support@myledger.com">support@myledger.com</a>
                        </p>
                        <p style="margin-top: 15px; font-size: 12px; color: #999999;">
                            © 2026 My Ledger. All rights reserved.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verificationLink, verificationLink);
    }
}
