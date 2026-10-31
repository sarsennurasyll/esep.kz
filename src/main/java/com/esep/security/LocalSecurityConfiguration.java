package com.esep.security;

import com.esep.account.service.AccountService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Защита локального режима (ADR-0002) и вход по учётной записи (ADR-0008).
 *
 * <p>Страницы и API доступны только по локальным именам хоста, изменяющие запросы защищены от подделки
 * с других сайтов. Разовая проверка, политика конфиденциальности, вход и регистрация открыты без входа;
 * полный анализ и API требуют входа.
 */
@Configuration
@Profile("!prod")
public class LocalSecurityConfiguration {

    static final int MAX_LOGIN_FAILURES = 5;
    static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(15);

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final String[] PUBLIC_PATHS = {
            "/check", "/privacy", "/login", "/register", "/css/**", "/favicon.ico", "/error", "/error/**"
    };

    @Bean
    LocalRequestOrigin localRequestOrigin(@Value("${esep.security.allowed-hosts}") List<String> allowedHosts) {
        return new LocalRequestOrigin(allowedHosts);
    }

    // Делегирующий кодировщик хранит префикс алгоритма ({bcrypt}), поэтому алгоритм можно сменить без сброса паролей.
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    LoginAttemptLimiter loginAttemptLimiter() {
        return new LoginAttemptLimiter(MAX_LOGIN_FAILURES, LOGIN_LOCK_DURATION, Clock.systemUTC());
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LocalRequestOrigin localRequestOrigin,
            LoginAttemptLimiter loginAttemptLimiter
    ) throws Exception {
        http
                .addFilterBefore(new AllowedHostFilter(localRequestOrigin), CsrfFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(sharedKnowledgeBaseChange()).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.requireCsrfProtectionMatcher(csrfRequired(localRequestOrigin)))
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler(loginAttemptLimiter))
                        .failureHandler((request, response, exception) -> {
                            String email = submittedEmail(request);
                            loginAttemptLimiter.recordFailure(email);
                            // Текст не говорит, существует ли адрес: блокировка считается по любому введённому адресу.
                            response.sendRedirect(request.getContextPath()
                                    + (loginAttemptLimiter.isLocked(email) ? "/login?locked" : "/login?error"));
                        }))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout"))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint()))
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    // База продавцов общая для всех: одна правка обычного пользователя изменила бы результаты у остальных.
    private static RequestMatcher sharedKnowledgeBaseChange() {
        return request -> {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            return !SAFE_METHODS.contains(request.getMethod())
                    && (path.startsWith("/knowledge-base/") || path.startsWith("/api/knowledge-base/")
                    || path.equals("/merchants/match") || path.equals("/api/merchants/match"));
        };
    }

    // Без входа API отвечает 401, а страницы отправляют на форму входа — независимо от заголовка Accept.
    private static AuthenticationEntryPoint authenticationEntryPoint() {
        AuthenticationEntryPoint api = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
        AuthenticationEntryPoint pages = new LoginUrlAuthenticationEntryPoint("/login");
        return (request, response, exception) -> (isApiRequest(request) ? api : pages).commence(request, response, exception);
    }

    private static SavedRequestAwareAuthenticationSuccessHandler successHandler(LoginAttemptLimiter loginAttemptLimiter) {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication
            ) throws IOException, ServletException {
                loginAttemptLimiter.recordSuccess(submittedEmail(request));
                super.onAuthenticationSuccess(request, response, authentication);
            }
        };
        handler.setDefaultTargetUrl("/");
        return handler;
    }

    private static String submittedEmail(HttpServletRequest request) {
        return AccountService.normalizeEmail(request.getParameter("email")).orElse(null);
    }

    // Формы Thymeleaf получают CSRF-токен автоматически. Клиенты API вне браузера токена не имеют, поэтому для API
    // проверяется источник запроса: чужой Origin отправляется на обязательную проверку токена и отклоняется.
    static RequestMatcher csrfRequired(LocalRequestOrigin localRequestOrigin) {
        return request -> !SAFE_METHODS.contains(request.getMethod())
                && (!isApiRequest(request) || !localRequestOrigin.isSameOriginOrNonBrowser(request));
    }

    private static boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/api/");
    }
}
