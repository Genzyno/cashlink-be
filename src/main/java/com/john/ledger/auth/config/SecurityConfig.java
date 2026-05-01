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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                .httpBasic(httpBasic -> httpBasic.disable())   // REMOVE POPUP
                .formLogin(form -> form.disable())             // REMOVE LOGIN PAGE

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/myledger-api/auth/**",
                                "/myledger-api/public/**",
                                "/myledger-api/actuator/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}