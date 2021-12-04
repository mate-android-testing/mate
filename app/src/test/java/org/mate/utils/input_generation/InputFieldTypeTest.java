package org.mate.utils.input_generation;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class InputFieldTypeTest {


    @ParameterizedTest
    @ValueSource(strings = {"Max Mustermann", "G. Milner","Hans-Xaver","max", "Stefan", "v. Goethe","Müller"})
    void testRegexVariationPersonNameSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_PERSON_NAME;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertTrue(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"new\nLine", "new\ttab","Hello!","\r", "", "+","-","$","462",":","[b-z]*","of"})
    void testRegexVariationPersonNameFail(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_PERSON_NAME;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertFalse(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello it's \n a multi\nline\rtest!",".;:-+*","&h10)2"})
    void testRegexVariationMultiLineSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_FLAG_MULTI_LINE;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertTrue(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"of","\n","\n\n"})
    void testRegexVariationMultiLineFail(String text) {
        InputFieldType type = InputFieldType.TEXT_FLAG_MULTI_LINE;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertFalse(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"password","password123","pw#10ef","goodJob1234;"})
    void testRegexVariationPWSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_PASSWORD;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertTrue(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test a","ab", "hello\nworld"})
    void testRegexVariationPWFail(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_PASSWORD;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertFalse(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"max.muster@gmail.com","hello@test.de","fsinfo@fim.uni-passau.de","hel_lo123@hello.com"})
    void testRegexVariationEmailSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_EMAIL;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertTrue(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"max.muster@","@yahoo.de","xyz@gmail","abc@@bce.de","hello!@test.de","h@h@world.com","no_Valid"})
    void testRegexVariationEmailFail(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_EMAIL;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertFalse(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Innstraße 41, 94032 Passau", "Innstr. 41 \n 94032 Passau","Innstr-weg 41","Innstra","Innstr. 41a \n 94032 Passau \n GERMANY"})
    void testRegexVariationPostalAddressSuccessful(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_POSTAL_ADDRESS;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertTrue(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Innstr. @ Passau", "Innstr. #4","[regex]+","092203","Innstr"})
    void testRegexVariationPostalAddressFail(String text) {
        InputFieldType type = InputFieldType.TEXT_VARIATION_POSTAL_ADDRESS;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertFalse(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"085112345","0852/3948","0851 293948", "0832-1933","(+49)852 2334","(+432)232/323"})
    void testRegexVariationPhoneSuccessful(String text) {
        InputFieldType type = InputFieldType.CLASS_PHONE;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertTrue(m.matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/8438","0/323","03939/3/3323","(+49)/","(+49)23423/","9393/"})
    void testRegexVariationPhoneFail(String text) {
        InputFieldType type = InputFieldType.CLASS_PHONE;
        Pattern p = Pattern.compile(type.getRegex());
        Matcher m = p.matcher(text);
        assertFalse(m.matches());
    }
}