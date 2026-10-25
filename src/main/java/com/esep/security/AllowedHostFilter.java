package com.esep.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Отклоняет запросы с заголовком Host, которого нет в списке разрешённых имён локального сервиса.
 */
class AllowedHostFilter extends OncePerRequestFilter {

    private final LocalRequestOrigin localRequestOrigin;

    AllowedHostFilter(LocalRequestOrigin localRequestOrigin) {
        this.localRequestOrigin = localRequestOrigin;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!localRequestOrigin.isAllowedHost(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Host is not allowed");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
