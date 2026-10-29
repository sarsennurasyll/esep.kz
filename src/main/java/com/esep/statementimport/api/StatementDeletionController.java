package com.esep.statementimport.api;

import com.esep.common.api.ApiErrorResponse;
import com.esep.statementimport.api.dto.StatementDeletionResponse;
import com.esep.statementimport.exception.StatementDeletionMismatchException;
import com.esep.statementimport.exception.StatementNotFoundException;
import com.esep.statementimport.service.StatementDeletionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP API удаления выписки вместе с операциями.
 */
@RestController
@RequestMapping("/api/statements")
@Tag(name = "Statements", description = "Удаление импортированных выписок")
public class StatementDeletionController {

    static final String DELETION_MISMATCH_CODE = "STATEMENT_DELETION_MISMATCH";

    private final StatementDeletionService deletionService;

    public StatementDeletionController(StatementDeletionService deletionService) {
        this.deletionService = deletionService;
    }

    @GetMapping("/{id}/deletion")
    @Operation(summary = "Какие выписки и сколько операций будут удалены вместе с выпиской")
    public StatementDeletionResponse deletionPlan(@PathVariable long id) {
        return StatementDeletionResponse.from(deletionService.plan(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить выписку вместе с операциями",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Выписки и их операции удалены"),
                    @ApiResponse(responseCode = "404", description = "Выписка не найдена"),
                    @ApiResponse(responseCode = "409", description = "Переданный набор выписок не совпадает с планом удаления")
            }
    )
    public StatementDeletionResponse delete(
            @PathVariable long id,
            @Parameter(description = "Все выписки из плана удаления; по умолчанию — только выбранная")
            @RequestParam(name = "statementIds", required = false) List<Long> statementIds
    ) {
        return StatementDeletionResponse.from(deletionService.delete(id, statementIds == null ? List.of(id) : statementIds));
    }

    @ExceptionHandler(StatementNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("Выписка не найдена"));
    }

    @ExceptionHandler(StatementDeletionMismatchException.class)
    public ResponseEntity<ApiErrorResponse> mismatch() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
                "Вместе с этой выпиской нужно удалить выписки, импортированные после неё за пересекающийся период. "
                        + "Получите план удаления и подтвердите весь набор.",
                DELETION_MISMATCH_CODE
        ));
    }
}
