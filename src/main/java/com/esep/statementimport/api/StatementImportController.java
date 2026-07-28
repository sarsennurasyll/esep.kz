package com.esep.statementimport.api;

import com.esep.entity.BankType;
import com.esep.statementimport.api.dto.StatementImportResponse;
import com.esep.statementimport.service.DefaultStatementImportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * HTTP API импорта Kaspi-выписок.
 */
@RestController
@RequestMapping("/api/statements")
@Tag(name = "Statements", description = "Импорт банковских выписок")
public class StatementImportController {

    private final DefaultStatementImportUseCase statementImportUseCase;

    public StatementImportController(DefaultStatementImportUseCase statementImportUseCase) {
        this.statementImportUseCase = statementImportUseCase;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Импортировать Kaspi PDF-выписку",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Выписка импортирована"),
                    @ApiResponse(responseCode = "400", description = "Некорректный файл", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Выписка уже импортирована", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка", content = @Content)
            }
    )
    public StatementImportResponse importStatement(
            @RequestPart("file")
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "PDF-выписка Kaspi",
                    required = true,
                    schema = @Schema(type = "string", format = "binary")
            )
            MultipartFile file,
            @RequestParam(name = "bankType", defaultValue = "KASPI") BankType bankType
    ) throws IOException {
        return StatementImportResponse.from(statementImportUseCase.importStatement(
                file.getInputStream(),
                bankType,
                file.getOriginalFilename()
        ));
    }
}
