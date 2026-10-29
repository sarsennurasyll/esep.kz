package com.esep.merchantmanagement.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.containsString;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantRequest;
import com.esep.entity.MerchantType;
import com.esep.security.LocalSecurityConfiguration;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(KnowledgeBaseWebController.class)
@WithMockUser
@Import(LocalSecurityConfiguration.class)
class KnowledgeBaseWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KnowledgeBaseWebApiClient knowledgeBaseWebApiClient;

    @Test
    void shouldNotOfferPersonTypeWhenAddingMerchant() throws Exception {
        when(knowledgeBaseWebApiClient.findMerchants(null, null, null)).thenReturn(List.of());
        when(knowledgeBaseWebApiClient.findCategories()).thenReturn(List.of());

        String page = mockMvc.perform(get("/knowledge-base/merchants"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String addForm = page.substring(page.indexOf("Добавить продавца"), page.indexOf("</form>", page.indexOf("Добавить продавца")));
        assertThat(addForm).contains("value=\"STORE\"").doesNotContain("value=\"PERSON\"");
    }

    @Test
    void shouldExplainEditConflict() throws Exception {
        doThrow(new MerchantApiException(409, "Conflict", null)).when(knowledgeBaseWebApiClient).updateMerchant(eq("42"), any());

        mockMvc.perform(post("/knowledge-base/merchants/42")
                        .param("name", "MAGNUM").param("merchantType", "STORE").param("categoryCode", "GROCERY").param("version", "2")
                        .with(csrf()))
                .andExpect(redirectedUrl("/knowledge-base/merchants/42"))
                .andExpect(flash().attribute("errorMessage", containsString("изменили в другом окне")));

        verify(knowledgeBaseWebApiClient).updateMerchant("42", new KnowledgeBaseMerchantRequest("MAGNUM", MerchantType.STORE, "GROCERY", 2L));
    }

    @Test
    void shouldRenderNotFoundPageForUnknownMerchant() throws Exception {
        when(knowledgeBaseWebApiClient.findMerchant("99999")).thenThrow(new MerchantApiException(404, "Not found", null));

        mockMvc.perform(get("/knowledge-base/merchants/99999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }
}
