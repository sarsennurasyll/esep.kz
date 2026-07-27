package com.esep.statementimport.api;

import com.esep.statementimport.api.dto.StatementResponse;
import com.esep.statementimport.api.dto.TransactionResponse;
import com.esep.statementimport.query.interfaces.StatementReadQuery;
import com.esep.statementimport.query.interfaces.TransactionReadQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP API просмотра импортированных выписок и операций.
 */
@RestController
@RequestMapping("/api/statements")
@Tag(name = "Statements", description = "Просмотр импортированных банковских выписок")
public class StatementQueryController {

    private final StatementReadQuery statementReadQuery;
    private final TransactionReadQuery transactionReadQuery;

    public StatementQueryController(
            StatementReadQuery statementReadQuery,
            TransactionReadQuery transactionReadQuery
    ) {
        this.statementReadQuery = statementReadQuery;
        this.transactionReadQuery = transactionReadQuery;
    }

    @GetMapping
    @Operation(summary = "Получить список импортированных выписок")
    public List<StatementResponse> findAll() {
        return statementReadQuery.findAll().stream()
                .map(StatementResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить выписку по идентификатору",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Выписка найдена"),
                    @ApiResponse(responseCode = "404", description = "Выписка не найдена")
            }
    )
    public ResponseEntity<StatementResponse> findById(@PathVariable Long id) {
        return ResponseEntity.of(statementReadQuery.findById(id).map(StatementResponse::from));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Получить операции выписки")
    public List<TransactionResponse> findTransactions(@PathVariable Long id) {
        return transactionReadQuery.findByStatementId(id).stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
