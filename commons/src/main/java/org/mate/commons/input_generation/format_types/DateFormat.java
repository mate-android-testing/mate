package org.mate.commons.input_generation.format_types;

/**
 * The supported date formats.
 */
public enum DateFormat {

    VARIANT1("MM-dd-yyyy"),
    VARIANT2("MM/dd/yyyy"),
    VARIANT3("dd-MM-yyyy"),
    VARIANT4("dd/MM/yyyy"),
    VARIANT5("dd.MM.yyyy"),
    VARIANT6("yyyy-MM-dd");

    /**
     * The date format pattern, e.g. 'yyyy-MM-dd'.
     */
    private final String pattern;

    /**
     * Constructs a new date format described by the given pattern.
     *
     * @param pattern The given date format pattern.
     */
    DateFormat(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns the date format pattern.
     *
     * @return Returns the date format pattern.
     */
    public String getPattern() {
        return pattern;
    }
}