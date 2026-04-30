package com.john.ledger.config;

import com.john.ledger.entry.entity.*;
import com.john.ledger.entry.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class DefaultDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DefaultDataSeeder.class);

    private static final String DEFAULT_BUSINESS_TYPE = "General";
    @Bean
    @Order(2)
    CommandLineRunner seedDefaultData(
            BusinessTypeRepository businessTypeRepository) {
        return args -> {
            // 1. Ensure default Business Type exists (used when users create their own business)
            seedBusinessTypeIfMissing(businessTypeRepository);
        };
    }

    private BusinessTypeEntity seedBusinessTypeIfMissing(BusinessTypeRepository repo) {
        return repo.findByBusinessType(DEFAULT_BUSINESS_TYPE).orElseGet(() -> {
            BusinessTypeEntity bt = BusinessTypeEntity.builder()
                    .businessType(DEFAULT_BUSINESS_TYPE)
                    .build();
            bt = repo.save(bt);
            log.info("Default business type seeded: {}", DEFAULT_BUSINESS_TYPE);
            return bt;
        });
    }
}
