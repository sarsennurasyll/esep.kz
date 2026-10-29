package com.esep.onetimecheck.web;

import com.esep.analytics.model.MonthlyMoneyFlow;
import com.esep.entity.BankType;
import com.esep.moneyflow.model.MoneyFlowReport;
import com.esep.moneyflow.service.MoneyFlowReportCalculator;
import com.esep.moneyflow.model.MoneyFlowTotal;
import com.esep.moneyflow.model.MoneyFlowType;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.onetimecheck.model.CategorySpending;
import com.esep.onetimecheck.model.OneTimeCheckReport;
import com.esep.onetimecheck.service.OneTimeCheckService;
import com.esep.security.LocalSecurityConfiguration;
import com.esep.statementimport.model.ImportErrorCode;
import com.esep.statementimport.pdf.PdfExtractionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OneTimeCheckWebController.class)
@Import(LocalSecurityConfiguration.class)
class OneTimeCheckWebControllerTest {

    private static final MockMultipartFile FILE = new MockMultipartFile("file", "statement.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OneTimeCheckService oneTimeCheckService;

    @Test
    void shouldOfferUploadWithConsentAndPrivacyLink() throws Exception {
        mockMvc.perform(get("/check"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"consent\"")))
                .andExpect(content().string(containsString("href=\"/privacy\"")));
        mockMvc.perform(get("/privacy")).andExpect(status().isOk());
    }

    @Test
    void shouldRenderReportWithoutCachingIt() throws Exception {
        when(oneTimeCheckService.check(any(), eq(BankType.KASPI))).thenReturn(report());

        mockMvc.perform(multipart("/check").file(FILE).param("consent", "true").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("check-result"))
                .andExpect(content().string(containsString("Выписка не сохранена")))
                .andExpect(content().string(containsString("1500")))
                .andExpect(content().string(containsString("<td>Игры</td>")))
                .andExpect(header().string("Cache-Control", containsString("no-store")));
    }

    @Test
    void shouldRefuseToProcessStatementWithoutConsent() throws Exception {
        mockMvc.perform(multipart("/check").file(FILE).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(OneTimeCheckWebController.CONSENT_REQUIRED)));

        verifyNoInteractions(oneTimeCheckService);
    }

    @Test
    void shouldRejectUploadWithoutCsrfToken() throws Exception {
        mockMvc.perform(multipart("/check").file(FILE).param("consent", "true"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(oneTimeCheckService);
    }

    @Test
    void shouldExplainReasonByErrorCode() throws Exception {
        when(oneTimeCheckService.check(any(), eq(BankType.KASPI)))
                .thenThrow(new PdfExtractionException(ImportErrorCode.PDF_ENCRYPTED, "encrypted", null));

        mockMvc.perform(multipart("/check").file(FILE).param("consent", "true").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("check"))
                .andExpect(content().string(containsString("PDF защищён паролем")));
    }

    private static OneTimeCheckReport report() {
        MoneyFlowReportCalculator calculator = new MoneyFlowReportCalculator();
        MoneyFlowReport moneyFlow = calculator.calculate(List.of(
                new MoneyFlowTotal(MoneyFlowType.PURCHASE, MoneyPurpose.UNKNOWN, BigDecimal.ZERO, new BigDecimal("1500"), 2)));
        return new OneTimeCheckReport(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), 2, moneyFlow,
                List.of(new MonthlyMoneyFlow(YearMonth.of(2026, 7), moneyFlow, true)), 1, 1,
                List.of(new CategorySpending("Игры", new BigDecimal("1500"))));
    }
}
