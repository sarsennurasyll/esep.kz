package com.esep.merchantmanagement.web;

import com.esep.security.LocalSecurityConfiguration;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KnowledgeBaseWebController.class)
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
}
