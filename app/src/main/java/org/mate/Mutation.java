package org.mate;

import android.text.InputType;

import java.util.Random;

public class Mutation {

    private static final String SET_OF_LOW_LETTERS = "abcdefghijklmnopqrstuvwxyz";

    private static final String SET_OF_BIG_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String SET_OF_NUMBERS = "0123456789";

    private static final String SET_OF_SPECIAL_SIGNS = "+-*/!\"§$%&/()=?´`_.,@€<>|{[]}\\:;^°";

    private static final double PROBABILITY_POINT_IN_DATE_TIME = 0.8;


    private enum MutationType {
        ADDITION, CHANGE, DELETE;

        private static MutationType getRandomMutationType() {
            Random r = new Random();
            return MutationType.values()[r.nextInt(MutationType.values().length)];
        }
    }

    public static String mutateInput(int inputType, String hint) {
        switch (inputType) {
            case InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_CLASS_TEXT:
            case InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT:
            case InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT:
            case InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_CLASS_TEXT:
                return mutateString(hint, 2);

            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT:
                return mutateEmailAddress(hint, 2);

            case InputType.TYPE_CLASS_PHONE:
                return mutatePhone(hint, 2);

            case InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_NUMBER:
                return mutateNumber(hint, 2);
            case InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER:
                return mutateDecNumber(hint, 2);

            case InputType.TYPE_DATETIME_VARIATION_TIME | InputType.TYPE_CLASS_DATETIME:
                return mutateTime(hint);
            case InputType.TYPE_DATETIME_VARIATION_DATE | InputType.TYPE_CLASS_DATETIME:
                return mutateDate(hint);

            default:
                //    Log.d("inputType", hint + ":" + inputType + " (nonSucc)");
                return mutateString(hint + "", 2);
        }
    }

    private static String mutateString(String hint, int maxNumberMutation) {
        return mutateString(hint, maxNumberMutation, SET_OF_LOW_LETTERS + SET_OF_BIG_LETTERS);
    }

    private static String mutateString(String hint, int maxNumberMutation, String charSet) {
        return mutateString(hint, maxNumberMutation, charSet, null);
    }

    private static String mutateString(String hint, int maxNumberMutation, String charSet, MutationType mutationType) {
        StringBuilder stb = new StringBuilder(hint);
        Random r = new Random();
        for (int i = 0; i < maxNumberMutation; i++) {
            if (mutationType == null) {
                mutationType = MutationType.getRandomMutationType();
            }
            int randomNumber = r.nextInt(hint.length());
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
                    } else {
                        // TODO: Log message
                    }
                    break;
                default:
                    // TODO: Log message.
                    return hint;
            }
        }
        return stb.toString();
    }

    private static String mutateEmailAddress(String hint, int maxNumberMutation) {
        String[] emailParts = hint.split("@");
        String mutatedMail;
        Random r = new Random();
        if (r.nextDouble() < 0.5) {
            mutatedMail = hint;
        } else {
            mutatedMail = emailParts[0];
            if (emailParts.length > 1) {
                mutatedMail += emailParts[1];
            }
        }

        return mutateString(mutatedMail, maxNumberMutation, SET_OF_LOW_LETTERS + SET_OF_NUMBERS + SET_OF_SPECIAL_SIGNS);
    }

    private static String mutatePhone(String hint, int maxNumberMutation) {
        hint = hint.replace("/", "");
        hint = mutateNumber(hint, maxNumberMutation);
        Random r = new Random();
        if (r.nextDouble() < 0.5) {
            return hint;
        } else {
            int randomGenNumber = r.nextInt(hint.length());
            StringBuilder stb = new StringBuilder(hint);
            stb.replace(randomGenNumber, randomGenNumber, "\\");
            return stb.toString();
        }
    }

    private static String mutateNumber(String hint, int maxNumberMutation) {
        try {
            int number = Integer.parseInt(hint);
            Random r = new Random();
            int randomGenNumber = r.nextInt(hint.length() * 2) - hint.length();
            number += randomGenNumber;
            return String.valueOf(number);
        } catch (NumberFormatException e) {
            return mutateString(hint, maxNumberMutation);
        }
    }

    private static String mutateDecNumber(String number, int maxNumberMutation) {
        String[] parts = number.split("\\.");
        StringBuilder stb = new StringBuilder();
        stb.append(mutateNumber(parts[0], maxNumberMutation));
        if (parts.length >= 2) {
            stb.append(mutateNumber(parts[1], maxNumberMutation));
        }
        return stb.toString();
    }

    private static String mutateDate(String date) {
        return mutateDateOrTime(date, "\\.", '.');
    }

    private static String mutateTime(String time) {
        return mutateDateOrTime(time, ":", ':');
    }

    private static String mutateDateOrTime(String dateTime, String regex, char replacedChar) {
        String[] dateTimes = dateTime.split(regex);
        StringBuilder stb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < dateTimes.length; i++) {
            dateTimes[i] = mutateString(dateTimes[i], dateTimes[i].length(), SET_OF_NUMBERS);
            stb.append(dateTimes[i]);
            if (i + 1 != dateTimes.length && r.nextDouble() < PROBABILITY_POINT_IN_DATE_TIME) {
                stb.append(replacedChar);
            }
        }
        return stb.toString();
    }


    private static String generateRandomCharString(Random r, String charSet) {
        return String.valueOf(charSet.charAt(r.nextInt(charSet.length())));
    }
}
