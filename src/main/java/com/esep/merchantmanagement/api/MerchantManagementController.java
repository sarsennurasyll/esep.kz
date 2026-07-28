package com.esep.merchantmanagement.api;

import com.esep.merchantmanagement.api.dto.MerchantMatchRequest;
import com.esep.merchantmanagement.api.dto.MerchantResponse;
import com.esep.merchantmanagement.api.dto.UnknownMerchantResponse;
import com.esep.merchantmanagement.interfaces.MerchantManagementService;
import com.esep.merchantmanagement.interfaces.MerchantLearningStatisticsQuery;
import com.esep.merchantmanagement.api.dto.MerchantLearningStatisticsResponse;
import com.esep.merchantresolver.model.MerchantReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP API управления неизвестными описаниями продавцов.
 */
@RestController
@RequestMapping("/api/merchants")
@Tag(name = "Merchants", description = "Управление неизвестными продавцами")
public class MerchantManagementController {

    private final MerchantManagementService merchantManagementService;
    private final MerchantLearningStatisticsQuery merchantLearningStatisticsQuery;

    public MerchantManagementController(MerchantManagementService merchantManagementService,
                                        MerchantLearningStatisticsQuery merchantLearningStatisticsQuery) {
        this.merchantManagementService = merchantManagementService;
        this.merchantLearningStatisticsQuery = merchantLearningStatisticsQuery;
    }

    @GetMapping("/learning-statistics")
    public MerchantLearningStatisticsResponse learningStatistics() {
        return MerchantLearningStatisticsResponse.from(merchantLearningStatisticsQuery.getStatistics());
    }

    @GetMapping("/unknown")
    @Operation(summary = "Получить неизвестные описания операций")
    public List<UnknownMerchantResponse> findUnknownDescriptions(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "false") boolean onlyNew,
            @RequestParam(required = false) Long minUsageCount,
            @RequestParam(required = false) java.math.BigDecimal minTotalAmount
    ) {
        return merchantManagementService.findUnknownDescriptions().stream()
                .filter(description -> query == null || query.isBlank() || description.normalizedDescription().contains(query.trim().toUpperCase()))
                .filter(description -> !onlyNew || description.newInLatestStatement())
                .filter(description -> minUsageCount == null || description.usageCount() >= minUsageCount)
                .filter(description -> minTotalAmount == null || description.totalAmount().compareTo(minTotalAmount) >= 0)
                .map(UnknownMerchantResponse::from)
                .toList();
    }

    @GetMapping
    @Operation(summary = "Получить список известных продавцов")
    public List<MerchantResponse> findMerchants() {
        return merchantManagementService.findMerchants().stream()
                .map(MerchantResponse::from)
                .toList();
    }

    @PostMapping("/match")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Связать неизвестное описание с продавцом",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Соответствие сохранено"),
                    @ApiResponse(responseCode = "404", description = "Продавец не найден"),
                    @ApiResponse(responseCode = "409", description = "Соответствие уже существует")
            }
    )
    public void match(@Valid @RequestBody MerchantMatchRequest request) {
        merchantManagementService.match(
                request.normalizedDescription(),
                new MerchantReference(request.merchantId())
        );
    }
}
