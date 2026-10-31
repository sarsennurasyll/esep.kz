package com.esep.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionProfileGuardTest {

    private final ApplicationContextRunner production = new ApplicationContextRunner()
            .withUserConfiguration(ProductionProfileGuard.class)
            .withPropertyValues("spring.profiles.active=prod");

    @Test
    void shouldStartWithProductionSettings() {
        production.withPropertyValues("esep.security.allowed-hosts=esep.example.test", "spring.datasource.password=long-generated-value")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void shouldRefuseLocalHosts() {
        production.withPropertyValues("esep.security.allowed-hosts=esep.example.test,localhost", "spring.datasource.password=long-generated-value")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void shouldRefuseMissingHosts() {
        production.withPropertyValues("spring.datasource.password=long-generated-value")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void shouldRefuseDevelopmentDatabasePassword() {
        production.withPropertyValues("esep.security.allowed-hosts=esep.example.test", "spring.datasource.password=esep")
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
