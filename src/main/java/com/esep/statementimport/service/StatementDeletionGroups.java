package com.esep.statementimport.service;

import com.esep.persistence.model.StoredStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Выписки, которые нельзя удалить по отдельности.
 *
 * <p>При импорте операции общего периода сохраняются только в выписке, импортированной раньше (ADR-0004).
 * Поэтому вместе с выпиской удаляются все выписки того же счёта, импортированные после неё и пересекающиеся
 * с ней по периоду, — и так далее по цепочке. Иначе в их периоде остались бы пропуски, которых не видно
 * ни в отчёте, ни в отметке полноты месяца.
 */
final class StatementDeletionGroups {

    private StatementDeletionGroups() {
    }

    /** @param statements все выписки в порядке импорта; должна содержать {@code target} */
    static List<Long> of(StoredStatement target, List<StoredStatement> statements) {
        List<StoredStatement> group = new ArrayList<>(List.of(target));
        for (StoredStatement candidate : statements) {
            if (group.contains(candidate)) {
                continue;
            }
            if (group.stream().anyMatch(member -> dependsOn(candidate, member))) {
                group.add(candidate);
            }
        }
        return group.stream().map(StoredStatement::id).sorted().toList();
    }

    // Порядок импорта совпадает с порядком id, поэтому проход по списку один раз находит всю цепочку.
    private static boolean dependsOn(StoredStatement later, StoredStatement earlier) {
        return later.id() > earlier.id()
                && earlier.accountNumber() != null
                && later.bank() == earlier.bank()
                && Objects.equals(later.accountNumber(), earlier.accountNumber())
                && !later.periodFrom().isAfter(earlier.periodTo())
                && !earlier.periodFrom().isAfter(later.periodTo());
    }
}
