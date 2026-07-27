package com.esep.persistence.jpa.adapter;

import com.esep.entity.Category;
import com.esep.persistence.interfaces.CategoryCatalog;
import com.esep.persistence.jpa.repository.CategoryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA-адаптер прикладного каталога категорий.
 */
@Repository
public class CategoryCatalogJpaAdapter implements CategoryCatalog {

    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryCatalogJpaAdapter(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public Optional<String> findNameByCode(String categoryCode) {
        return categoryJpaRepository.findByCode(categoryCode)
                .map(Category::getName);
    }
}
