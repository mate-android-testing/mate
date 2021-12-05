package org.mate.utils.input_generation;


import android.text.InputType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Enum for the different input field types. Clearly, not all can be covered here, but here are the
 * most important ones. It can be extended at any time.
 */
public enum InputFieldType {
    /**
     * Regex rules:
     * <ul>
     *  <li>no tabs, line breaks</li>
     *  <li>no numbers </li>
     *  <li>at least 3 characters</li>
     * </ul>
     */
    TEXT_VARIATION_PERSON_NAME("[a-zA-Zß\\p{javaSpaceChar}\\.\\-äöüÄÖÜ]{3,}", InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_CLASS_TEXT),

    /**
     * Regex rules:
     * <ul>
     *  <li>at least 3 characters</li>
     * </ul>
     */
    TEXT_FLAG_MULTI_LINE("[\\w\\s!\"ß§$%&/()=?\\`´+*#-\\.\\,\\;\\:]{3,}", InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT),

    /**
     * Regex rules:
     * <ul>
     *  <li>any character, no spaces, line breaks, ...</li>
     *  <li>at least 3 characters</li>
     * </ul>
     */
    TEXT_VARIATION_PASSWORD("^[\\w\\.\\-+*#-\\.\\,\\;\\:]{3,}", InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT),

    /**
     * Regex rules:
     * <ul>
     *  <li>Line breaks, numbers are allowed</li>
     *  <li>Special characters forbidden.</li>
     *  <li>at least 7 characters</li>
     * </ul>
     */
    TEXT_VARIATION_POSTAL_ADDRESS("^[\\w-,ß\\s\\.,]{7,}", InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_CLASS_TEXT),
    /**
     * Regex rules:
     * <ul>
     *  <li>Only numbers and spaces are allowed.</li>
     *  <li>Also special characters for separating addressing groups.</li>
     *  <li>Prefixes such as (+49) are also permitted for the country-specific area code.</li>
     * </ul>
     */
    TEXT_VARIATION_EMAIL("[\\w\\._%+-]+\\@[\\w.-]+\\.[A-Za-z]{2,4}", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT),
    /**
     * Regex rules:
     * <ul>
     *  <li>Line breaks, numbers are allowed</li>
     *  <li>Special characters forbidden.</li>
     *  <li>at least 7 characters</li>
     * </ul>
     */
    CLASS_PHONE("(\\(\\+[0-9]{2,3}\\)){0,1}\\p{javaSpaceChar}{0,1}[0-9]{3,}[/\\p{javaSpaceChar}-]{0,1}[0-9]{2,}", InputType.TYPE_CLASS_PHONE),

    /**
     * Regex rules:
     * <ul>
     *  <li>Only numbers are allowed.</li>
     *  <li>at least 4 characters</li>
     * </ul>
     */
    NUMBER_VARIATION_PASSWORD("^[0-9]{4,}", InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER),
    /**
     * Regex rules:
     * <ul>
     *  <li>Signs (+-) are allowed.</li>
     *  <li>Separators can be represented by full stops and commas.</li>
     *  <li>Otherwise only numbers.</li>
     * </ul>
     */
    NUMBER_FLAG_SIGNED("[+|-]{0,1}[0-9]+((\\.|,)[0-9]+){0,1}", InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER),
    /**
     * Regex rules:
     * <ul>
     *  <li>Only numbers are allowed.</li>
     *  <li>at least 1 character</li>
     * </ul>
     */
    CLASS_NUMBER("^[0-9]+", InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER),

    /**
     * Regex rules:
     * <ul>
     *  <li>Signs (+-) aren't allowed.</li>
     *  <li>Separators can be represented by full stops and commas.</li>
     *  <li>Otherwise only numbers.</li>
     * </ul>
     */
    NUMBER_FLAG_DECIMAL("[0-9]+([\\.,][0-9]+){0,1}", InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER),
    /**
     * A separate method was written for checking a valid date format.
     */
    DATETIME_VARIATION_TIME(InputType.TYPE_DATETIME_VARIATION_TIME | InputType.TYPE_CLASS_DATETIME),
    /**
     * A separate method was written for checking a valid time format.
     */
    DATETIME_VARIATION_DATE(InputType.TYPE_DATETIME_VARIATION_DATE | InputType.TYPE_CLASS_DATETIME),
    /**
     * If an input field type cannot be defined.
     */
    NOTHING;


    private int inputTypeNumber;

    private String regex;

    /**
     * Default constructor for {@link InputFieldType#NOTHING}.
     */
    InputFieldType() {
    }

    /**
     * Constructor for {@link InputFieldType#DATETIME_VARIATION_DATE} and {@link InputFieldType#DATETIME_VARIATION_TIME}.
     *
     * @param inputTypeNumber The number given by the rules of {@link android.view.inputmethod.EditorInfo}.
     */
    InputFieldType(int inputTypeNumber) {
        this.inputTypeNumber = inputTypeNumber;
    }

    /**
     * Constructor for all enum types with regex.
     *
     * @param regexInputField The given regex. Rules are defined above.
     * @param inputTypeNumber The number given by the rules of {@link android.view.inputmethod.EditorInfo}.
     */
    InputFieldType(String regexInputField, int inputTypeNumber) {
        this.regex = regexInputField;
        this.inputTypeNumber = inputTypeNumber;
    }

    /**
     * Gets a input field type by the number calculated by the rules of {@link android.view.inputmethod.EditorInfo}.
     *
     * @param inputTypeNumber The given input type nuber.
     * @return The corresponding input field type. If it does not exist, {@link InputFieldType#NOTHING} is returned.
     */
    public static InputFieldType getFieldTypeByNumber(int inputTypeNumber) {
        for (InputFieldType inputField : values()) {
            if (inputField.inputTypeNumber == inputTypeNumber) {
                return inputField;
            }
        }
        return NOTHING;
    }

    /**
     * Determines for the existing input field type whether the entered string is valid.
     *
     * @param exampleString The string to be tested.
     * @return {@code true} if valid, {@code false} if not.
     */
    public boolean isValid(final String exampleString) {
        if (regex != null) {
            return this != NOTHING && exampleString.matches(regex);
        } else if (this == DATETIME_VARIATION_DATE) {
            return isDate(exampleString);
        } else if (this == DATETIME_VARIATION_TIME) {
            return isTime(exampleString);
        }
        return false;
    }

    /**
     * Returns a set of InputFieldTypes that would match the given string.
     *
     * @param staticString The given string.
     * @return The InputFieldTypes matching the given string.
     */
    public static Set<InputFieldType> getInputFieldsMatchingRegex(final String staticString) {
        Set<InputFieldType> fields = new HashSet<>();
        for (InputFieldType inputField : values()) {
            if (inputField.isValid(staticString)) {
                fields.add(inputField);
            }
        }
        return fields;
    }

    /**
     * Getter for the regex.
     * @return The regex.
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Checks whether a string has a valid date format. Different formats of several countries are considered.
     * @param dateStr The input date string.
     * @return {@code true} if valid, {@code false} if not.
     */
    public static boolean isDate(String dateStr) {
        int counter = 0;
        for (DateFormat d : DateFormat.values()) {
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(d.getPattern());
                LocalDate.parse(dateStr, dateFormatter);
            } catch (DateTimeParseException e) {
                counter++;
            }
        }
        return counter != DateFormat.values().length;
    }

    /**
     * Checks whether a string has a valid time format. Different formats of several countries are considered.
     * @param timeStr The input time string.
     * @return {@code true} if valid, {@code false} if not.
     */
    public static boolean isTime(String timeStr) {
        int counter = 0;
        for (TimeFormat d : TimeFormat.values()) {
            try {
                DateTimeFormatter dateFormatter;
                if (d.getLocale() != null) {
                    dateFormatter = DateTimeFormatter.ofPattern(d.getPattern(), d.getLocale());
                } else {
                    dateFormatter = DateTimeFormatter.ofPattern(d.getPattern());
                }
                LocalTime.parse(timeStr, dateFormatter);
            } catch (DateTimeParseException e) {
                counter++;
            }
        }
        return counter != TimeFormat.values().length;
    }

    /**
     * Listing of different date formats.
     */
    private enum DateFormat {
        VARIANT1("MM-dd-yyyy"),
        VARIANT2("MM/dd/yyyy"),
        VARIANT3("dd-MM-yyyy"),
        VARIANT4("dd/MM/yyyy"),
        VARIANT5("dd.MM.yyyy"),
        VARIANT6("yyyy-MM-dd");

        private final String pattern;

        /**
         * Constructor for each variant.
         * @param pattern The pattern.
         */
        DateFormat(String pattern) {
            this.pattern = pattern;
        }

        private String getPattern() {
            return pattern;
        }
    }

    /**
     * Listing of different time formats.
     */
    private enum TimeFormat {
        VARIANT1("H:mm:ss", null),
        VARIANT2("H:mm", null),
        VARIANT3("h:mm a", Locale.ENGLISH);

        private final String pattern;
        private final Locale locale;

        /**
         * Constructor for each variant.
         * @param pattern The pattern of the variant.
         * @param locale Sometime the {@link Locale} is needed.
         */
        TimeFormat(String pattern, Locale locale) {
            this.pattern = pattern;
            this.locale = locale;
        }

        private String getPattern() {
            return pattern;
        }

        private Locale getLocale() {
            return locale;
        }
    }
}
