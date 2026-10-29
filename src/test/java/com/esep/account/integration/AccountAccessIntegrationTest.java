package com.esep.account.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Регистрация, вход, ограничение подбора пароля и доступ к страницам на всём приложении (ADR-0008).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:accounts;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AccountAccessIntegrationTest {

    private static final String PASSWORD = "correct horse battery";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldKeepFullAnalysisBehindLoginAndCheckPublic() throws Exception {
        mockMvc.perform(get("/")).andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/statements")).andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/api/statements")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/check")).andExpect(status().isOk()).andExpect(content().string(containsString("Войти")));
        mockMvc.perform(get("/privacy")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    void shouldRegisterLogInAndLogOut() throws Exception {
        register("flow@example.test");

        MvcResult login = mockMvc.perform(post("/login").param("email", "Flow@Example.test ").param("password", PASSWORD).with(csrf()))
                .andExpect(redirectedUrl("/"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession();

        mockMvc.perform(get("/account").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("flow@example.test")))
                .andExpect(content().string(containsString("Аккаунт")));
        mockMvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/login?logout"));
        mockMvc.perform(get("/account").session(session)).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldRequireConsentAndMatchingPasswords() throws Exception {
        mockMvc.perform(post("/register").param("email", "consent@example.test").param("password", PASSWORD)
                        .param("passwordConfirmation", PASSWORD).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("политикой конфиденциальности")));
        mockMvc.perform(post("/register").param("email", "consent@example.test").param("password", PASSWORD)
                        .param("passwordConfirmation", PASSWORD + "!").param("consent", "true").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Пароли не совпадают")));
        mockMvc.perform(post("/login").param("email", "consent@example.test").param("password", PASSWORD).with(csrf()))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void shouldLockLoginAfterRepeatedFailuresWithoutRevealingWhetherAccountExists() throws Exception {
        register("locked@example.test");

        for (int attempt = 1; attempt < 5; attempt++) {
            mockMvc.perform(post("/login").param("email", "locked@example.test").param("password", "wrong password").with(csrf()))
                    .andExpect(redirectedUrl("/login?error"));
            mockMvc.perform(post("/login").param("email", "missing@example.test").param("password", "wrong password").with(csrf()))
                    .andExpect(redirectedUrl("/login?error"));
        }
        mockMvc.perform(post("/login").param("email", "locked@example.test").param("password", "wrong password").with(csrf()))
                .andExpect(redirectedUrl("/login?locked"));
        mockMvc.perform(post("/login").param("email", "missing@example.test").param("password", "wrong password").with(csrf()))
                .andExpect(redirectedUrl("/login?locked"));

        // Даже правильный пароль не пускает, пока адрес заблокирован.
        mockMvc.perform(post("/login").param("email", "locked@example.test").param("password", PASSWORD).with(csrf()))
                .andExpect(redirectedUrl("/login?locked"));
    }

    @Test
    void shouldRejectLoginWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/login").param("email", "csrf@example.test").param("password", PASSWORD))
                .andExpect(status().isForbidden());
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/register").param("email", email).param("password", PASSWORD)
                        .param("passwordConfirmation", PASSWORD).param("consent", "true").with(csrf()))
                .andExpect(redirectedUrl("/login"));
    }
}
