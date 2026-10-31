package com.esep.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * Не даёт запустить профиль prod с настройками локальной разработки (ADR-0011): лучше не стартовать,
 * чем открыть сервис с известным паролем базы или с проверкой Host на локальные имена.
 */
@Configuration
@Profile("prod")
public class ProductionProfileGuard {

    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]");
    private static final Set<String> DEVELOPMENT_PASSWORDS = Set.of("", "esep", "postgres", "password");

    public ProductionProfileGuard(
            @Value("${esep.security.allowed-hosts:}") String allowedHosts,
            @Value("${spring.datasource.password:}") String databasePassword
    ) {
        if (Arrays.stream(allowedHosts.split(",", -1)).anyMatch(host -> host.isBlank() || LOCAL_HOSTS.contains(host.strip().toLowerCase(Locale.ROOT)))) {
            throw new IllegalStateException("Профиль prod: ESEP_ALLOWED_HOSTS должен содержать только публичные имена сервиса.");
        }
        if (DEVELOPMENT_PASSWORDS.contains(databasePassword)) {
            throw new IllegalStateException("Профиль prod: DB_PASSWORD не задан или совпадает с паролем для разработки.");
        }
    }
}
