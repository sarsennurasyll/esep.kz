package com.esep.entity;

/**
 * Документы, с которыми соглашается пользователь. Изменение текста обязательного документа требует новой версии:
 * при следующем действии пользователь увидит запрос согласия.
 */
public enum LegalDocument {
    PRIVACY("2026-10-31", true),
    TERMS("2026-10-31", true),
    MERCHANT_CONTRIBUTION("2026-10-31", false);

    private final String version;
    private final boolean required;

    LegalDocument(String version, boolean required) {
        this.version = version;
        this.required = required;
    }

    public String version() {
        return version;
    }

    public boolean required() {
        return required;
    }
}
