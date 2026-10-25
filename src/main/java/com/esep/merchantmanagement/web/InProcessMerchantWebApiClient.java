package com.esep.merchantmanagement.web;

import com.esep.merchantmanagement.api.MerchantManagementController;
import com.esep.merchantmanagement.api.dto.MerchantLearningStatisticsResponse;
import com.esep.merchantmanagement.api.dto.MerchantMatchRequest;
import com.esep.merchantmanagement.api.dto.MerchantResponse;
import com.esep.merchantmanagement.api.dto.UnknownMerchantResponse;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Вызывает API подтверждения продавцов в том же процессе, без HTTP-запроса к самому приложению.
 */
@Component
public class InProcessMerchantWebApiClient implements MerchantWebApiClient {

    private final MerchantManagementController merchantManagementController;
    private final InProcessApiCalls calls;

    public InProcessMerchantWebApiClient(MerchantManagementController merchantManagementController, Validator validator) {
        this.merchantManagementController = merchantManagementController;
        this.calls = new InProcessApiCalls(validator);
    }

    @Override
    public List<UnknownMerchantResponse> findUnknownDescriptions(String query, boolean onlyNew, Long minUsageCount, BigDecimal minTotalAmount) {
        return calls.execute(() -> merchantManagementController.findUnknownDescriptions(query, onlyNew, minUsageCount, minTotalAmount));
    }

    @Override
    public List<MerchantResponse> findMerchants() {
        return calls.execute(merchantManagementController::findMerchants);
    }

    @Override
    public void match(MerchantMatchRequest request) {
        calls.run(() -> merchantManagementController.match(calls.valid(request)));
    }

    @Override
    public MerchantLearningStatisticsResponse learningStatistics() {
        return calls.execute(merchantManagementController::learningStatistics);
    }
}
