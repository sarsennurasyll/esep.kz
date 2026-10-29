package com.esep.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptLimiterTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-30T10:00:00Z"));
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(3, Duration.ofMinutes(15), clock);

    @Test
    void shouldLockAfterMaxFailuresAndUnlockAfterDuration() {
        limiter.recordFailure("user@example.test");
        limiter.recordFailure("user@example.test");
        assertThat(limiter.isLocked("user@example.test")).isFalse();

        limiter.recordFailure("user@example.test");
        assertThat(limiter.isLocked("user@example.test")).isTrue();
        assertThat(limiter.isLocked("other@example.test")).isFalse();

        clock.advance(Duration.ofMinutes(15));
        assertThat(limiter.isLocked("user@example.test")).isFalse();
    }

    @Test
    void shouldResetAfterSuccessfulLogin() {
        limiter.recordFailure("user@example.test");
        limiter.recordFailure("user@example.test");
        limiter.recordSuccess("user@example.test");
        limiter.recordFailure("user@example.test");

        assertThat(limiter.isLocked("user@example.test")).isFalse();
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
