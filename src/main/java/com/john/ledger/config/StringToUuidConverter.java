package com.john.ledger.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Converts String to UUID for @PathVariable and @RequestParam.
 * Legacy numeric strings (e.g. "30") are treated as null so URLs with old IDs
 * don't throw; controllers/services should validate and return 400 when a required UUID is null.
 */
@Component
public class StringToUuidConverter implements Converter<String, UUID> {

    private static final String EXPECTED_FORMAT = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx (36 characters)";

    private static boolean isLegacyNumericId(String value) {
        if (value == null || value.length() > 20) return false;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }

    @Override
    public UUID convert(@NonNull String source) {
        if (source.isBlank()) {
            return null;
        }
        String value = source.trim();
        if (isLegacyNumericId(value)) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            String hint = value.length() < 36
                    ? " IDs are now UUIDs. Use the UUID returned by the API (e.g. from list/get endpoints)."
                    : " Expected format: " + EXPECTED_FORMAT + ".";
            throw new IllegalArgumentException("Invalid UUID: \"" + value + "\"." + hint);
        }
    }
}
