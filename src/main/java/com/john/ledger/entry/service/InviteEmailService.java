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
                ? "<div style='margin-bottom: 8px;'><span style='color: #71717a; font-size: 13px; text-transform: uppercase; letter-spacing: 0.05em;'>Role</span><br/><strong style='color: #18181b; font-size: 15px;'>" + escapeHtml(roleName) + "</strong></div>"
                : "";
        String booksSection = "";
        if (bookAndBusinessLines != null && !bookAndBusinessLines.isEmpty()) {
            StringBuilder sb = new StringBuilder("<div style='margin-top: 16px; padding-top: 16px; border-top: 1px solid #e4e4e7;'><span style='color: #71717a; font-size: 13px; text-transform: uppercase; letter-spacing: 0.05em;'>Books you will access</span><ul style='margin: 8px 0 0 0; padding-left: 20px; color: #3f3f46; font-size: 14px;'>");
            for (String line : bookAndBusinessLines) {
                sb.append("<li style='margin-bottom: 4px;'>").append(escapeHtml(line)).append("</li>");
            }
            sb.append("</ul></div>");
            booksSection = sb.toString();
        }
        String invitedByLine = (invitedByName != null && !invitedByName.isBlank())
                ? "<div style='margin-bottom: 8px;'><span style='color: #71717a; font-size: 13px; text-transform: uppercase; letter-spacing: 0.05em;'>Invited by</span><br/><strong style='color: #18181b; font-size: 15px;'>" + escapeHtml(invitedByName) + "</strong></div>"
                : "";
        String emailLine = "<div style='margin-bottom: 8px;'><span style='color: #71717a; font-size: 13px; text-transform: uppercase; letter-spacing: 0.05em;'>Your Email</span><br/><strong style='color: #18181b; font-size: 15px;'>" + escapeHtml(email) + "</strong></div>";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
                    body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 0; background-color: #f8fafc; color: #1e293b; }
                    .wrapper { padding: 40px 20px; }
                    .container { max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); border: 1px solid #e2e8f0; }
                    .header { background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); padding: 40px 32px; text-align: center; }
                    .header h1 { margin: 0; color: #f8fafc; font-size: 24px; font-weight: 700; letter-spacing: -0.025em; }
                    .header p { margin: 8px 0 0 0; color: #94a3b8; font-size: 14px; }
                    .content { padding: 32px; }
                    .intro { font-size: 16px; color: #475569; margin-bottom: 24px; }
                    .details-card { background: #f1f5f9; border-radius: 12px; padding: 24px; margin-bottom: 32px; }
                    .actions { display: flex; flex-direction: column; gap: 12px; }
                    .btn { display: block; text-align: center; padding: 14px 24px; border-radius: 8px; font-weight: 600; font-size: 15px; text-decoration: none; transition: all 0.2s; }
                    .btn-primary { background-color: #0f172a; color: #ffffff !important; margin-bottom: 12px; }
                    .btn-secondary { background-color: #ffffff; color: #475569 !important; border: 1px solid #e2e8f0; }
                    .footer { padding: 24px 32px; background: #f8fafc; border-top: 1px solid #e2e8f0; text-align: center; color: #64748b; font-size: 13px; }
                    .expiration { margin-top: 24px; font-size: 13px; color: #94a3b8; line-height: 1.5; }
                </style>
            </head>
            <body>
                <div class="wrapper">
                    <div class="container">
                        <div class="header">
                            <h1>My Ledger</h1>
                            <p>Premium Team Collaboration</p>
                        </div>
                        <div class="content">
                            <p class="intro">Hello,</p>
                            <p class="intro">You've been invited to collaborate on <strong>My Ledger</strong>. Join your team to start managing your books efficiently.</p>
                            
                            <div class="details-card">
                                %s
                                %s
                                %s
                                %s
                            </div>
                            
                            <div class="actions">
                                <a href="%s" class="btn btn-primary">Accept Invitation</a>
                                <a href="%s" class="btn btn-secondary">Decline</a>
                            </div>
                            
                            <div class="expiration">
                                <strong>Note:</strong> This invitation expires in 7 days. Once accepted, you'll be prompted to set up your secure password.
                            </div>
                        </div>
                        <div class="footer">
                            &copy; 2026 My Ledger. All rights reserved.<br/>
                            <span style="font-size: 11px;">This is an automated security notification.</span>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """
                .formatted(emailLine, invitedByLine, roleLine, booksSection, escapeHtml(acceptLink), escapeHtml(rejectLink));
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
