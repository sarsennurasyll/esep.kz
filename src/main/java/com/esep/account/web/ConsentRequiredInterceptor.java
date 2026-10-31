package com.esep.account.web;

import com.esep.account.service.ConsentService;
import com.esep.security.AccountPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

/**
 * Пока пользователь не принял текущие версии обязательных документов, полный анализ ему закрыт (ADR-0010).
 * Открыты сами документы, страница согласия и аккаунт: тот, кто не согласен, должен иметь возможность удалить данные.
 */
@Configuration
public class ConsentRequiredInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    private final ObjectProvider<ConsentService> consentService;

    public ConsentRequiredInterceptor(ObjectProvider<ConsentService> consentService) {
        this.consentService = consentService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).excludePathPatterns(
                "/consent", "/privacy", "/terms", "/account", "/account/**", "/check", "/login", "/register", "/error", "/error/**", "/css/**", "/js/**", "/vendor/**", "/favicon.ico");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(request.getUserPrincipal() instanceof org.springframework.security.core.Authentication authentication)
                || !(authentication.getPrincipal() instanceof AccountPrincipal principal)
                || consentService.getObject().outstanding(principal.accountId()).isEmpty()) {
            return true;
        }
        if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"CONSENT_REQUIRED\"}");
        } else {
            response.sendRedirect(request.getContextPath() + "/consent");
        }
        return false;
    }
}
