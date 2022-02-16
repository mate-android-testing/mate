package org.mate.commons.input_generation.format_types;

import java.util.Locale;

/**
 * The supported time formats.
 */
public enum TimeFormat {

    VARIANT1("H:mm:ss", null),
    VARIANT2("H:mm", null),
    VARIANT3("h:mm a", Locale.ENGLISH);

    /**
     * The time format pattern, e.g. 'hh:mm:ss'.
     */
    private final String pattern;

    /**
     * The used locale, e.g. {@link Locale#ENGLISH}.
     */
    private final Locale locale;

    /**
     * Constructs a new time format described by the given pattern and locale.
     *
     * @param pattern The given time format pattern.
     * @param locale The given locale or {@code null} otherwise.
     */
    TimeFormat(String pattern, Locale locale) {
        this.pattern = pattern;
        this.locale = locale;
    }

    /**
     * Getter for the pattern.
     *
     * @return The pattern for a {@link TimeFormat} is returned.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Getter for the locale of a time format.
     *
     * @return The locale for a {@link TimeFormat} is returned.
     */
    public Locale getLocale() {
        return locale;
    }
}