package org.mate.utils.input_generation;

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

    public String getPattern() {
        return pattern;
    }

    public Locale getLocale() {
        return locale;
    }
}