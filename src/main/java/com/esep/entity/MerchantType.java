package com.esep.entity;

/**
 * Тип получателя платежа в базе знаний о продавцах.
 */
public enum MerchantType {
    STORE("Магазин"),
    PHARMACY("Аптека"),
    MOBILE_OPERATOR("Связь"),
    TAXI("Такси"),
    PUBLIC_TRANSPORT("Общественный транспорт"),
    BANK("Банк"),
    MARKETPLACE("Маркетплейс"),
    DIGITAL_SERVICE("Цифровой сервис"),
    RESTAURANT("Ресторан"),
    GOVERNMENT("Государственные услуги"),
    PERSON("Перевод человеку"),
    OTHER("Другое");

    private final String displayName;

    MerchantType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * Общая база знаний хранит только организации. Тип PERSON остаётся лишь у системного получателя
     * «Переводы людям»; имена частных лиц в общую базу не записываются.
     */
    public boolean allowedInSharedKnowledgeBase() {
        return this != PERSON;
    }
}
