package org.mate.utils.input_generation;

import android.text.InputType;

import java.util.HashSet;
import java.util.Set;

// TODO: add documentation
public enum InputFieldType {

    TEXT_VARIATION_PERSON_NAME("^[a-zA-Z\\s]+", InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_CLASS_TEXT),
    TEXT_FLAG_MULTI_LINE("^[a-zA-Z\\s\n]+", InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT),
    TEXT_VARIATION_PASSWORD("^(.)+", InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT),
    TEXT_VARIATION_POSTAL_ADDRESS("^[a-zA-Z0-9-,\\s]+", InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_CLASS_TEXT),
    TEXT_VARIATION_EMAIL("[A-Z0-9._%+-]+@[A-Z0-9.-]+.[A-Z]{2,4}", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT),
    CLASS_PHONE("[0-9/]+", InputType.TYPE_CLASS_PHONE),
    NUMBER_VARIATION_PASSWORD("^[0-9]+", InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER),
    NUMBER_FLAG_SIGNED("[+|-]{1}[0-9]+\\.[0-9]+", InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER),
    CLASS_NUMBER("^[0-9]+", InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER),
    NUMBER_FLAG_DECIMAL("[0-9]+\\.[0-9]+", InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER),
    DATETIME_VARIATION_TIME("[0-9]{1,2}\\:[0-9]{2}", InputType.TYPE_DATETIME_VARIATION_TIME | InputType.TYPE_CLASS_DATETIME),
    DATETIME_VARIATION_DATE("[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{4}", InputType.TYPE_DATETIME_VARIATION_DATE | InputType.TYPE_CLASS_DATETIME),
    NOTHING;


    private int inputTypeNumber;
    private String regexInputField;

    InputFieldType(){}

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
            if(inputField!=NOTHING && staticString.matches(inputField.regexInputField)){
                fields.add(inputField);
            }
        }
        return fields;
    }
}
