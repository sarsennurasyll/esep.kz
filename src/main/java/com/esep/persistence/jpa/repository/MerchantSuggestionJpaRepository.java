package com.esep.persistence.jpa.repository;

import com.esep.entity.MerchantSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MerchantSuggestionJpaRepository extends JpaRepository<MerchantSuggestion, String> {

    // Уже отклонённое название остаётся отклонённым; одновременная вставка не ломает импорт.
    @Modifying
    @Query(value = """
            insert into merchant_suggestions (normalized_name, status) values (:name, 'NEW')
            on conflict (normalized_name) do nothing""", nativeQuery = true)
    void addIfAbsent(@Param("name") String normalizedName);

    @Modifying
    @Query(value = """
            insert into merchant_suggestions (normalized_name, status) values (:name, 'REJECTED')
            on conflict (normalized_name) do update set status = 'REJECTED'""", nativeQuery = true)
    void reject(@Param("name") String normalizedName);

    List<MerchantSuggestion> findByStatusOrderByNormalizedName(String status);
}
