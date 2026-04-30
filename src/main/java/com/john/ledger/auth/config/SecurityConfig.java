// package com.john.ledger.auth.config;

// import com.john.ledger.auth.filter.JwtAuthFilter;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.web.cors.CorsConfigurationSource;

// @Configuration
// public class SecurityConfig {

//     private final JwtAuthFilter jwtAuthFilter;

//     @Autowired
//     private CorsConfigurationSource corsConfigurationSource;

//     public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
//         this.jwtAuthFilter = jwtAuthFilter;
//     }

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

//         http
//                 // Disable CSRF for JWT-based APIs
//                 .csrf(csrf -> csrf.disable())

//                 // Use CorsConfigurationSource bean (NOT separate CorsFilter to avoid conflicts)
//                 .cors(cors -> cors.configurationSource(corsConfigurationSource))

//                 // No session, JWT only
//                 .sessionManagement(session ->
//                         session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                 )

//                 // Route authorization
//                 .authorizeHttpRequests(auth -> auth
//                         .requestMatchers(
//                                 "/auth/**",
//                                 "/public/**",
//                                 "/swagger-ui/**",
//                                 "/v3/api-docs/**",
//                                 "/api-docs/**"
//                         ).permitAll()
//                         .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
//                         .anyRequest().authenticated()
//                 )

//                 // Prevent default redirect to login
//                 .exceptionHandling(ex -> ex
//                         .authenticationEntryPoint((request, response, ex1) -> {
//                             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                             response.setContentType("application/json");
//                             response.getWriter().write(
//                                     "{\"message\":\"Unauthorized\",\"statusCode\":401}"
//                             );
//                         })
//                 )

//                 // Disable default login mechanisms
//                 .formLogin(form -> form.disable())
//                 .httpBasic(basic -> basic.disable())
//                 .oauth2Login(oauth -> oauth.disable());

//         http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }
// }
