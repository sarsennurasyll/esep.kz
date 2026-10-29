package com.esep.merchantmanagement.policy;

import java.util.List;
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

    // Все шаблоны однозначные и без вложенных повторов: названия приходят из выписок, а значит, от пользователя,
    // и выражение не должно замедляться на специально подобранной строке (ReDoS).
    private static final Pattern ENTREPRENEUR_PREFIX = Pattern.compile("^(ИП|IP|И\\.П\\.)( |$)");
    // Фамилия и один-два инициала: «Тестов Т.», «Тестова А.Б.», «TESTOV T». Пробелы уже сведены к одному.
    private static final Pattern SURNAME_WITH_INITIALS = Pattern.compile("^\\p{L}+(?:-\\p{L}+)? \\p{L}\\.?(?: ?\\p{L}\\.?)?$");
    private static final Pattern TRADE_NAME_MARKER = Pattern.compile("[\\d«»\"'&]");
    // Отчество или казахская форма «сын/дочь» последним словом: «Тестович», «Тестовна», «Тестұлы», «Тестқызы».
    private static final List<String> PATRONYMIC_ENDINGS = List.of("ОВИЧ", "ЕВИЧ", "ИЧ", "ОВНА", "ЕВНА", "ИЧНА", "УЛЫ", "ҰЛЫ", "КЫЗЫ", "ҚЫЗЫ");
    // Типичные окончания фамилий; проверяются только у названия после «ИП», где одно слово чаще всего фамилия.
    private static final List<String> SURNAME_ENDINGS = List.of(
            "ОВ", "ЕВ", "ЁВ", "ИН", "ЫН", "ОВА", "ЕВА", "ЁВА", "ИНА", "ЫНА", "СКИЙ", "ЦКИЙ", "СКАЯ", "ЦКАЯ", "ЕНКО", "УК", "ЮК", "ЯН");

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
        String[] words = withoutPrefix.split(" ");
        if (SURNAME_WITH_INITIALS.matcher(withoutPrefix).matches() || endsWithPatronymic(words)) {
            return true;
        }
        if (!entrepreneur || TRADE_NAME_MARKER.matcher(withoutPrefix).find()) {
            return false;
        }
        // «ИП Тестов», «ИП Тестова Анна»: фамилия первым словом из одного-двух кириллических слов.
        return words.length <= 2 && isCyrillicWord(words[0]) && SURNAME_ENDINGS.stream().anyMatch(ending -> hasEnding(words[0], ending));
    }

    private static boolean endsWithPatronymic(String[] words) {
        String last = words[words.length - 1];
        return words.length >= 2 && isLetterWord(last) && PATRONYMIC_ENDINGS.stream().anyMatch(ending -> hasEnding(last, ending));
    }

    private static boolean hasEnding(String word, String ending) {
        return word.length() > ending.length() && word.endsWith(ending);
    }

    private static boolean isLetterWord(String word) {
        return !word.isEmpty() && word.codePoints().allMatch(Character::isLetter);
    }

    private static boolean isCyrillicWord(String word) {
        return !word.isEmpty() && word.codePoints().allMatch(codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.CYRILLIC);
    }
}
