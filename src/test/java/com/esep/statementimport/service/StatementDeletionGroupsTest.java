package com.esep.statementimport.service;

import com.esep.entity.BankType;
import com.esep.persistence.model.StoredStatement;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatementDeletionGroupsTest {

    private static final String CARD = "****0000";

    @Test
    void shouldDeleteLaterStatementAlone() {
        StoredStatement first = statement(1, CARD, 4, 10);
        StoredStatement second = statement(2, CARD, 9, 12);

        assertThat(StatementDeletionGroups.of(second, List.of(first, second))).containsExactly(2L);
    }

    @Test
    void shouldIncludeLaterOverlappingStatementsTransitively() {
        StoredStatement first = statement(1, CARD, 1, 3);
        StoredStatement second = statement(2, CARD, 3, 6);
        StoredStatement third = statement(3, CARD, 6, 9);
        StoredStatement separate = statement(4, CARD, 11, 12);

        assertThat(StatementDeletionGroups.of(first, List.of(first, second, third, separate))).containsExactly(1L, 2L, 3L);
    }

    @Test
    void shouldIgnoreOtherCardsAndStatementsWithoutCard() {
        StoredStatement first = statement(1, CARD, 1, 6);
        StoredStatement otherCard = statement(2, "****1111", 1, 6);
        StoredStatement unknownCard = statement(3, null, 1, 6);

        assertThat(StatementDeletionGroups.of(first, List.of(first, otherCard, unknownCard))).containsExactly(1L);
        assertThat(StatementDeletionGroups.of(unknownCard, List.of(first, otherCard, unknownCard))).containsExactly(3L);
    }

    private static StoredStatement statement(long id, String card, int fromMonth, int toMonth) {
        LocalDate to = LocalDate.of(2026, toMonth, 1);
        return new StoredStatement(id, BankType.KASPI, card, LocalDate.of(2026, fromMonth, 1), to.withDayOfMonth(to.lengthOfMonth()));
    }
}
