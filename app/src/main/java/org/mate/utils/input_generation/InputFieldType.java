package org.mate.utils.input_generation;


import android.text.InputType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: add documentation
public enum InputFieldType {

    TEXT_VARIATION_PERSON_NAME("[a-zA-Zß\\p{javaSpaceChar}\\.\\-äöüÄÖÜ]{3,}", InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_CLASS_TEXT),
    TEXT_FLAG_MULTI_LINE("[\\w\\s!\"ß§$%&/()=?\\`´+*#-\\.\\,\\;\\:]{3,}", InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT),
    TEXT_VARIATION_PASSWORD("^[\\w\\.\\-+*#-\\.\\,\\;\\:]{3,}", InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT),
    TEXT_VARIATION_POSTAL_ADDRESS("^[\\w-,ß\\s\\.,]{7,}", InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_CLASS_TEXT),
    TEXT_VARIATION_EMAIL("[\\w\\._%+-]+\\@[\\w.-]+\\.[A-Za-z]{2,4}", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT),
    CLASS_PHONE("(\\(\\+[0-9]{2,3}\\)){0,1}\\p{javaSpaceChar}{0,1}[0-9]{3,}[/\\p{javaSpaceChar}-]{0,1}[0-9]{2,}", InputType.TYPE_CLASS_PHONE),
    NUMBER_VARIATION_PASSWORD("^[0-9]{4,}", InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER),
    NUMBER_FLAG_SIGNED("[+|-]{0,1}[0-9]+((\\.|,)[0-9]+){0,1}", InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER),
    CLASS_NUMBER("^[0-9]+", InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER),
    NUMBER_FLAG_DECIMAL("[0-9]+([\\.,][0-9]+){0,1}", InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER),
    DATETIME_VARIATION_TIME(InputType.TYPE_DATETIME_VARIATION_TIME | InputType.TYPE_CLASS_DATETIME),
    DATETIME_VARIATION_DATE(InputType.TYPE_DATETIME_VARIATION_DATE | InputType.TYPE_CLASS_DATETIME),
    NOTHING;


    private int inputTypeNumber;

    private String regexInputField;

    InputFieldType() {
    }

    InputFieldType(int inputTypeNumber) {
        this.inputTypeNumber = inputTypeNumber;
    }

    InputFieldType(String regexInputField, int inputTypeNumber) {
        this.regexInputField = regexInputField;
        this.inputTypeNumber = inputTypeNumber;
    }

    public static InputFieldType getFieldTypeByNumber(int inputTypeNumber) {
        for (InputFieldType inputField : values()) {
            if (inputField.inputTypeNumber == inputTypeNumber) {
                return inputField;
            }
        }
        return NOTHING;
    }

    public static Set<InputFieldType> getInputFieldsMatchingRegex(final String staticString) {
        Set<InputFieldType> fields = new HashSet<>();
        for (InputFieldType inputField : values()) {
            if (inputField.regexInputField != null) {
                if (inputField != NOTHING && staticString.matches(inputField.regexInputField)) {
                    fields.add(inputField);
                }
            } else if (inputField == DATETIME_VARIATION_DATE) {
                if (isDate(staticString)) {
                    fields.add(inputField);
                }
            } else if (inputField == DATETIME_VARIATION_TIME) {
                if (isTime(staticString)) {
                    fields.add(inputField);
                }
            }
        }
        return fields;
    }

    public String getRegex() {
        return regexInputField;
    }

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

    private enum DateFormat {
        VARIANT1("MM-dd-yyyy"),
        VARIANT2("MM/dd/yyyy"),
        VARIANT3("dd-MM-yyyy"),
        VARIANT4("dd/MM/yyyy"),
        VARIANT5("dd.MM.yyyy"),
        VARIANT6("yyyy-MM-dd");

        private final String pattern;

        DateFormat(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }

    private enum TimeFormat {
        VARIANT1("H:mm:ss", null),
        VARIANT2("H:mm", null),
        VARIANT3("h:mm a", Locale.ENGLISH);

        private final String pattern;
        private final Locale locale;

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
}
