package com.esep.merchantmanagement.policy;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Отличает имя человека от торгового названия до того, как описание попадёт в общую базу продавцов (ADR-0007).
 *
 * <p>ИП по Гражданскому кодексу РК — физическое лицо, поэтому «ИП Тестов Т.» — персональные данные,
 * а «ИП Gamer» — торговое название. Правила намеренно осторожные: при сомнении название считается именем
 * человека и в общую базу не попадает. Ошибка в эту сторону стоит одной неразмеченной покупки,
 * ошибка в обратную — имени человека в общей базе.
 */
public final class PersonalNameDetector {

    private static final Pattern ENTREPRENEUR_PREFIX = Pattern.compile("^(ИП|IP|И\\.П\\.)(\\s+|$)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    // Фамилия и один-два инициала: «Тестов Т.», «Тестова А.Б.», «TESTOV T».
    private static final Pattern SURNAME_WITH_INITIALS = Pattern.compile(
            "^\\p{L}+(?:-\\p{L}+)?\\s+\\p{L}\\.?(?:\\s?\\p{L}\\.?)?$");
    // Отчество или казахская форма «сын/дочь» последним словом: «Тестович», «Тестовна», «Тестұлы», «Тестқызы».
    private static final Pattern PATRONYMIC = Pattern.compile(
            ".*\\s\\p{L}*(ОВИЧ|ЕВИЧ|ИЧ|ОВНА|ЕВНА|ИЧНА|УЛЫ|ҰЛЫ|КЫЗЫ|ҚЫЗЫ)$");
    // Типичные окончания фамилий; проверяются только у названия после «ИП», где одно слово чаще всего фамилия.
    private static final Pattern SURNAME_ENDING = Pattern.compile(
            "^\\p{IsCyrillic}+(ОВ|ЕВ|ЁВ|ИН|ЫН|ОВА|ЕВА|ЁВА|ИНА|ЫНА|СКИЙ|ЦКИЙ|СКАЯ|ЦКАЯ|ЕНКО|УК|ЮК|ЯН)$");
    private static final Pattern TRADE_NAME_MARKERS = Pattern.compile(".*[\\d«»\"'&].*");

    private PersonalNameDetector() {
    }

    public static boolean looksLikePersonalName(String rawName) {
        if (rawName == null) {
            return false;
        }
        String name = rawName.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        boolean entrepreneur = ENTREPRENEUR_PREFIX.matcher(name).find();
        String withoutPrefix = ENTREPRENEUR_PREFIX.matcher(name).replaceFirst("").strip();
        if (withoutPrefix.isEmpty()) {
            return entrepreneur;
        }
        if (SURNAME_WITH_INITIALS.matcher(withoutPrefix).matches() || PATRONYMIC.matcher(withoutPrefix).matches()) {
            return true;
        }
        if (!entrepreneur || TRADE_NAME_MARKERS.matcher(withoutPrefix).matches()) {
            return false;
        }
        String[] words = withoutPrefix.split(" ");
        // «ИП Тестов», «ИП Тестова Анна»: фамилия первым словом из одного-двух кириллических слов.
        return words.length <= 2 && SURNAME_ENDING.matcher(words[0]).matches();
    }
}
