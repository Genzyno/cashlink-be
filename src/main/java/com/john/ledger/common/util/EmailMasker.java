package com.john.ledger.common.util;

/**
 * Masks email addresses for display – a little more masked (e.g. j***e@example.com).
 */
public final class EmailMasker {

    private EmailMasker() {}

    /**
     * A little more masked: first 1–2 chars + "***" + last 1–2 chars of local part, domain as-is.
     * Examples: jane@example.com -> ja***e@example.com, johnsmith@gmail.com -> jo***th@gmail.com
     */
    public static String mask(String email) {
        if (email == null || email.isBlank()) return "***";
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 0 || at >= trimmed.length() - 1) return "***";
        String local = trimmed.substring(0, at);
        String domain = trimmed.substring(at + 1);
        if (local.isEmpty() || domain.isEmpty()) return "***";
        String maskedLocal;
        int len = local.length();
        if (len == 1) {
            maskedLocal = local + "***";
        } else if (len == 2) {
            maskedLocal = local.charAt(0) + "***" + local.charAt(1);
        } else if (len <= 4) {
            maskedLocal = local.charAt(0) + "***" + local.charAt(len - 1);
        } else {
            maskedLocal = local.substring(0, 2) + "***" + local.substring(len - 2);
        }
        return maskedLocal + "@" + domain;
    }
}
