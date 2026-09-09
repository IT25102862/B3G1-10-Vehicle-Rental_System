package com.sliit.vrs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// We only use Spring Security's BCryptPasswordEncoder class here (not the
// full Spring Security framework) to keep the login flow simple to explain:
// plain HttpSession-based login, but passwords are still safely hashed,
// satisfying NFR 4.1 (Security and Data Privacy) in the proposal.
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
