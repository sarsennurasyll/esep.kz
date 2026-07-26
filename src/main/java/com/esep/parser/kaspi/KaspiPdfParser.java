package com.esep.parser.kaspi;

import com.esep.entity.BankType;
import com.esep.parser.interfaces.StatementParser;
import com.esep.parser.model.FileType;
import com.esep.parser.model.ParserContext;
import org.springframework.stereotype.Component;

/**
 * Заготовка парсера PDF-выписок Kaspi Bank.
 */
@Component
public class KaspiPdfParser implements StatementParser {

    @Override
    public boolean supports(BankType bankType, FileType fileType) {
        // TODO: Реализовать проверку поддерживаемого банка и формата.
        return false;
    }

    @Override
    public void parse(ParserContext context) {
        // TODO: Реализовать импорт PDF-выписки Kaspi Bank.
    }
}
