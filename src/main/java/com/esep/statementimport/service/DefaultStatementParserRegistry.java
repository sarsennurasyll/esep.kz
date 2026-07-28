package com.esep.statementimport.service;

import com.esep.entity.BankType;
import com.esep.statementimport.exception.UnsupportedBankTypeException;
import com.esep.statementimport.interfaces.StatementParser;
import com.esep.statementimport.interfaces.StatementParserRegistry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Реестр parser-ов, доступных для импорта банковских выписок.
 */
public class DefaultStatementParserRegistry implements StatementParserRegistry {

    private final Map<BankType, StatementParser> parsers;

    public DefaultStatementParserRegistry(List<StatementParser> parsers) {
        this.parsers = new EnumMap<>(BankType.class);
        for (StatementParser parser : parsers) {
            StatementParser existing = this.parsers.putIfAbsent(parser.supportedBank(), parser);
            if (existing != null) {
                throw new IllegalArgumentException("More than one parser is registered for bank: " + parser.supportedBank());
            }
        }
    }

    @Override
    public StatementParser getParser(BankType bankType) {
        StatementParser parser = parsers.get(bankType);
        if (parser == null) {
            throw new UnsupportedBankTypeException(bankType);
        }
        return parser;
    }
}
