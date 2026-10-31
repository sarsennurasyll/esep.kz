package com.esep.persistence.jpa.repository;

import com.esep.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserConsentJpaRepository extends JpaRepository<UserConsent, Long> {

    List<UserConsent> findByAccountIdOrderByIdDesc(Long accountId);
}
