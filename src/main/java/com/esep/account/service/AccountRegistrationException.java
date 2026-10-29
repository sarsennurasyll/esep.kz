package com.esep.account.service;

/** Регистрация отклонена по известной причине; текст для пользователя выбирает web-слой по причине. */
public class AccountRegistrationException extends RuntimeException {

    public enum Reason {
        INVALID_EMAIL,
        PASSWORD_TOO_SHORT,
        PASSWORD_TOO_LONG,
        PASSWORD_EQUALS_EMAIL,
        EMAIL_TAKEN
    }

    private final Reason reason;

    public AccountRegistrationException(Reason reason) {
        super("Account registration rejected: " + reason);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
