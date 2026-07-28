package com.esep.merchantmanagement.web;

import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.api.dto.CategoryOptionResponse;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseAliasRequest;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantRequest;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantResponse;

import java.util.List;

public interface KnowledgeBaseWebApiClient {

    List<KnowledgeBaseMerchantResponse> findMerchants(String query, MerchantType merchantType, String categoryCode);

    KnowledgeBaseMerchantResponse findMerchant(String id);

    List<CategoryOptionResponse> findCategories();

    KnowledgeBaseMerchantResponse createMerchant(KnowledgeBaseMerchantRequest request);

    void updateMerchant(String id, KnowledgeBaseMerchantRequest request);

    void deleteMerchant(String id);

    void addAlias(String merchantId, KnowledgeBaseAliasRequest request);

    void updateAlias(String merchantId, Long aliasId, KnowledgeBaseAliasRequest request);

    void deleteAlias(String merchantId, Long aliasId);
}
