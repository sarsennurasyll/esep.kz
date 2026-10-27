package com.esep.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalCsrfRequirementTest {

    private final RequestMatcher csrfRequired = LocalSecurityConfiguration.csrfRequired(
            new LocalRequestOrigin(List.of("localhost", "127.0.0.1"))
    );

    @Test
    void shouldRequireTokenForFormPost() {
        assertThat(csrfRequired.matches(post("/import"))).isTrue();
    }

    @Test
    void shouldRequireTokenForApiCallFromForeignOrigin() {
        MockHttpServletRequest request = post("/api/statements/import");
        request.addHeader("Origin", "https://attacker.example");

        assertThat(csrfRequired.matches(request)).isTrue();
        assertThat(csrfRequired.matches(post("/api/statements/import"))).isFalse();
    }

    private MockHttpServletRequest post(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setServerName("localhost");
        request.setLocalPort(8080);
        return request;
    }
}
