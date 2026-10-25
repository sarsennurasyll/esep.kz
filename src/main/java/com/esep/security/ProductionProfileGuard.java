package com.esep.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Не даёт запустить приложение под профилем prod, пока не реализована Production-безопасность (ADR-0005).
 * Локальная конфигурация без входа под этим профилем не загружается.
 */
@Configuration
@Profile("prod")
public class ProductionProfileGuard {

    public ProductionProfileGuard() {
        throw new IllegalStateException(
                "Профиль prod пока не поддерживается: нет аутентификации и изоляции пользователей (ADR-0005)."
        );
    }
}
