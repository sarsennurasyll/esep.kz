package com.esep.normalization.util;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Общие вспомогательные методы для обработки названий продавцов.
 */
public final class MerchantTextUtils {

    private static final Pattern REPEATED_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern BOUNDARY_SPECIAL_CHARACTERS = Pattern.compile("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$");
    private static final Set<String> LEGAL_ENTITY_PREFIXES = Set.of("ИП", "ТОО", "TOO", "LLP");
    private static final Set<String> LOCATION_SUFFIXES = Set.of("KZ", "KAZAKHSTAN", "ALMATY", "ASTANA");

    private MerchantTextUtils() {
    }

    /**
     * Удаляет пробельные символы в начале и конце строки.
     */
    public static String trim(String value) {
        return value == null ? "" : value.strip();
    }

    /**
     * Приводит строку к верхнему регистру независимо от локали сервера.
     */
    public static String toUpper(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    /**
     * Заменяет последовательности пробельных символов одним пробелом.
     */
    public static String normalizeSpaces(String value) {
        return value == null ? "" : REPEATED_WHITESPACE.matcher(value).replaceAll(" ");
    }

    /**
     * Удаляет не буквенно-цифровые символы в начале и конце строки.
     */
    public static String removeSpecialCharacters(String value) {
        return value == null ? "" : BOUNDARY_SPECIAL_CHARACTERS.matcher(value).replaceAll("");
    }

    /**
     * Удаляет известный организационно-правовой префикс в начале названия.
     */
    public static String removeLegalEntityPrefix(String value) {
        String normalizedValue = trim(value);
        int separatorIndex = normalizedValue.indexOf(' ');

        if (separatorIndex < 0) {
            return normalizedValue;
        }

        String prefix = normalizedValue.substring(0, separatorIndex);
        return LEGAL_ENTITY_PREFIXES.contains(prefix)
                ? normalizedValue.substring(separatorIndex + 1)
                : normalizedValue;
    }

    /**
     * Удаляет номер филиала с маркером # или № в конце названия.
     */
    public static String removeBranchNumber(String value) {
        String normalizedValue = trim(value);
        String withoutHashNumber = removeTrailingNumber(normalizedValue, " #");
        return removeTrailingNumber(withoutHashNumber, " №");
    }

    /**
     * Удаляет известный географический суффикс в конце названия.
     */
    public static String removeLocationSuffix(String value) {
        String normalizedValue = trim(value);
        int separatorIndex = normalizedValue.lastIndexOf(' ');

        if (separatorIndex < 0) {
            return normalizedValue;
        }

        String suffix = normalizedValue.substring(separatorIndex + 1);
        return LOCATION_SUFFIXES.contains(suffix)
                ? normalizedValue.substring(0, separatorIndex)
                : normalizedValue;
    }

    private static String removeTrailingNumber(String value, String marker) {
        int markerIndex = value.lastIndexOf(marker);

        if (markerIndex < 0) {
            return value;
        }

        String number = value.substring(markerIndex + marker.length());
        return number.isEmpty() || !number.chars().allMatch(Character::isDigit)
                ? value
                : value.substring(0, markerIndex);
    }
}
