package com.esep.normalization.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Общие вспомогательные методы для обработки названий продавцов.
 */
public final class MerchantTextUtils {

    private static final Pattern REPEATED_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern BOUNDARY_SPECIAL_CHARACTERS = Pattern.compile("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$");

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
}
