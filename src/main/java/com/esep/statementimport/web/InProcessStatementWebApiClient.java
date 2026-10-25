package com.esep.statementimport.web;

import com.esep.entity.BankType;
import com.esep.statementimport.api.StatementApiErrors;
import com.esep.statementimport.api.StatementImportController;
import com.esep.statementimport.api.StatementQueryController;
import com.esep.statementimport.api.dto.StatementImportResponse;
import com.esep.statementimport.api.dto.StatementResponse;
import com.esep.statementimport.api.dto.TransactionResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.function.Supplier;

/**
 * Вызывает API выписок в том же процессе, без HTTP-запроса к самому приложению.
 * Контракт и коды ошибок совпадают с HTTP API.
 */
@Component
public class InProcessStatementWebApiClient implements StatementWebApiClient {

    private final StatementImportController statementImportController;
    private final StatementQueryController statementQueryController;

    public InProcessStatementWebApiClient(
            StatementImportController statementImportController,
            StatementQueryController statementQueryController
    ) {
        this.statementImportController = statementImportController;
        this.statementQueryController = statementQueryController;
    }

    @Override
    public List<StatementResponse> findAllStatements() {
        return execute(statementQueryController::findAll);
    }

    @Override
    public StatementResponse findStatementById(Long statementId) {
        StatementResponse statement = execute(() -> statementQueryController.findById(statementId).getBody());
        if (statement == null) {
            throw new StatementApiException(404, "Выписка не найдена.", null);
        }
        return statement;
    }

    @Override
    public List<TransactionResponse> findTransactionsByStatementId(Long statementId) {
        return execute(() -> statementQueryController.findTransactions(statementId));
    }

    @Override
    public StatementImportResponse importStatement(MultipartFile file, BankType bankType) {
        return execute(() -> {
            try {
                return statementImportController.importStatement(file, bankType);
            } catch (java.io.IOException exception) {
                throw new StatementApiException(500, "Не удалось прочитать выбранный файл.", exception);
            }
        });
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (StatementApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new StatementApiException(
                    StatementApiErrors.statusOf(exception).value(),
                    StatementApiErrors.clientMessageOf(exception),
                    exception
            );
        }
    }
}
