package com.john.ledger.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailService {

    private static final Logger log = LoggerFactory.getLogger(OtpEmailService.class);
    private static final int MAX_SEND_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private final JavaMailSender mailSender;
    private final String mailUsername;

    public OtpEmailService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername != null ? mailUsername : "";
    }

    @Async("emailTaskExecutor")
    public void sendOtpToEmail(String email, String otp) {
        if (mailSender == null || mailUsername.isBlank()) {
            log.info("Mail not configured - OTP for {} (dev): {}", email, otp);
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailUsername);
            helper.setTo(email);
            helper.setSubject("Your Verification Code - My Ledger");
            helper.setText(buildOtpEmailHtml(otp), true);

            sendWithRetry(mimeMessage, email, otp);
            log.info("OTP email sent successfully to {}", email);
        } catch (MailSendException e) {
            log.error("Failed to send OTP email to {} after {} attempts: {} - OTP for dev: {}", email, MAX_SEND_ATTEMPTS, e.getMessage(), otp, e);
            if (isConnectionBlocked(e)) {
                log.warn("Mail connection blocked. Allow outbound SMTP (port 587 or 465) in firewall, or use --spring.profiles.active=mail-465");
            }
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {} - OTP for dev: {}", email, e.getMessage(), otp, e);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {} - OTP for dev: {}", email, e.getMessage(), otp, e);
        }
    }

    /** Retry send on transient failures (timeout, EOF, connect error) to improve reliability. */
    private void sendWithRetry(MimeMessage message, String email, String otp) throws MailSendException {
        MailSendException lastEx = null;
        for (int attempt = 1; attempt <= MAX_SEND_ATTEMPTS; attempt++) {
            try {
                mailSender.send(message);
                return;
            } catch (MailSendException e) {
                lastEx = e;
                if (attempt < MAX_SEND_ATTEMPTS && isTransientMailError(e)) {
                    log.warn("Mail send attempt {}/{} failed for {} ({}), retrying in {}ms", attempt, MAX_SEND_ATTEMPTS, email, e.getMessage(), RETRY_DELAY_MS);
                    sleepQuietly();
                } else {
                    throw e;
                }
            }
        }
        if (lastEx != null) throw lastEx;
    }

    private static void sleepQuietly() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Mail retry interrupted", ie);
        }
    }

    private static boolean isTransientMailError(Throwable e) {
        String msg = e.getMessage();
        if (msg == null) msg = "";
        if (msg.contains("timed out") || msg.contains("Timeout") || msg.contains("[EOF]") || msg.contains("bad greeting") ||
            msg.contains("Connection refused") || msg.contains("Connection reset") || msg.contains("Connect timed out") || msg.contains("Read timed out")) {
            return true;
        }
        Throwable cause = e.getCause();
        return cause != null && isTransientMailError(cause);
    }

    private String buildOtpEmailHtml(String otp) {
        String htmlTemplate = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; margin: 0; padding: 32px 16px; background: #f4f4f5; color: #18181b; font-size: 15px; line-height: 1.6; }
                    .card { max-width: 420px; margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.08); border: 1px solid #e4e4e7; }
                    .header { background: #18181b; color: #fff; padding: 24px; text-align: center; }
                    .header h1 { margin: 0; font-size: 18px; font-weight: 600; letter-spacing: -0.02em; }
                    .body { padding: 32px 24px; }
                    .body p { margin: 0 0 16px 0; color: #52525b; }
                    .otp-box { background: #fafafa; border: 1px dashed #d4d4d8; border-radius: 8px; padding: 20px; margin: 24px 0; text-align: center; }
                    .otp { font-family: 'SF Mono', Monaco, 'Courier New', monospace; font-size: 32px; font-weight: 600; letter-spacing: 8px; color: #18181b; margin: 0; }
                    .note { display: flex; align-items: center; gap: 8px; background: #fefce8; border: 1px solid #fef08a; border-radius: 6px; padding: 12px 16px; margin-top: 24px; color: #854d0e; font-size: 13px; }
                    .footer { background: #fafafa; padding: 16px 24px; text-align: center; color: #71717a; font-size: 12px; border-top: 1px solid #e4e4e7; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <h1>My Ledger</h1>
                    </div>
                    <div class="body">
                        <p>Your verification code:</p>
                        <div class="otp-box">
                            <p class="otp">OTP_PLACEHOLDER</p>
                        </div>
                        <p>Enter this code to complete your login.</p>
                        <div class="note">Valid for 5 minutes. Do not share this code with anyone.</div>
                    </div>
                    <div class="footer">
                        This is an automated message from My Ledger.
                    </div>
                </div>
            </body>
            </html>
            """;
        return htmlTemplate.replace("OTP_PLACEHOLDER", otp);
    }

    private static boolean isConnectionBlocked(Throwable e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("Permission denied")) return true;
        Throwable cause = e.getCause();
        return cause != null && isConnectionBlocked(cause);
    }
}
