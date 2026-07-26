package com.esep.parser.model;

import com.esep.entity.BankType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;

/**
 * Метаданные файла, передаваемые в парсер банковской выписки.
 */
@Builder
public record ParserContext(
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 20) String extension,
        @NotNull BankType bankType,
        @NotNull Instant uploadedAt
) {
}
