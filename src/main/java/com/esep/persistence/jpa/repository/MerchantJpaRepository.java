package com.esep.persistence.jpa.repository;

import com.esep.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface MerchantJpaRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByNormalizedName(String normalizedName);

    @Query("select distinct merchant from Merchant merchant left join fetch merchant.category left join fetch merchant.aliases")
    List<Merchant> findAllForKnowledgeBase();
}
