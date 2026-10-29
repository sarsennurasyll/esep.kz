package com.esep.statementimport.api;

import com.esep.security.LocalSecurityConfiguration;
import com.esep.statementimport.exception.StatementDeletionMismatchException;
import com.esep.statementimport.exception.StatementNotFoundException;
import com.esep.statementimport.model.StatementDeletionPlan;
import com.esep.statementimport.service.StatementDeletionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatementDeletionController.class)
@Import(LocalSecurityConfiguration.class)
class StatementDeletionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatementDeletionService deletionService;

    @Test
    void shouldReturnDeletionPlan() throws Exception {
        when(deletionService.plan(1L)).thenReturn(new StatementDeletionPlan(1L, List.of(1L, 2L), 5));

        mockMvc.perform(get("/api/statements/1/deletion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statementIds.length()").value(2))
                .andExpect(jsonPath("$.operationCount").value(5));
    }

    @Test
    void shouldDeleteOnlySelectedStatementByDefault() throws Exception {
        when(deletionService.delete(2L, List.of(2L))).thenReturn(new StatementDeletionPlan(2L, List.of(2L), 3));

        mockMvc.perform(delete("/api/statements/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationCount").value(3));
    }

    @Test
    void shouldRejectUnconfirmedDependentStatements() throws Exception {
        when(deletionService.delete(1L, List.of(1L)))
                .thenThrow(new StatementDeletionMismatchException(new StatementDeletionPlan(1L, List.of(1L, 2L), 5)));

        mockMvc.perform(delete("/api/statements/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(StatementDeletionController.DELETION_MISMATCH_CODE));
    }

    @Test
    void shouldReturnNotFoundForMissingStatement() throws Exception {
        when(deletionService.plan(9L)).thenThrow(new StatementNotFoundException(9L));

        mockMvc.perform(get("/api/statements/9/deletion")).andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectDeletionFromForeignOrigin() throws Exception {
        mockMvc.perform(delete("/api/statements/2").header("Origin", "https://attacker.example"))
                .andExpect(status().isForbidden());
    }
}
