package com.esep.merchantmanagement.api;

import com.esep.merchantmanagement.interfaces.MerchantManagementService;
import com.esep.merchantmanagement.model.MerchantSummary;
import com.esep.merchantmanagement.model.UnknownMerchantDescription;
import com.esep.merchantresolver.model.MerchantReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantManagementController.class)
class MerchantManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MerchantManagementService merchantManagementService;

    @Test
    void shouldReturnUnknownDescriptions() throws Exception {
        when(merchantManagementService.findUnknownDescriptions()).thenReturn(List.of(
                new UnknownMerchantDescription("UNKNOWN SHOP", 3, "Unknown Shop")
        ));

        mockMvc.perform(get("/api/merchants/unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].normalizedDescription").value("UNKNOWN SHOP"))
                .andExpect(jsonPath("$[0].usageCount").value(3))
                .andExpect(jsonPath("$[0].exampleDescription").value("Unknown Shop"));
    }

    @Test
    void shouldReturnMerchants() throws Exception {
        when(merchantManagementService.findMerchants()).thenReturn(List.of(
                new MerchantSummary(new MerchantReference("42"), "MAGNUM")
        ));

        mockMvc.perform(get("/api/merchants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("42"))
                .andExpect(jsonPath("$[0].displayName").value("MAGNUM"));
    }

    @Test
    void shouldCreateMerchantMatch() throws Exception {
        MerchantReference merchantReference = new MerchantReference("42");
        doNothing().when(merchantManagementService).match(eq("UNKNOWN SHOP"), eq(merchantReference));

        mockMvc.perform(post("/api/merchants/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"normalizedDescription":"UNKNOWN SHOP","merchantId":"42"}
                                """))
                .andExpect(status().isCreated());
    }
}
