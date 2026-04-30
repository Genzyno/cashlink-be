package com.john.ledger.common.util;

import java.util.Optional;
import java.util.UUID;

/**
 * Holds the current request's user ID (from JWT) so services can apply permission-scope filtering
 * (e.g. "assigned" = show only books assigned to this user).
 */
public final class CurrentUserHolder {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    public static void clear() {
        USER_ID.remove();
    }

    /** Returns current user UUID if authenticated, empty otherwise. */
    public static Optional<UUID> getUserId() {
        String id = USER_ID.get();
        if (id == null || id.isBlank()) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
