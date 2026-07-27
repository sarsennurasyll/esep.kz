package com.esep.persistence.jpa.repository;

import com.esep.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatementJpaRepository extends JpaRepository<Statement, Long> {

    boolean existsBySourceFileHash(String sourceFileHash);

    Optional<Statement> findBySourceFileHash(String sourceFileHash);
}
