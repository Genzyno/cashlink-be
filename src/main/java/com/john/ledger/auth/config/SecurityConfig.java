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
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/myledger-api/auth/**").permitAll()
                        .requestMatchers("/myledger-api/public/**").permitAll()
                        .requestMatchers("/myledger-api/actuator/**").permitAll()
                        .requestMatchers("/myledger-api/swagger-ui/**").permitAll()
                        .requestMatchers("/myledger-api/v3/api-docs/**").permitAll()

                        .anyRequest().authenticated()
                )

                .httpBasic(http -> http.disable())
                .formLogin(form -> form.disable());

        return httpSecurity.build();
    }
}