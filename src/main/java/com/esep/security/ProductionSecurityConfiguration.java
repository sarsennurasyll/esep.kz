package com.esep.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Clock;
import java.util.List;

/**
 * Защита Production и Staging (ADR-0005, ADR-0011). Отличия от локального режима: CSRF-токен обязателен для всех
 * изменяющих запросов, включая API. HSTS Spring Security добавляет сам на запросах по HTTPS.
 */
@Configuration
@Profile("prod")
public class ProductionSecurityConfiguration {

    @Bean
    LocalRequestOrigin requestOrigin(@Value("${esep.security.allowed-hosts}") List<String> allowedHosts) {
        return new LocalRequestOrigin(allowedHosts);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    LoginAttemptLimiter loginAttemptLimiter() {
        return new LoginAttemptLimiter(LocalSecurityConfiguration.MAX_LOGIN_FAILURES, LocalSecurityConfiguration.LOGIN_LOCK_DURATION, Clock.systemUTC());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, LocalRequestOrigin requestOrigin, LoginAttemptLimiter loginAttemptLimiter) throws Exception {
        return LocalSecurityConfiguration.applyCommon(http, requestOrigin, loginAttemptLimiter).build();
    }
}
