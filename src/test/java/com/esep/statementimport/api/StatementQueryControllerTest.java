package com.esep.statementimport.api;

import com.esep.entity.BankType;
import com.esep.entity.TransactionType;
import com.esep.statementimport.query.interfaces.StatementReadQuery;
import com.esep.statementimport.query.interfaces.TransactionReadQuery;
import com.esep.statementimport.query.model.StatementQueryResult;
import com.esep.statementimport.query.model.TransactionQueryResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatementQueryController.class)
class StatementQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatementReadQuery statementReadQuery;

    @MockitoBean
    private TransactionReadQuery transactionReadQuery;

    @Test
    void shouldReturnImportedStatements() throws Exception {
        when(statementReadQuery.findAll()).thenReturn(List.of(statementResult()));

        mockMvc.perform(get("/api/statements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].bank").value("KASPI"))
                .andExpect(jsonPath("$[0].originalFileName").value("july.pdf"))
                .andExpect(jsonPath("$[0].periodFrom").value("2026-07-01"))
                .andExpect(jsonPath("$[0].periodTo").value("2026-07-31"))
                .andExpect(jsonPath("$[0].transactionCount").value(2))
                .andExpect(jsonPath("$[0].importedAt").value("2026-07-31T10:15:30Z"));
    }

    @Test
    void shouldReturnStatementById() throws Exception {
        when(statementReadQuery.findById(42L)).thenReturn(Optional.of(statementResult()));

        mockMvc.perform(get("/api/statements/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.originalFileName").value("july.pdf"));
    }

    @Test
    void shouldReturnNotFoundForUnknownStatement() throws Exception {
        when(statementReadQuery.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/statements/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnStatementTransactions() throws Exception {
        when(transactionReadQuery.findByStatementId(42L)).thenReturn(List.of(
                new TransactionQueryResult(
                        LocalDate.of(2026, 7, 12),
                        "MAGNUM CASH&CARRY",
                        "MAGNUM",
                        "GROCERY",
                        new BigDecimal("-14500.00"),
                        "KZT",
                        TransactionType.EXPENSE
                ),
                new TransactionQueryResult(
                        LocalDate.of(2026, 7, 13),
                        "UNKNOWN SHOP",
                        null,
                        "UNCATEGORIZED",
                        new BigDecimal("-100.00"),
                        "KZT",
                        TransactionType.EXPENSE
                )
        ));

        mockMvc.perform(get("/api/statements/42/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-07-12"))
                .andExpect(jsonPath("$[0].description").value("MAGNUM CASH&CARRY"))
                .andExpect(jsonPath("$[0].merchant").value("MAGNUM"))
                .andExpect(jsonPath("$[0].category").value("GROCERY"))
                .andExpect(jsonPath("$[0].amount").value(-14500.00))
                .andExpect(jsonPath("$[0].currency").value("KZT"))
                .andExpect(jsonPath("$[0].transactionType").value("EXPENSE"))
                .andExpect(jsonPath("$[1].merchant").doesNotExist())
                .andExpect(jsonPath("$[1].category").value("UNCATEGORIZED"));
    }

    private StatementQueryResult statementResult() {
        return new StatementQueryResult(
                42L,
                BankType.KASPI,
                "july.pdf",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                2,
                Instant.parse("2026-07-31T10:15:30Z")
        );
    }
}
