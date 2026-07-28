package com.esep.persistence.jpa.read.repository;

import com.esep.entity.Merchant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Read-репозиторий списка продавцов.
 */
public interface MerchantReadJpaRepository extends Repository<Merchant, Long> {

    @Query("""
            select merchant
            from Merchant merchant
            left join fetch merchant.category
            order by merchant.originalName asc
            """)
    List<Merchant> findAllForSelection();
}
