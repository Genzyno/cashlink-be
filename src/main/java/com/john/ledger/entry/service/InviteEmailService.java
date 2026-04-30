package com.john.ledger.entry.service;

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

import java.util.List;

@Service
public class InviteEmailService {

    private static final Logger log = LoggerFactory.getLogger(InviteEmailService.class);
    private static final int MAX_SEND_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private final JavaMailSender mailSender;
    private final String mailUsername;

    public InviteEmailService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername != null ? mailUsername : "";
    }

    /**
     * @param bookAndBusinessLines list of "BookName (BusinessName)" for the invite; can be empty
     * @param invitedByName display name of the user who sent the invite (e.g. "John Britto"); can be null
     */
    @Async("emailTaskExecutor")
    public void sendInviteEmail(String toEmail, String roleName, List<String> bookAndBusinessLines, String acceptLink, String rejectLink, String invitedByName) {
        if (mailSender == null || mailUsername.isBlank()) {
            log.info("Mail not configured - Invite for {} (dev): accept={}, reject={}", toEmail, acceptLink, rejectLink);
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailUsername);
            helper.setTo(toEmail);
            helper.setSubject("You're invited to My Ledger");
            helper.setText(buildInviteEmailHtml(toEmail, roleName, bookAndBusinessLines, acceptLink, rejectLink, invitedByName), true);

            sendWithRetry(mimeMessage, toEmail);
            log.info("Invite email sent successfully to {}", toEmail);
        } catch (MailSendException e) {
            log.error("Failed to send invite email to {} after {} attempts: {}", toEmail, MAX_SEND_ATTEMPTS, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to send invite email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private void sendWithRetry(MimeMessage message, String toEmail) throws MailSendException {
        MailSendException lastEx = null;
        for (int attempt = 1; attempt <= MAX_SEND_ATTEMPTS; attempt++) {
            try {
                mailSender.send(message);
                return;
            } catch (MailSendException e) {
                lastEx = e;
                if (attempt < MAX_SEND_ATTEMPTS && isTransientMailError(e)) {
                    log.warn("Invite mail send attempt {}/{} failed for {} ({}), retrying in {}ms", attempt, MAX_SEND_ATTEMPTS, toEmail, e.getMessage(), RETRY_DELAY_MS);
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

    private String buildInviteEmailHtml(String email, String roleName, List<String> bookAndBusinessLines, String acceptLink, String rejectLink, String invitedByName) {
        String roleLine = (roleName != null && !roleName.isBlank())
                ? "<p><strong>Role:</strong> " + escapeHtml(roleName) + "</p>"
                : "<p><strong>Role:</strong> To be assigned</p>";
        String booksSection = "";
        if (bookAndBusinessLines != null && !bookAndBusinessLines.isEmpty()) {
            StringBuilder sb = new StringBuilder("<p><strong>Books you will have access to:</strong></p><ul>");
            for (String line : bookAndBusinessLines) {
                sb.append("<li>").append(escapeHtml(line)).append("</li>");
            }
            sb.append("</ul>");
            booksSection = sb.toString();
        }
        String invitedByLine = (invitedByName != null && !invitedByName.isBlank())
                ? "<p><strong>Invited by:</strong> " + escapeHtml(invitedByName) + "</p>"
                : "";
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; margin: 0; padding: 32px 16px; background: #f4f4f5; color: #18181b; font-size: 15px; line-height: 1.6; }
                    .card { max-width: 480px; margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.08); border: 1px solid #e4e4e7; }
                    .header { background: #18181b; color: #fff; padding: 24px; text-align: center; }
                    .header h1 { margin: 0; font-size: 18px; font-weight: 600; }
                    .body { padding: 32px 24px; }
                    .body p { margin: 0 0 12px 0; color: #52525b; }
                    .detail-box { background: #fafafa; border: 1px solid #e4e4e7; border-radius: 8px; padding: 16px; margin: 20px 0; }
                    .detail-box ul { margin: 8px 0 0 0; padding-left: 20px; }
                    .btn { display: inline-block; color: #fff !important; padding: 14px 28px; border-radius: 8px; text-decoration: none; font-weight: 600; margin: 8px 8px 0 0; }
                    .btn-accept { background: #18181b; }
                    .btn-accept:hover { background: #3f3f46; }
                    .btn-reject { background: #71717a; }
                    .btn-reject:hover { background: #52525b; }
                    .footer { background: #fafafa; padding: 16px 24px; text-align: center; color: #71717a; font-size: 12px; border-top: 1px solid #e4e4e7; }
                    .note { background: #fefce8; border: 1px solid #fef08a; border-radius: 6px; padding: 12px; margin-top: 20px; color: #854d0e; font-size: 13px; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <h1>My Ledger – Team Invitation</h1>
                    </div>
                    <div class="body">
                        <p>You have been invited to join <strong>My Ledger</strong>.</p>
                        <div class="detail-box">
                            <p><strong>Email:</strong> %s</p>
                            %s
                            %s
                            %s
                        </div>
                        <p>Choose an option below:</p>
                        <a href="%s" class="btn btn-accept">Accept invitation</a>
                        <a href="%s" class="btn btn-reject">Reject invitation</a>
                        <div class="note">This link expires in 7 days. Accept to create your account (you will set your password). Reject to decline; the admin can send a new invite later if needed.</div>
                    </div>
                    <div class="footer">
                        This is an automated message from My Ledger.
                    </div>
                </div>
            </body>
            </html>
            """
                .formatted(escapeHtml(email), invitedByLine, roleLine, booksSection, escapeHtml(acceptLink), escapeHtml(rejectLink));
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
