package com.esep.account.service;

import com.esep.account.service.AccountRegistrationException.Reason;
import com.esep.entity.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({AccountService.class, AccountServiceIntegrationTest.Encoder.class})
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class AccountServiceIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldStoreNormalizedEmailAndPasswordHashOnly() {
        accountService.register("  Test.User@Example.TEST ", "correct horse battery");

        UserAccount account = accountService.findByEmail("test.user@example.test").orElseThrow();
        assertThat(account.getEmail()).isEqualTo("test.user@example.test");
        assertThat(account.getPasswordHash()).startsWith("{bcrypt}").doesNotContain("correct horse battery");
        assertThat(passwordEncoder.matches("correct horse battery", account.getPasswordHash())).isTrue();
    }

    @Test
    void shouldRejectInvalidRegistrations() {
        accountService.register("taken@example.test", "correct horse battery");

        assertReason(() -> accountService.register("TAKEN@example.test", "another password 1"), Reason.EMAIL_TAKEN);
        assertReason(() -> accountService.register("not-an-email", "correct horse battery"), Reason.INVALID_EMAIL);
        assertReason(() -> accountService.register("a b@example.test", "correct horse battery"), Reason.INVALID_EMAIL);
        assertReason(() -> accountService.register("user@localhost", "correct horse battery"), Reason.INVALID_EMAIL);
        assertReason(() -> accountService.register("user@example.test", "short"), Reason.PASSWORD_TOO_SHORT);
        assertReason(() -> accountService.register("user@example.test", "пароль".repeat(7)), Reason.PASSWORD_TOO_LONG);
        assertReason(() -> accountService.register("user@example.test", "USER@example.test"), Reason.PASSWORD_EQUALS_EMAIL);
    }

    private static void assertReason(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, Reason reason) {
        assertThatThrownBy(call).isInstanceOfSatisfying(AccountRegistrationException.class,
                exception -> assertThat(exception.reason()).isEqualTo(reason));
    }

    @TestConfiguration
    static class Encoder {
        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
    }
}
