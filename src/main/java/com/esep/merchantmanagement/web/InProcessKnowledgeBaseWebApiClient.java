package com.esep.merchantmanagement.web;

import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.api.KnowledgeBaseMerchantController;
import com.esep.merchantmanagement.api.dto.CategoryOptionResponse;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseAliasRequest;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantRequest;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantResponse;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Вызывает API базы знаний в том же процессе, без HTTP-запроса к самому приложению.
 */
@Component
public class InProcessKnowledgeBaseWebApiClient implements KnowledgeBaseWebApiClient {

    private final KnowledgeBaseMerchantController controller;
    private final InProcessApiCalls calls;

    public InProcessKnowledgeBaseWebApiClient(KnowledgeBaseMerchantController controller, Validator validator) {
        this.controller = controller;
        this.calls = new InProcessApiCalls(validator);
    }

    @Override
    public List<KnowledgeBaseMerchantResponse> findMerchants(String query, MerchantType merchantType, String categoryCode) {
        return calls.execute(() -> controller.findMerchants(blankToNull(query), merchantType, blankToNull(categoryCode)));
    }

    @Override
    public KnowledgeBaseMerchantResponse findMerchant(String id) {
        return calls.execute(() -> controller.findMerchant(id));
    }

    @Override
    public List<CategoryOptionResponse> findCategories() {
        return calls.execute(controller::findCategories);
    }

    @Override
    public KnowledgeBaseMerchantResponse createMerchant(KnowledgeBaseMerchantRequest request) {
        return calls.execute(() -> controller.createMerchant(calls.valid(request)));
    }

    @Override
    public void updateMerchant(String id, KnowledgeBaseMerchantRequest request) {
        calls.run(() -> controller.updateMerchant(id, calls.valid(request)));
    }

    @Override
    public void deleteMerchant(String id) {
        calls.run(() -> controller.deleteMerchant(id));
    }

    @Override
    public void addAlias(String merchantId, KnowledgeBaseAliasRequest request) {
        calls.run(() -> controller.addAlias(merchantId, calls.valid(request)));
    }

    @Override
    public void updateAlias(String merchantId, Long aliasId, KnowledgeBaseAliasRequest request) {
        calls.run(() -> controller.updateAlias(merchantId, aliasId, calls.valid(request)));
    }

    @Override
    public void deleteAlias(String merchantId, Long aliasId) {
        calls.run(() -> controller.deleteAlias(merchantId, aliasId));
    }

    // HTTP-клиент не передавал пустые параметры; прямой вызов сохраняет это поведение.
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
