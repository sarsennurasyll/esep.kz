package com.esep.merchantmanagement.api;

import com.esep.entity.MerchantType;
import com.esep.merchantmanagement.api.dto.CategoryOptionResponse;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseAliasRequest;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantRequest;
import com.esep.merchantmanagement.api.dto.KnowledgeBaseMerchantResponse;
import com.esep.merchantmanagement.model.KnowledgeBaseAliasCommand;
import com.esep.merchantmanagement.model.KnowledgeBaseMerchantCommand;
import com.esep.merchantmanagement.service.DefaultMerchantManagementService;
import com.esep.merchantmanagement.service.KnowledgeBaseMerchantService;
import com.esep.merchantresolver.model.MerchantReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** HTTP API административного управления базой знаний продавцов. */
@RestController
@RequestMapping("/api/knowledge-base")
@Tag(name = "Knowledge Base", description = "Управление продавцами и их алиасами")
public class KnowledgeBaseMerchantController {

    private final KnowledgeBaseMerchantService knowledgeBaseMerchantService;
    private final DefaultMerchantManagementService merchantManagementService;

    public KnowledgeBaseMerchantController(
            KnowledgeBaseMerchantService knowledgeBaseMerchantService,
            DefaultMerchantManagementService merchantManagementService
    ) {
        this.knowledgeBaseMerchantService = knowledgeBaseMerchantService;
        this.merchantManagementService = merchantManagementService;
    }

    @GetMapping("/merchants")
    @Operation(summary = "Получить продавцов базы знаний")
    public List<KnowledgeBaseMerchantResponse> findMerchants(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) MerchantType merchantType,
            @RequestParam(required = false) String categoryCode
    ) {
        return knowledgeBaseMerchantService.findAll(query, merchantType, categoryCode).stream()
                .map(KnowledgeBaseMerchantResponse::from).toList();
    }

    @GetMapping("/merchants/{id}")
    public KnowledgeBaseMerchantResponse findMerchant(@PathVariable String id) {
        return KnowledgeBaseMerchantResponse.from(knowledgeBaseMerchantService.findById(id));
    }

    @PostMapping("/merchants")
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeBaseMerchantResponse createMerchant(@Valid @RequestBody KnowledgeBaseMerchantRequest request) {
        MerchantReference reference = knowledgeBaseMerchantService.create(command(request));
        return KnowledgeBaseMerchantResponse.from(knowledgeBaseMerchantService.findById(reference.value()));
    }

    @PutMapping("/merchants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMerchant(@PathVariable String id, @Valid @RequestBody KnowledgeBaseMerchantRequest request) {
        knowledgeBaseMerchantService.update(id, command(request));
    }

    @DeleteMapping("/merchants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMerchant(@PathVariable String id) {
        knowledgeBaseMerchantService.delete(id);
    }

    @PostMapping("/merchants/{id}/aliases")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAlias(@PathVariable String id, @Valid @RequestBody KnowledgeBaseAliasRequest request) {
        merchantManagementService.match(request.aliasName(), new MerchantReference(id));
    }

    @PutMapping("/merchants/{id}/aliases/{aliasId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAlias(@PathVariable String id, @PathVariable Long aliasId, @Valid @RequestBody KnowledgeBaseAliasRequest request) {
        knowledgeBaseMerchantService.updateAlias(id, aliasId, new KnowledgeBaseAliasCommand(request.aliasName()));
    }

    @DeleteMapping("/merchants/{id}/aliases/{aliasId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlias(@PathVariable String id, @PathVariable Long aliasId) {
        knowledgeBaseMerchantService.deleteAlias(id, aliasId);
    }

    @GetMapping("/categories")
    public List<CategoryOptionResponse> findCategories() {
        return knowledgeBaseMerchantService.findCategories().stream().map(CategoryOptionResponse::from).toList();
    }

    private KnowledgeBaseMerchantCommand command(KnowledgeBaseMerchantRequest request) {
        return new KnowledgeBaseMerchantCommand(request.name(), request.merchantType(), request.categoryCode());
    }
}
