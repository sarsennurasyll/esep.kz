package com.esep.security;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Определяет, пришёл ли запрос к локальному экземпляру Esep и с какого источника.
 * Используется в локальном режиме без входа пользователя (ADR-0002).
 */
public class LocalRequestOrigin {

    private final Set<String> allowedHosts;

    public LocalRequestOrigin(List<String> allowedHosts) {
        this.allowedHosts = allowedHosts.stream()
                .map(host -> host.strip().toLowerCase(Locale.ROOT))
                .filter(host -> !host.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Проверяет заголовок Host. Защищает локальный сервис от DNS rebinding: чужой домен,
     * указывающий на 127.0.0.1, приходит со своим именем в Host.
     */
    public boolean isAllowedHost(HttpServletRequest request) {
        return isAllowedHostName(request.getServerName());
    }

    /**
     * Проверяет, что браузер отправил запрос со страницы самого Esep или что запрос пришёл не из браузера.
     * Браузеры добавляют Origin к межсайтовым POST-запросам; внутренний HTTP-клиент web-слоя его не передаёт.
     */
    public boolean isSameOriginOrNonBrowser(HttpServletRequest request) {
        String source = request.getHeader("Origin");
        if (source == null) {
            source = request.getHeader("Referer");
        }
        return source == null || isOwnOrigin(source, request.getLocalPort());
    }

    private boolean isOwnOrigin(String source, int localPort) {
        try {
            URI uri = URI.create(source);
            if (uri.getHost() == null || !("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))) {
                return false;
            }
            int port = uri.getPort() != -1 ? uri.getPort() : ("https".equals(uri.getScheme()) ? 443 : 80);
            return isAllowedHostName(uri.getHost()) && port == localPort;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isAllowedHostName(String host) {
        return host != null && allowedHosts.contains(host.toLowerCase(Locale.ROOT));
    }
}
