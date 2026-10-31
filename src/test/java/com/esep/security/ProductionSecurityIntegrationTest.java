package com.esep.security;

import org.junit.jupiter.api.Test;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Всё приложение под профилем prod (ADR-0011).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "DB_URL=jdbc:h2:mem:production;DB_CLOSE_DELAY=-1",
        "DB_USERNAME=sa",
        "DB_PASSWORD=long-generated-value",
        "ESEP_ALLOWED_HOSTS=esep.example.test",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProductionSecurityIntegrationTest {

    private static final String HOST = "esep.example.test";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationContext context;

    @Test
    void shouldRejectUnknownHost() throws Exception {
        mockMvc.perform(get("/check").with(request -> { request.setServerName("attacker.example.test"); return request; })).andExpect(status().isForbidden());
        mockMvc.perform(get("/check").with(host())).andExpect(status().isOk());
    }

    @Test
    void shouldRequireCsrfTokenForApiWithoutOrigin() throws Exception {
        AccountPrincipal principal = new AccountPrincipal(1, "user@example.test", "unused", false, AuthorityUtils.createAuthorityList("ROLE_USER"));

        mockMvc.perform(delete("/api/purposes/rules/1").with(host()).with(user(principal))).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/purposes/rules/1").with(host()).with(user(principal)).with(csrf())).andExpect(status().is4xxClientError());
    }

    @Test
    void shouldSendSecurityHeadersAndHstsOverHttps() throws Exception {
        mockMvc.perform(get("/check").with(host()).secure(true))
                .andExpect(header().string("Strict-Transport-Security", containsString("max-age=")))
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self'")))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void shouldHideApiDocsAndKeepOnlyHealthPublic() throws Exception {
        mockMvc.perform(get("/actuator/health").with(host())).andExpect(status().isOk()).andExpect(content().string(containsString("UP")));
        mockMvc.perform(get("/actuator/env").with(host())).andExpect(status().is3xxRedirection());
        assertThat(context.getBeanNamesForType(OpenApiWebMvcResource.class)).isEmpty();
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor host() {
        return request -> {
            request.setServerName(HOST);
            return request;
        };
    }
}
