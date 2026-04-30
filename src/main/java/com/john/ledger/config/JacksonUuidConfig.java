package com.john.ledger.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Registers custom UUID deserializer and Java 8 date/time support (LocalDateTime, etc.)
 * so responses serialize correctly and invalid UUIDs produce a clear 400 error.
 */
@Configuration
public class JacksonUuidConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            SimpleModule uuidModule = new SimpleModule("UuidModule");
            uuidModule.addDeserializer(UUID.class, new UuidDeserializer());
            builder.modules(new JavaTimeModule(), uuidModule);
        };
    }
}
