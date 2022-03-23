package org.mate.utils.input_generation;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mate.utils.input_generation.format_types.InputFieldType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputFieldTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"Max Mustermann", "G. Milner", "Hans-Xaver", "max", "Stefan",
            "v. Goethe", "Müller"})
    void testRegexVariationPersonNameSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_PERSON_NAME;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"new\nLine", "new\ttab", "Hello!", "\r", "", "+", "-", "$", "462", ":",
            "[b-z]*", "of"})
    void testRegexVariationPersonNameFail(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_PERSON_NAME;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello it's \n a multi\nline\rtest!", ".;:-+*", "&h10)2"})
    void testRegexVariationMultiLineSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_FLAG_MULTI_LINE;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"of", "\n", "\n\n"})
    void testRegexVariationMultiLineFail(String text) {
        InputFieldType type = InputFieldType.TEXT_FLAG_MULTI_LINE;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"password", "password123", "pw#10ef", "goodJob1234;"})
    void testRegexVariationPWSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_PASSWORD;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test a", "ab", "hello\nworld"})
    void testRegexVariationPWFail(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_PASSWORD;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"max.muster@gmail.com", "hello@test.de", "fsinfo@fim.uni-passau.de",
            "hel_lo123@hello.com"})
    void testRegexVariationEmailSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_EMAIL;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"max.muster@", "@yahoo.de", "xyz@gmail", "abc@@bce.de",
            "hello!@test.de", "h@h@world.com", "no_Valid"})
    void testRegexVariationEmailFail(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_EMAIL;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Innstraße 41, 94032 Passau", "Innstr. 41 \n 94032 Passau",
            "Innstr-weg 41", "Innstra", "Innstr. 41a \n 94032 Passau \n GERMANY"})
    void testRegexVariationPostalAddressSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_POSTAL_ADDRESS;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Innstr. @ Passau", "Innstr. #4", "[regex]+", "092203", "Innstr"})
    void testRegexVariationPostalAddressFail(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_POSTAL_ADDRESS;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"085112345", "0852/3948", "0851 293948", "0832-1933", "(+49)852 2334",
            "(+432)232/323"})
    void testRegexVariationPhoneSuccessful(String text) {
        InputFieldType type = InputFieldType.CLASS_PHONE;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/8438", "0/323", "03939/3/3323", "(+49)/", "(+49)23423/", "9393/"})
    void testRegexVariationPhoneFail(String text) {
        InputFieldType type = InputFieldType.CLASS_PHONE;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"085112345", "0000"})
    void testRegexVariationNumberPWSuccessful(String text) {
        InputFieldType type = InputFieldType.NUMBER_VARIATION_PASSWORD;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/8438", "039"})
    void testRegexVariationNumberPWFail(String text) {
        InputFieldType type = InputFieldType.NUMBER_VARIATION_PASSWORD;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.23", "-12,3", "14", "22.00000", "-2", "+234"})
    void testRegexSignedNumberSuccessful(String text) {
        InputFieldType type = InputFieldType.NUMBER_FLAG_SIGNED;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1.2.3", ".", ".,"})
    void testRegexSignedNumberFail(String text) {
        InputFieldType type = InputFieldType.CLASS_NUMBER;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "123", "344"})
    void testRegexClassNumberSuccessful(String text) {
        InputFieldType type = InputFieldType.CLASS_NUMBER;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1.0", "abc"})
    void testRegexClassNumberFail(String text) {
        InputFieldType type = InputFieldType.CLASS_NUMBER;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0", "1.23", "0,23", "1"})
    void testRegexNumberFlagSuccessful(String text) {
        InputFieldType type = InputFieldType.NUMBER_FLAG_DECIMAL;
        assertPatternTrue(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.0", "+1.02", "3.02."})
    void testRegexNumberFlagFail(String text) {
        InputFieldType type = InputFieldType.NUMBER_FLAG_DECIMAL;
        assertPatternFalse(text, type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"31.10.2000", "2000-07-22", "12-25-1999", "07-15-2011", "31/07/2001"})
    void testDateFormatSuccessful(String text) {
        assertTrue(InputFieldType.isDate(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"32.10.2000", "200-07-22", "12--25-1999", "07-15", "31.07/2001"})
    void testDateFormatFail(String text) {
        assertFalse(InputFieldType.isDate(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"23:08:30", "22:00", "6:00", "10:01 AM"})
    void testTimeFormatSuccessful(String text) {
        assertTrue(InputFieldType.isTime(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"25:08:30", "22:00.05", "6", "pm"})
    void testTimeFormatFail(String text) {
        assertFalse(InputFieldType.isTime(text));
    }

    private void assertPatternTrue(String text, InputFieldType type) {
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertTrue(m.matches());
    }

    private void assertPatternFalse(String text, InputFieldType type) {
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertFalse(m.matches());
    }
}