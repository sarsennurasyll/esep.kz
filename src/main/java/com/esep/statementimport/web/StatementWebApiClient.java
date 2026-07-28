package com.esep.statementimport.web;

import com.esep.entity.BankType;
import com.esep.statementimport.api.dto.StatementImportResponse;
import com.esep.statementimport.api.dto.StatementResponse;
import com.esep.statementimport.api.dto.TransactionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Клиент HTTP API выписок для web-интерфейса.
 */
public interface StatementWebApiClient {

    List<StatementResponse> findAllStatements();

    StatementResponse findStatementById(Long statementId);

    List<TransactionResponse> findTransactionsByStatementId(Long statementId);

    StatementImportResponse importStatement(MultipartFile file, BankType bankType);
}
