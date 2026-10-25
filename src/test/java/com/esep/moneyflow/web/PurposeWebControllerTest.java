package com.esep.moneyflow.web;

import com.esep.moneyflow.model.CounterpartyLabelingItem;
import com.esep.moneyflow.model.LabelingProgress;
import com.esep.moneyflow.model.MoneyDirection;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.moneyflow.service.PurposeLabelingService;
import com.esep.security.LocalSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurposeWebController.class)
@Import(LocalSecurityConfiguration.class)
class PurposeWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurposeLabelingService purposeLabelingService;

    @Test
    void shouldRenderCounterpartiesWithPurposesAllowedForDirection() throws Exception {
        when(purposeLabelingService.findCounterparties()).thenReturn(List.of(new CounterpartyLabelingItem(
                "ТЕСТОВ Т.", "Тестов Т.", MoneyDirection.OUTFLOW, new BigDecimal("5000"), 2, BigDecimal.ZERO, null
        )));
        when(purposeLabelingService.progress()).thenReturn(new LabelingProgress(
                new BigDecimal("5000"), BigDecimal.ZERO, new BigDecimal("0.0000"), 1, 0
        ));

        mockMvc.perform(get("/purposes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Тестов Т.")))
                .andExpect(content().string(containsString("value=\"FAMILY\"")))
                .andExpect(content().string(not(containsString("value=\"INCOME_WORK\""))));
    }

    @Test
    void shouldSaveRuleWithCsrfToken() throws Exception {
        mockMvc.perform(post("/purposes/rules")
                        .param("counterpartyKey", "ТЕСТОВ Т.")
                        .param("direction", "INFLOW")
                        .param("purpose", "INCOME_WORK")
                        .with(csrf()))
                .andExpect(redirectedUrl("/purposes"));

        verify(purposeLabelingService).setRule("ТЕСТОВ Т.", MoneyDirection.INFLOW, MoneyPurpose.INCOME_WORK);
    }

    @Test
    void shouldRejectRuleWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/purposes/rules")
                        .param("counterpartyKey", "ТЕСТОВ Т.")
                        .param("direction", "INFLOW")
                        .param("purpose", "INCOME_WORK"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(purposeLabelingService);
    }
}
