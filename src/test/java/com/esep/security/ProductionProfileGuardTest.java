package com.esep.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionProfileGuardTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ProductionProfileGuard.class, LocalSecurityConfiguration.class)
            .withPropertyValues("esep.security.allowed-hosts=localhost");

    @Test
    void shouldRefuseToStartUnderProductionProfile() {
        contextRunner.withPropertyValues("spring.profiles.active=prod")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void shouldNotLoadLocalSecurityUnderProductionProfile() {
        new ApplicationContextRunner()
                .withUserConfiguration(LocalSecurityConfiguration.class)
                .withPropertyValues("spring.profiles.active=prod", "esep.security.allowed-hosts=localhost")
                .run(context -> assertThat(context).doesNotHaveBean(LocalSecurityConfiguration.class));
    }

    @Test
    void shouldNotActivateGuardInLocalMode() {
        new ApplicationContextRunner()
                .withUserConfiguration(ProductionProfileGuard.class)
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ProductionProfileGuard.class));
    }
}
