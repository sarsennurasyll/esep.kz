package com.esep.persistence.interfaces;

import java.util.Optional;

/**
 * Прикладной контракт поиска категории по стабильному коду.
 */
public interface CategoryCatalog {

    Optional<String> findNameByCode(String categoryCode);
}
