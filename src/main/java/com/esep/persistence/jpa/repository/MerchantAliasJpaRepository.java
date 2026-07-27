package com.esep.persistence.jpa.repository;

import com.esep.entity.MerchantAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantAliasJpaRepository extends JpaRepository<MerchantAlias, Long> {

    Optional<MerchantAlias> findByNormalizedAlias(String normalizedAlias);
}
