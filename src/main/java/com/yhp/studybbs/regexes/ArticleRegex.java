package com.yhp.studybbs.regexes;

public enum ArticleRegex implements Regex {
    TITLE("^(?=.{3,100}$)(\\S)(.*)(\\S)$");

    public final String expression;

    ArticleRegex(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean matches(String input) {
        return input != null && input.matches(this.expression);
    }
}
