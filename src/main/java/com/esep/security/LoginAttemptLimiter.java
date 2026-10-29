package com.esep.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ограничивает подбор пароля: после нескольких неудачных попыток вход по адресу закрыт на время (ADR-0008).
 *
 * <p>Счётчик ведётся по введённому адресу независимо от того, есть ли такая учётная запись, — иначе по
 * блокировке можно было бы узнать, что адрес зарегистрирован. Состояние хранится в памяти процесса;
 * при нескольких экземплярах сервиса его нужно будет вынести в общее хранилище.
 */
public class LoginAttemptLimiter {

    private static final int CLEANUP_THRESHOLD = 10_000;

    private final int maxFailures;
    private final Duration lockDuration;
    private final Clock clock;
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public LoginAttemptLimiter(int maxFailures, Duration lockDuration, Clock clock) {
        this.maxFailures = maxFailures;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    public void recordFailure(String email) {
        if (email == null) {
            return;
        }
        Instant now = clock.instant();
        if (attempts.size() > CLEANUP_THRESHOLD) {
            attempts.values().removeIf(entry -> entry.expired(now, lockDuration));
        }
        attempts.compute(email, (key, current) -> current == null || current.expired(now, lockDuration)
                ? new Attempts(1, now)
                : new Attempts(current.failures() + 1, now));
    }

    public void recordSuccess(String email) {
        if (email != null) {
            attempts.remove(email);
        }
    }

    public boolean isLocked(String email) {
        if (email == null) {
            return false;
        }
        Attempts current = attempts.get(email);
        return current != null && current.failures() >= maxFailures && !current.expired(clock.instant(), lockDuration);
    }

    // Блокировка отсчитывается от последней неудачной попытки.
    private record Attempts(int failures, Instant lastFailure) {

        boolean expired(Instant now, Duration lockDuration) {
            return !lastFailure.plus(lockDuration).isAfter(now);
        }
    }
}
