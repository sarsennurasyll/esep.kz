package com.esep.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.Set;

/**
 * Защита локального режима (ADR-0002): вход пользователя не требуется, но страницы и API
 * доступны только по локальным именам хоста, а изменяющие запросы защищены от подделки с других сайтов.
 */
@Configuration
@Profile("!prod")
public class LocalSecurityConfiguration {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Bean
    LocalRequestOrigin localRequestOrigin(@Value("${esep.security.allowed-hosts}") List<String> allowedHosts) {
        return new LocalRequestOrigin(allowedHosts);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, LocalRequestOrigin localRequestOrigin) throws Exception {
        http
                .addFilterBefore(new AllowedHostFilter(localRequestOrigin), CsrfFilter.class)
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .csrf(csrf -> csrf.requireCsrfProtectionMatcher(csrfRequired(localRequestOrigin)))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }

    // Формы Thymeleaf получают CSRF-токен автоматически. Web-слой вызывает /api по HTTP без токена,
    // поэтому для API проверяется источник запроса: чужой Origin отправляется на обязательную проверку токена и отклоняется.
    private RequestMatcher csrfRequired(LocalRequestOrigin localRequestOrigin) {
        return request -> !SAFE_METHODS.contains(request.getMethod())
                && (!isApiRequest(request) || !localRequestOrigin.isSameOriginOrNonBrowser(request));
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/api/");
    }
}
