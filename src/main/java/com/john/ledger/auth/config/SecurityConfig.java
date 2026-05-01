package com.john.ledger.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth

                        // OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // AUTH APIs
                        .requestMatchers("/auth/**").permitAll()

                        // PUBLIC APIs
                        .requestMatchers("/public/**").permitAll()

                        // actuator
                        .requestMatchers("/actuator/**").permitAll()

                        // swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // all others
                        .anyRequest().authenticated()
                )

                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}