package com.esep.moneyflow.api;

import com.esep.moneyflow.exception.OperationNotFoundException;
import com.esep.moneyflow.model.MoneyPurpose;
import com.esep.moneyflow.service.PurposeLabelingService;
import com.esep.security.LocalSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurposeLabelingController.class)
@WithMockUser
@Import(LocalSecurityConfiguration.class)
class PurposeLabelingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurposeLabelingService purposeLabelingService;

    @Test
    void shouldSetOperationPurpose() throws Exception {
        mockMvc.perform(put("/api/purposes/operations/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"purpose\":\"DEBT\"}"))
                .andExpect(status().isNoContent());

        verify(purposeLabelingService).setOperationPurpose(7L, MoneyPurpose.DEBT);
    }

    @Test
    void shouldReturnNotFoundForMissingOperation() throws Exception {
        doThrow(new OperationNotFoundException(9L)).when(purposeLabelingService).clearOperationPurpose(9L);

        mockMvc.perform(delete("/api/purposes/operations/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectRequestWithoutPurpose() throws Exception {
        mockMvc.perform(put("/api/purposes/operations/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(purposeLabelingService);
    }
}
