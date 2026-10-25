package com.esep.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalRequestOriginTest {

    private final LocalRequestOrigin localRequestOrigin = new LocalRequestOrigin(List.of("localhost", "127.0.0.1"));

    @Test
    void shouldAllowLocalHostNames() {
        assertThat(localRequestOrigin.isAllowedHost(request("localhost"))).isTrue();
        assertThat(localRequestOrigin.isAllowedHost(request("127.0.0.1"))).isTrue();
    }

    @Test
    void shouldRejectForeignHostName() {
        assertThat(localRequestOrigin.isAllowedHost(request("rebinding.example"))).isFalse();
    }

    @Test
    void shouldTreatRequestWithoutOriginAsNonBrowser() {
        assertThat(localRequestOrigin.isSameOriginOrNonBrowser(request("localhost"))).isTrue();
    }

    @Test
    void shouldAcceptOwnOriginOnSamePort() {
        MockHttpServletRequest request = request("localhost");
        request.addHeader("Origin", "http://127.0.0.1:8080");

        assertThat(localRequestOrigin.isSameOriginOrNonBrowser(request)).isTrue();
    }

    @Test
    void shouldRejectLocalOriginOnAnotherPort() {
        MockHttpServletRequest request = request("localhost");
        request.addHeader("Origin", "http://localhost:3000");

        assertThat(localRequestOrigin.isSameOriginOrNonBrowser(request)).isFalse();
    }

    @Test
    void shouldRejectForeignNullAndRefererOrigins() {
        MockHttpServletRequest foreignOrigin = request("localhost");
        foreignOrigin.addHeader("Origin", "https://attacker.example");
        MockHttpServletRequest nullOrigin = request("localhost");
        nullOrigin.addHeader("Origin", "null");
        MockHttpServletRequest foreignReferer = request("localhost");
        foreignReferer.addHeader("Referer", "https://attacker.example/page");

        assertThat(localRequestOrigin.isSameOriginOrNonBrowser(foreignOrigin)).isFalse();
        assertThat(localRequestOrigin.isSameOriginOrNonBrowser(nullOrigin)).isFalse();
        assertThat(localRequestOrigin.isSameOriginOrNonBrowser(foreignReferer)).isFalse();
    }

    private MockHttpServletRequest request(String host) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/statements/import");
        request.setServerName(host);
        request.setLocalPort(8080);
        return request;
    }
}
