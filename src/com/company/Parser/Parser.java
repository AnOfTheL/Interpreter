package com.company.Parser;

import com.company.LexicalAnalyser.Token;

import java.util.*;

public class Parser {
    private List<Token> tokens = new ArrayList<>();
    private List<String> varSet = new ArrayList<>();
    private List<String> set = new ArrayList<>();

    private Token next() {
        assert !tokens.isEmpty();
        return tokens.get(0);
    }

    private boolean next(String type) {
        assert !tokens.isEmpty();
        return tokens.get(0).getType().equals(type);
    }

    public String parse(List<Token> tokens) {
        this.tokens.addAll(tokens);
        lang();
        return "Syntax is OK";
    }

    private void lang() {
        while (!tokens.isEmpty()) { expr(); }
    }

    private void expr() {
        if (next("VAR")) {
            String value = next().getValue();
            tokens.remove(0);
            set_expr(value);
        } else if (next("INIT")) {
            tokens.remove(0);
            if (!set.contains(next().getValue())) {
                if (!varSet.contains(next().getValue()))
                    assign_expr();
                else
                    throw new IllegalArgumentException("Error. Variable '" +
                            next().getValue() + "' is already defined for a primitive type");
            } else
                throw new IllegalArgumentException("Error. Variable '" +
                        next().getValue() + "' is already defined for a set.");
        } else if (next("WHILE")) {
            tokens.remove(0);
            while_expr();
        } else
            throw new IllegalArgumentException("Error. Expected VAR, INIT or WHILE, but " +
                    next().getType() + " was found.");
    }

    private void set_expr(String value) {
        if (next("TYPE")) {
            tokens.remove(0);
            if (next("SET")) {
                if (!set.contains(value)) {
                    if (!varSet.contains(value)) {
                        set.add(value);
                        tokens.remove(0);
                        if (next("EOL")) {
                            tokens.remove(0);
                        } else
                            throw new IllegalArgumentException("Error. Expected EOL, but " +
                                    next().getType() + " was found.");
                    } else
                        throw new IllegalArgumentException("Error. Variable '" +
                                value + "' is already defined for a primitive type.");
                } else throw new IllegalArgumentException("Error. Variable '" +
                        value + "' is already defined for a set.");
            } else
                throw new IllegalArgumentException("Error. Expected SET, but " +
                        next().getType() + " was found.");

        } else if (set.contains(value) && (next("ADD") || next("REMOVE") || next("CONTAINS"))) {
            tokens.remove(0);
            value();
            if (next("EOL"))
                tokens.remove(0);
            else
                throw new IllegalArgumentException("Error. Expected EOL, but " +
                        next().getType() + " was found.");
        } else
            throw new IllegalArgumentException("Error. Set '" +
                    value + "' was not initialized.");
    }

    private void while_expr() {
        condition();
        body();
    }

    private void condition() {
        if (next("LPAR")) {
            tokens.remove(0);
            log_expr();
            if (next("RPAR")) {
                tokens.remove(0);
            } else
                throw new IllegalArgumentException("Error. Expected RPAR, but " +
                        next().getType() + " was found.");
        } else
            throw new IllegalArgumentException("Error. Expected LPAR, but " +
                    next().getType() + " was found.");
    }

    private void log_expr() {
        value();
        if (next("LOGOP")) {
            tokens.remove(0);
            value();
        } else
            throw new IllegalArgumentException("Error. Expected LOGOP, but " +
                    next().getType() + " was found.");
    }

    private void body() {
        if (next("LBRACE")) {
            tokens.remove(0);
            body_expr();
            if (next("RBRACE")) {
                tokens.remove(0);
            } else
                throw new IllegalArgumentException("Error. Expected RBRACE, but " +
                        next().getType() + " was found.");
        } else
            throw new IllegalArgumentException("Error. Expected LBRACE, but " +
                    next().getType() + " was found.");
    }

    private void body_expr() {
        while (!next("RBRACE")) {
            if (!set.contains(next().getValue())) {
                if (varSet.contains(next().getValue()))
                    assign_expr();
                else
                    throw new IllegalArgumentException("Error. Variable '" +
                            next().getValue() + "' was not initialized.");
            } else
                throw new IllegalArgumentException("Error. Variable '" +
                        next().getValue() + "' is defined for a set.");
            if (next("WHILE")) {
                tokens.remove(0);
                while_expr();
            }
        }
    }

    private void assign_expr() {
        if (next("VAR")) {
            varSet.add(next().getValue());
            tokens.remove(0);
            if (next("ASSIGN")) {
                tokens.remove(0);
                value_expr();
            } else
                throw new IllegalArgumentException("Error. Expected ASSIGN, but " +
                        next().getType() + " was found.");
        } else
            throw new IllegalArgumentException("Error. Expected VAR, but " +
                    next().getType() + " was found.");
    }

    private void value_expr() {
        value();
        while (!next("EOL")) {
            if (next("ADDSUB") || next("MULDIV")) {
                tokens.remove(0);
                value();
            } else
                throw new IllegalArgumentException("Error. Expected ADDSUB or MULDIV, but " +
                        next().getType() + " was found.");
        }
        tokens.remove(0);
    }

    private void value() {
        if (next("VAR") || next("NUM"))
            tokens.remove(0);
        else
            throw new IllegalArgumentException("Error. Expected VAR or NUM, but " +
                    next().getType() + " was found.");
    }
}
