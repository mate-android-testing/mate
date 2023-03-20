package org.mate.utils.input_generation;


import org.mate.Registry;
import org.mate.utils.Randomness;
import org.mate.utils.input_generation.format_types.InputFieldType;

import java.util.Random;

import static org.mate.utils.input_generation.Letters.SET_OF_BIG_LETTERS;
import static org.mate.utils.input_generation.Letters.SET_OF_LOW_LETTERS;
import static org.mate.utils.input_generation.Letters.SET_OF_NUMBERS;
import static org.mate.utils.input_generation.Letters.SET_OF_SPECIAL_SIGNS;
import static org.mate.utils.input_generation.Letters.generatePossibleLetters;

/**
 * Class that mutates a given input.
 */
public class Mutation {

    /**
     * The probability that a full stop should be placed in a datum or tense.
     */
    private static final double PROBABILITY_POINT_IN_DATE_TIME = 0.8;

    /**
     * The maximum number of changes in a mutation process.
     */
    private static final int MUTATION_DEGREE = 2;

    /**
     * Enum for different mutation types.
     */
    private enum MutationType {

        /**
         * Stands for adding a character.
         */
        ADDITION,

        /**
         * Stands for substituting a character.
         */
        CHANGE,

        /**
         * Stands for deleting a character.
         */
        DELETE;

        /**
         * Randomly selects a mutation type. (Uniform distributed)
         *
         * @return A random mutation type.
         */
        private static MutationType getRandomMutationType() {
            Random r = Registry.getRandom();
            return MutationType.values()[r.nextInt(MutationType.values().length)];
        }
    }

    /**
     * Mutates a given input for a certain input field type. Various rules are defined that apply
     * to certain input field types.
     *
     * @param type The given input field type.
     * @param string The given string to mutate.
     * @return The mutated string.
     */
    public static String mutateInput(InputFieldType type, String string) {
        switch (type) {
            case TEXT_VARIATION_EMAIL:
                return mutateEmailAddress(string, MUTATION_DEGREE);

            case CLASS_PHONE:
                return mutatePhone(string, MUTATION_DEGREE);

            case NUMBER_VARIATION_PASSWORD:
            case NUMBER_FLAG_SIGNED:
            case CLASS_NUMBER_NORMAL:
                return mutateNumber(string, MUTATION_DEGREE);

            case NUMBER_FLAG_DECIMAL:
                return mutateDecNumber(string, MUTATION_DEGREE);

            case DATETIME_VARIATION_TIME:
                return mutateTime(string);
            case DATETIME_VARIATION_DATE:
                return mutateDate(string);

            case TEXT_VARIATION_PERSON_NAME:
            case TEXT_FLAG_MULTI_LINE:
            case TEXT_VARIATION_PASSWORD:
            case TEXT_VARIATION_POSTAL_ADDRESS:
            default:
                return mutateString(string, MUTATION_DEGREE);
        }
    }

    /**
     * Mutates a string with a maximum number of mutation operations maxNumberMutation. Upper and
     * lower case letters are considered.
     *
     * @param string The given string to mutate.
     * @param maxNumberMutation The max number of mutation operations.
     * @return The mutated string.
     */
    private static String mutateString(String string, int maxNumberMutation) {
        return mutateString(string, maxNumberMutation,
                generatePossibleLetters(SET_OF_BIG_LETTERS, SET_OF_LOW_LETTERS));
    }

    /**
     * Mutates a string with a maximum number of mutation operations maxNumberMutation for a given
     * charset.
     *
     * @param string The given string to mutate.
     * @param maxNumberMutation The max number of mutation operations.
     * @param charSet The given charset.
     * @return The mutated string.
     */
    private static String mutateString(String string, int maxNumberMutation, String charSet) {
        return mutateString(string, maxNumberMutation, charSet, null);
    }

    /**
     * Mutates a string with a maximum number of mutation operations maxNumberMutation for a given
     * charset and a given mutation type. If {@code null} is input a random {@link MutationType} is
     * taken.
     *
     * @param string The given string to mutate.
     * @param maxNumberMutation The max number of mutation operations.
     * @param charSet The given charset.
     * @param mutationType A given mutationType.
     * @return The mutated string considering all these parameters.
     */
    private static String mutateString(String string, int maxNumberMutation, String charSet, MutationType mutationType) {
        StringBuilder stb = new StringBuilder(string);
        Random r = Registry.getRandom();
        for (int i = 0; i < maxNumberMutation; i++) {
            if (mutationType == null) {
                mutationType = MutationType.getRandomMutationType();
            }
            int randomNumber = r.nextInt(string.length());
            switch (mutationType) {
                case CHANGE:
                    stb.replace(randomNumber, randomNumber + 1, generateRandomCharString(r, charSet));
                    break;
                case ADDITION:
                    stb.replace(randomNumber, randomNumber, generateRandomCharString(r, charSet));
                    break;
                case DELETE:
                    if (stb.length() != 0) {
                        stb.replace(randomNumber, randomNumber + 1, "");
                    }
                    break;
                default:
                    return string;
            }
        }
        return stb.toString();
    }

    /**
     * Mutates an email address. Sometimes invalid email addresses can occur.
     * (For example, removing the @ sign).
     *
     * @param email The given email address.
     * @param maxNumberMutation The maximal number of mutation operations.
     * @return A mutated email address.
     */
    private static String mutateEmailAddress(String email, int maxNumberMutation) {
        String[] emailParts = email.split("@");
        String mutatedMail;
        Random r = Registry.getRandom();
        if (r.nextDouble() < 0.5) {
            mutatedMail = email;
        } else {
            mutatedMail = emailParts[0];
            if (emailParts.length > 1) {
                mutatedMail += emailParts[1];
            }
        }

        return mutateString(mutatedMail, maxNumberMutation,
                generatePossibleLetters(SET_OF_LOW_LETTERS, SET_OF_NUMBERS, SET_OF_SPECIAL_SIGNS));
    }

    /**
     * Mutates phone number. Sometimes invalid phone numbers can occur.
     *
     * @param phone The given phone number.
     * @param maxNumberMutation The maximal number of mutation operations.
     * @return A mutated phone number.
     */
    private static String mutatePhone(String phone, int maxNumberMutation) {
        phone = phone.replace("/", "");
        phone = mutateNumber(phone, maxNumberMutation);
        Random r = Registry.getRandom();
        if (r.nextDouble() < 0.5) {
            return phone;
        } else {
            int randomGenNumber = r.nextInt(phone.length());
            StringBuilder stb = new StringBuilder(phone);
            stb.replace(randomGenNumber, randomGenNumber, "\\");
            return stb.toString();
        }
    }

    /**
     * Mutates a number by adding a random number in the same length of current number.
     *
     * @param num The given number.
     * @param maxNumberMutation The maximal number of mutation operations.
     * @return A mutated number.
     */
    private static String mutateNumber(String num, int maxNumberMutation) {
        try {
            int number = Integer.parseInt(num);
            Random r = Registry.getRandom();
            int randomGenNumber = r.nextInt(num.length() * 2) - num.length();
            number += randomGenNumber;
            return String.valueOf(number);
        } catch (NumberFormatException e) {
            return mutateString(num, maxNumberMutation);
        }
    }

    /**
     * Mutates a decimal number by removing the full stop and mutating the two seperated parts.
     *
     * @param decimalNumber The given number.
     * @param maxNumberMutation The maximal number of mutation operations.
     * @return A mutated number.
     */
    private static String mutateDecNumber(String decimalNumber, int maxNumberMutation) {
        String[] parts = decimalNumber.split("\\.");
        StringBuilder stb = new StringBuilder();
        stb.append(mutateNumber(parts[0], maxNumberMutation));
        if (Randomness.getRnd().nextDouble() < PROBABILITY_POINT_IN_DATE_TIME) {
            stb.append(".");
        }
        if (parts.length >= 2) {
            stb.append(mutateNumber(parts[1], maxNumberMutation));
        }
        return stb.toString();
    }

    /**
     * Mutates a given date string.
     *
     * @param date The given date string.
     * @return A mutated date is returned.
     */
    private static String mutateDate(String date) {
        return mutateDateOrTime(date, "\\.", '.');
    }

    /**
     * Mutates a given time string.
     *
     * @param time The given time string.
     * @return A mutated time is returned.
     */
    private static String mutateTime(String time) {
        return mutateDateOrTime(time, ":", ':');
    }

    /**
     * Mutates a date or time string. Sometimes the synthax of the date or time string is changed or
     * invalid number are produced. For example: 30/02/2000 25:01
     *
     * @param dateTime The given date time string.
     * @param regex The regex where the syntax should be changed.
     * @param replacedChar A char replacing the given regex.
     * @return A mutated date or time string.
     */
    private static String mutateDateOrTime(String dateTime, String regex, char replacedChar) {
        String[] dateTimes = dateTime.split(regex);
        StringBuilder stb = new StringBuilder();
        Random r = Registry.getRandom();
        for (int i = 0; i < dateTimes.length; i++) {
            dateTimes[i] = mutateString(dateTimes[i], dateTimes[i].length(),
                    SET_OF_NUMBERS.getLetters(), MutationType.CHANGE);
            stb.append(dateTimes[i]);
            if (i + 1 != dateTimes.length && r.nextDouble() < PROBABILITY_POINT_IN_DATE_TIME) {
                stb.append(replacedChar);
            }
        }
        return stb.toString();
    }

    /**
     * Returns a random char of a given char set.
     *
     * @param r The random object.
     * @param charSet The possible chars.
     * @return A random char of the set.
     */
    private static String generateRandomCharString(Random r, String charSet) {
        return String.valueOf(charSet.charAt(r.nextInt(charSet.length())));
    }
}
