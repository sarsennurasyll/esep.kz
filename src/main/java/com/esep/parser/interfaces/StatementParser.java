package com.esep.parser.interfaces;

import com.esep.entity.BankType;
import com.esep.parser.model.FileType;
import com.esep.parser.model.ParserContext;

/**
 * Контракт для импорта банковских выписок определённого банка и формата.
 */
public interface StatementParser {

    boolean supports(BankType bankType, FileType fileType);

    void parse(ParserContext context);
}
