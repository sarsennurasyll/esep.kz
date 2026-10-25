package com.esep.moneyflow.api.dto;

import com.esep.moneyflow.model.LabelingProgress;

import java.math.BigDecimal;

/** HTTP-представление прогресса разметки назначения. */
public record LabelingProgressResponse(BigDecimal totalAmount, BigDecimal labeledAmount, BigDecimal labeledShare, int counterparties, int rules) {

    public static LabelingProgressResponse from(LabelingProgress progress) {
        return new LabelingProgressResponse(progress.totalAmount(), progress.labeledAmount(), progress.labeledShare(),
                progress.counterparties(), progress.rules());
    }
}
