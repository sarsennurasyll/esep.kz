package com.esep.merchantmanagement.api;

import com.esep.security.LocalSecurityConfiguration;
import org.springframework.context.annotation.Import;
import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.exception.MerchantVersionConflictException;
import com.esep.merchantmanagement.model.KnowledgeBaseMerchant;
import com.esep.merchantmanagement.model.KnowledgeBaseMerchantCommand;
import com.esep.merchantmanagement.service.DefaultMerchantManagementService;
import com.esep.merchantmanagement.service.KnowledgeBaseMerchantService;
import com.esep.merchantresolver.model.MerchantReference;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KnowledgeBaseMerchantController.class)
@WithMockUser
@Import(LocalSecurityConfiguration.class)
class KnowledgeBaseMerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KnowledgeBaseMerchantService knowledgeBaseMerchantService;

    @MockitoBean
    private DefaultMerchantManagementService merchantManagementService;

    @Test
    void shouldReturnMerchantsWithCategoryAndAliases() throws Exception {
        when(knowledgeBaseMerchantService.findAll(null, null, null)).thenReturn(List.of(merchant("42")));

        mockMvc.perform(get("/api/knowledge-base/merchants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("42"))
                .andExpect(jsonPath("$[0].categoryCode").value("GROCERY"))
                .andExpect(jsonPath("$[0].transactionCount").value(4))
                .andExpect(jsonPath("$[0].version").value(3));
    }

    @Test
    void shouldCreateMerchant() throws Exception {
        when(knowledgeBaseMerchantService.create(any())).thenReturn(new MerchantReference("42"));
        when(knowledgeBaseMerchantService.findById(eq("42"))).thenReturn(merchant("42"));

        mockMvc.perform(post("/api/knowledge-base/merchants")
                        .contentType("application/json")
                        .content("{\"name\":\"Small\",\"merchantType\":\"STORE\",\"categoryCode\":\"GROCERY\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("MAGNUM"));
    }

    @Test
    void shouldRejectUpdateOfOutdatedVersion() throws Exception {
        doThrow(new MerchantVersionConflictException()).when(knowledgeBaseMerchantService).update(eq("42"), any());

        mockMvc.perform(put("/api/knowledge-base/merchants/42")
                        .contentType("application/json")
                        .content("{\"name\":\"MAGNUM\",\"merchantType\":\"STORE\",\"categoryCode\":\"GROCERY\",\"version\":2}"))
                .andExpect(status().isConflict());

        verify(knowledgeBaseMerchantService).update("42", new KnowledgeBaseMerchantCommand("MAGNUM", MerchantType.STORE, "GROCERY", 2L));
    }

    private KnowledgeBaseMerchant merchant(String id) {
        return new KnowledgeBaseMerchant(new MerchantReference(id), "MAGNUM", "MAGNUM", MerchantType.STORE,
                "GROCERY", "Продукты", 4, new BigDecimal("12000.00"),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 2), List.of(), 3);
    }
}
