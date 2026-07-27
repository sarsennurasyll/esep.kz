package com.esep.persistence.jpa.repository;

import com.esep.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantJpaRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByNormalizedName(String normalizedName);
}
