package com.esep.onetimecheck.integration;

import com.esep.statementimport.pdf.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Разовая проверка на всём приложении: реальные парсер Kaspi, классификация, распознавание продавцов
 * и web-слой. Подменено только извлечение текста из PDF — синтетическим текстом выписки.
 * Главная проверка: после запроса в базе данных не появилось ни одной строки, а в журнале нет данных выписки (ADR-0007).
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:one-time-check;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OneTimeCheckStorageIntegrationTest {

    private static final List<String> TABLES = List.of(
            "statements", "transactions", "merchants", "merchant_aliases", "categories", "counterparty_purpose_rules");

    private static final String SYNTHETIC_STATEMENT = """
            АО «Kaspi Bank», БИК CASPKZKA, www.kaspi.kz
            ВЫПИСКА
            по Kaspi Gold за период с 01.08.26 по 31.08.26
            Тестов Клиент
            Номер карты: *0000
            Доступно на 31.08.26 + 10 000,00 ₸ Валюта счета: тенге
            Краткое содержание операций по карте:
            Доступно на 01.08.26 + 1 000,00 ₸ Остаток зарплатных денег 0,00 ₸
            Пополнения + 60 000,00 ₸ Кредитный лимит 0,00 ₸
            Переводы - 20 000,00 ₸
            Переводы на Kaspi Депозит + 0,00 ₸
            Покупки - 21 500,00 ₸
            Снятия - 9 000,00 ₸
            Разное - 500,00 ₸
            Доступно на 31.08.26 + 10 000,00 ₸
            Дата Сумма Операция Детали
            31.08.26 - 500,00 ₸ Разное Плата за годовое обслуживание
            30.08.26 - 9 000,00 ₸ Снятие Банкомат Тестовый
            29.08.26 - 3 000,00 ₸ Покупка TEST DIGITAL SERVICE
            28.08.26 + 1 500,00 ₸ Покупка ТЕСТОВЫЙ МАГАЗИН
            15.08.26 - 20 000,00 ₸ Покупка ТЕСТОВЫЙ МАГАЗИН
            10.08.26 - 20 000,00 ₸ Перевод Тестов Т.
            05.08.26 + 10 000,00 ₸ Пополнение В Kaspi Банкомате
            01.08.26 + 50 000,00 ₸ Пополнение Тестов Т.
            АО «Kaspi Bank», БИК CASPKZKA, www.kaspi.kz
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private PdfTextExtractor pdfTextExtractor;

    @Test
    void shouldAnalyseStatementWithoutWritingAnythingToDatabase(CapturedOutput output) throws Exception {
        when(pdfTextExtractor.extract(any())).thenReturn(SYNTHETIC_STATEMENT);
        Map<String, Long> before = rowCounts();

        mockMvc.perform(multipart("/check")
                        .file(new MockMultipartFile("file", "statement.pdf", MediaType.APPLICATION_PDF_VALUE, "%PDF-1.4 synthetic".getBytes()))
                        .param("consent", "true")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("check-result"))
                .andExpect(content().string(containsString("операций: <span>8</span>")))
                .andExpect(content().string(containsString("Выписка не сохранена")));

        assertThat(rowCounts()).isEqualTo(before);
        assertThat(output.getAll()).doesNotContain("ТЕСТОВЫЙ МАГАЗИН", "Тестов Т.", "20 000,00");
        assertThat(before.get("statements")).isZero();
        assertThat(before.get("transactions")).isZero();
    }

    private Map<String, Long> rowCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        TABLES.forEach(table -> counts.put(table, jdbc.queryForObject("select count(*) from " + table, Long.class)));
        return counts;
    }
}
