package com.esep.account.service;

import com.esep.account.service.AccountRegistrationException.Reason;
import com.esep.entity.UserAccount;
import com.esep.persistence.jpa.repository.UserAccountJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

/**
 * Регистрация учётных записей (ADR-0008). Хранится только адрес почты и хеш пароля.
 */
@Service
public class AccountService {

    static final int MIN_PASSWORD_LENGTH = 10;
    // BCrypt учитывает только первые 72 байта пароля; более длинный пароль отклоняется, а не обрезается молча.
    static final int MAX_PASSWORD_BYTES = 72;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    private final UserAccountJpaRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserAccountJpaRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public long register(String rawEmail, String password) {
        String email = normalizeEmail(rawEmail).orElseThrow(() -> new AccountRegistrationException(Reason.INVALID_EMAIL));
        validatePassword(password, email);
        if (accounts.existsByEmail(email)) {
            throw new AccountRegistrationException(Reason.EMAIL_TAKEN);
        }
        try {
            return accounts.saveAndFlush(UserAccount.builder().email(email).passwordHash(passwordEncoder.encode(password)).build()).getId();
        } catch (DataIntegrityViolationException exception) {
            // Одновременная регистрация того же адреса упирается в уникальный индекс.
            throw new AccountRegistrationException(Reason.EMAIL_TAKEN);
        }
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> findByEmail(String rawEmail) {
        return normalizeEmail(rawEmail).flatMap(accounts::findByEmail);
    }

    /**
     * Приводит адрес к одному виду: без пробелов по краям и в нижнем регистре. Проверка намеренно простая
     * и без регулярных выражений: подтверждение адреса письмом появится вместе с почтовым сервисом.
     */
    public static Optional<String> normalizeEmail(String rawEmail) {
        if (rawEmail == null) {
            return Optional.empty();
        }
        String email = rawEmail.strip().toLowerCase(Locale.ROOT);
        int at = email.indexOf('@');
        if (email.length() > MAX_EMAIL_LENGTH || at <= 0 || at > MAX_LOCAL_PART_LENGTH || at != email.lastIndexOf('@')) {
            return Optional.empty();
        }
        String domain = email.substring(at + 1);
        boolean validDomain = domain.indexOf('.') > 0 && !domain.endsWith(".") && !domain.contains("..");
        boolean printable = email.codePoints().noneMatch(codePoint -> Character.isWhitespace(codePoint) || Character.isISOControl(codePoint));
        return validDomain && printable ? Optional.of(email) : Optional.empty();
    }

    private static void validatePassword(String password, String email) {
        if (password == null || password.codePointCount(0, password.length()) < MIN_PASSWORD_LENGTH) {
            throw new AccountRegistrationException(Reason.PASSWORD_TOO_SHORT);
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new AccountRegistrationException(Reason.PASSWORD_TOO_LONG);
        }
        if (password.strip().equalsIgnoreCase(email)) {
            throw new AccountRegistrationException(Reason.PASSWORD_EQUALS_EMAIL);
        }
    }
}
