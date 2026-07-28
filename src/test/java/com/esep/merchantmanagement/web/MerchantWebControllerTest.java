package com.esep.merchantmanagement.web;

import com.esep.merchantmanagement.api.dto.MerchantResponse;
import com.esep.merchantmanagement.api.dto.UnknownMerchantResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MerchantWebController.class)
class MerchantWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MerchantWebApiClient merchantWebApiClient;

    @Test
    void shouldRenderUnknownMerchantsPage() throws Exception {
        when(merchantWebApiClient.findUnknownDescriptions()).thenReturn(List.of(
                new UnknownMerchantResponse("UNKNOWN SHOP", 2, "Unknown Shop")
        ));
        when(merchantWebApiClient.findMerchants()).thenReturn(List.of(
                new MerchantResponse("42", "MAGNUM")
        ));

        mockMvc.perform(get("/merchants"))
                .andExpect(status().isOk())
                .andExpect(view().name("merchants"))
                .andExpect(model().attributeExists("unknownDescriptions"))
                .andExpect(model().attributeExists("merchants"));
    }

    @Test
    void shouldRedirectAfterMatch() throws Exception {
        doNothing().when(merchantWebApiClient).match(any());

        mockMvc.perform(post("/merchants/match")
                        .param("normalizedDescription", "UNKNOWN SHOP")
                        .param("merchantId", "42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/merchants"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
