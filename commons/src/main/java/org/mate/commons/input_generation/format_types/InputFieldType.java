package org.mate.commons.input_generation.format_types;


import android.text.InputType;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Enum for the different input field types.
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
    TEXT_VARIATION_PERSON_NAME("[a-zA-Zß\\p{javaSpaceChar}\\.\\-äöüÄÖÜ]{3,}",
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_CLASS_TEXT),

    /**
     * Regex rules:
     * <ul>
     *  <li>at least 3 characters</li>
     * </ul>
     */
    TEXT_FLAG_MULTI_LINE("[\\w\\s!\"ß§$%&/()=?\\`´+*#-\\.\\,\\;\\:]{3,}",
            InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT),

    /**
     * Regex rules:
     * <ul>
     *  <li>any character, no spaces, line breaks, ...</li>
     *  <li>at least 3 characters</li>
     * </ul>
     */
    TEXT_VARIATION_PASSWORD("^[\\w\\.\\-+*#-\\.\\,\\;\\:]{3,}",
            InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT),

    /**
     * Regex rules:
     * <ul>
     *  <li>Line breaks, numbers are allowed</li>
     *  <li>Special characters forbidden.</li>
     *  <li>at least 7 characters</li>
     * </ul>
     */
    TEXT_VARIATION_POSTAL_ADDRESS("^[\\w-,ß\\s\\.,]{7,}",
            InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_CLASS_TEXT),
    /**
     * Regex rules:
     * <ul>
     *  <li>Only numbers and spaces are allowed.</li>
     *  <li>Also special characters for separating addressing groups.</li>
     *  <li>Prefixes such as (+49) are also permitted for the country-specific area code.</li>
     * </ul>
     */
    TEXT_VARIATION_EMAIL("[\\w\\._%+-]+\\@[\\w.-]+\\.[A-Za-z]{2,4}",
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT),
    /**
     * Regex rules:
     * <ul>
     *  <li>Line breaks, numbers are allowed</li>
     *  <li>Special characters forbidden.</li>
     *  <li>at least 7 characters</li>
     * </ul>
     */
    CLASS_PHONE("(\\(\\+[0-9]{2,3}\\)){0,1}\\p{javaSpaceChar}{0,1}[0-9]{3,}" +
            "[/\\p{javaSpaceChar}-]{0,1}[0-9]{2,}", InputType.TYPE_CLASS_PHONE),

    /**
     * Regex rules:
     * <ul>
     *  <li>Only numbers are allowed.</li>
     *  <li>at least 4 characters</li>
     * </ul>
     */
    NUMBER_VARIATION_PASSWORD("^[0-9]{4,}",
            InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER),
    /**
     * Regex rules:
     * <ul>
     *  <li>Signs (+-) are allowed.</li>
     *  <li>Separators can be represented by full stops and commas.</li>
     *  <li>Otherwise only numbers.</li>
     * </ul>
     */
    NUMBER_FLAG_SIGNED("[+|-]{0,1}[0-9]+((\\.|,)[0-9]+){0,1}",
            InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER),
    /**
     * Regex rules:
     * <ul>
     *  <li>Only numbers are allowed.</li>
     *  <li>at least 1 character</li>
     * </ul>
     */
    CLASS_NUMBER("^[0-9]+", InputType.TYPE_NUMBER_FLAG_DECIMAL
            | InputType.TYPE_CLASS_NUMBER),

    /**
     * Regex rules:
     * <ul>
     *  <li>Signs (+-) aren't allowed.</li>
     *  <li>Separators can be represented by full stops and commas.</li>
     *  <li>Otherwise only numbers.</li>
     * </ul>
     */
    NUMBER_FLAG_DECIMAL("[0-9]+([\\.,][0-9]+){0,1}",
            InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER),

    /**
     * The input field type to represent a time.
     */
    DATETIME_VARIATION_TIME(InputType.TYPE_DATETIME_VARIATION_TIME
            | InputType.TYPE_CLASS_DATETIME),

    /**
     * The input field type to represent a date.
     */
    DATETIME_VARIATION_DATE(InputType.TYPE_DATETIME_VARIATION_DATE
            | InputType.TYPE_CLASS_DATETIME),
    /**
     * If an input field type cannot be derived.
     */
    NOTHING;

    /**
     * The input field type number corresponding to a given input field.
     */
    private int inputTypeNumber;

    /**
     * The regex describing the input field.
     */
    private String regex;

    /**
     * Default constructor used for {@link InputFieldType#NOTHING}.
     */
    InputFieldType() {
    }

    /**
     * Constructor used for {@link InputFieldType#DATETIME_VARIATION_DATE} and
     * {@link InputFieldType#DATETIME_VARIATION_TIME}.
     *
     * @param inputTypeNumber The number given by the rules of {@link android.view.inputmethod.EditorInfo}.
     */
    InputFieldType(int inputTypeNumber) {
        this.inputTypeNumber = inputTypeNumber;
    }

    /**
     * Constructs an {@link InputFieldType} that is described by the given regex and the input number.
     *
     * @param regex The given regex.
     * @param inputTypeNumber The number given by the rules of {@link android.view.inputmethod.EditorInfo}.
     */
    InputFieldType(String regex, int inputTypeNumber) {
        this.regex = regex;
        this.inputTypeNumber = inputTypeNumber;
    }

    /**
     * Gets the {@link InputFieldType} that matches the given number calculated by the rules of
     * {@link android.view.inputmethod.EditorInfo}.
     *
     * @param inputTypeNumber The given number referring to an {@link InputFieldType}.
     * @return Returns the corresponding input field type. If such a type does not exist,
     *         {@link InputFieldType#NOTHING} is returned.
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
     * Determines whether the given string represents a valid {@link InputFieldType}.
     *
     * @param string The string to be checked.
     * @return Returns {@code true} if the given string is a valid {@link InputFieldType},
     *         otherwise {@code false} is returned.
     */
    public boolean isValid(final String string) {
        if (regex != null) {
            return this != NOTHING && string.matches(regex);
        } else if (this == DATETIME_VARIATION_DATE) {
            return isDate(string);
        } else if (this == DATETIME_VARIATION_TIME) {
            return isTime(string);
        }
        return false;
    }

    /**
     * Returns a set of {@link InputFieldType}s that match the given string.
     *
     * @param string The given string.
     * @return Returns the {@link InputFieldType}s that match the given string.
     */
    public static Set<InputFieldType> getInputFieldsMatchingRegex(final String string) {
        Set<InputFieldType> fields = new HashSet<>();
        for (InputFieldType inputField : values()) {
            if (inputField.isValid(string)) {
                fields.add(inputField);
            }
        }
        return fields;
    }

    /**
     * Returns the regex.
     *
     * @return Returns the regex.
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Checks whether a string has a valid date format. Different formats of several countries
     * are considered.
     *
     * @param dateStr The given date string.
     * @return Returns {@code true} if the date string is valid, otherwise {@code false}.
     */
    public static boolean isDate(final String dateStr) {
        for (DateFormat d : DateFormat.values()) {
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(d.getPattern(), Locale.US);
                LocalDate date = LocalDate.parse(dateStr, dateFormatter);

                if (date != null) {
                    return true;
                }
            } catch (DateTimeParseException e) {
                // not a date
            }
        }

        return false;
    }

    /**
     * Checks whether a string has a valid time format. Different formats of several countries
     * are considered.
     *
     * @param timeStr The given time string.
     * @return Returns {@code true} if the time string is valid, otherwise {@code false}.
     */
    public static boolean isTime(final String timeStr) {
        for (TimeFormat d : TimeFormat.values()) {
            try {
                DateTimeFormatter dateFormatter;

                if (d.getLocale() != null) {
                    dateFormatter = DateTimeFormatter.ofPattern(d.getPattern(), d.getLocale());
                } else {
                    dateFormatter = DateTimeFormatter.ofPattern(d.getPattern(), Locale.US);
                }

                LocalTime date = LocalTime.parse(timeStr, dateFormatter);

                if (date != null) {
                    return true;
                }
            } catch (DateTimeParseException e) {
                // not a time
            }
        }

        return false;
    }
}
