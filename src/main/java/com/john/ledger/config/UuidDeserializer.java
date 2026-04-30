package com.john.ledger.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.UUID;

/**
 * Deserializes UUID from JSON. Accepts standard 36-character UUID strings.
 * Legacy numeric strings (e.g. "30", "0") from frontends that still send old Long IDs
 * are treated as null so that flows like "Add New Book" keep working during migration.
 * Other invalid strings still throw a clear error.
 */
public class UuidDeserializer extends JsonDeserializer<UUID> implements ContextualDeserializer {

    private static final String EXPECTED_FORMAT = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx (36 characters)";

    /** Legacy numeric ID (e.g. "30") → treat as null instead of failing. */
    private static boolean isLegacyNumericId(String value) {
        if (value == null || value.length() > 20) return false;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }

    @Override
    public UUID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        value = value.trim();
        if (isLegacyNumericId(value)) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            String hint = value.length() < 36
                    ? " IDs are now UUIDs. Use the UUID returned by the API (e.g. from list/get endpoints)."
                    : " Expected format: " + EXPECTED_FORMAT + ".";
            throw JsonMappingException.from(ctxt,
                    "Invalid UUID for value \"" + value + "\"." + hint);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JsonMappingException {
        return this;
    }
}
