package org.mate;

import android.text.InputType;
import android.util.Log;

import org.mate.interaction.action.ui.Widget;

import java.util.Random;

public class Mutation {

    private static final String SET_OF_LOW_LETTERS = "abcdefghijklmnopqrstuvwxyz";

    private static final String SET_OF_BIG_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String SET_OF_NUMBERS = "0123456789";

    private static final String SET_OF_SPECIAL_SIGNS = "+-*/!\"§$%&/()=?´`_.,@€<>|{[]}\\:;^°";


    private enum MutationType {
        ADDITION, CHANGE, DELETE;

        private static MutationType getRandomMutationType() {
            Random r = new Random();
            int randomNumber = r.nextInt(3);
            if (randomNumber == 0.5) {
                return ADDITION;
            } else if (randomNumber == 1) {
                return CHANGE;
            } else {
                return DELETE;
            }
        }
    }

    public static String mutateInput(int inputType, String hint) {
        switch (inputType) {
            case InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_CLASS_TEXT:
            case InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT:
            case InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT:
            case InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_CLASS_TEXT:
                return mutateString(hint, 2); //DONE

            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT:
                return mutateEmailAddress(hint, 2); //DONE

            case InputType.TYPE_CLASS_PHONE:
                return mutatePhone(hint, 2);

            case InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_NUMBER:
                return mutateNumber(hint, 2);
            case InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER:
                return mutateDecNumber(hint, 2);

            case InputType.TYPE_DATETIME_VARIATION_TIME | InputType.TYPE_CLASS_DATETIME:
                return mutateTime(hint, 2);
            case InputType.TYPE_DATETIME_VARIATION_DATE | InputType.TYPE_CLASS_DATETIME:
                return mutateDate(hint, 2);

            default:
            //    Log.d("inputType", hint + ":" + inputType + " (nonSucc)");
                return mutateString(hint + "", 2);
        }
    }

    private static String mutateString(String hint, int maxNumberMutation){
        return mutateString(hint, maxNumberMutation,SET_OF_LOW_LETTERS+SET_OF_BIG_LETTERS);
    }

    private static String mutateString(String hint, int maxNumberMutation, String charSet) {
        StringBuilder stb = new StringBuilder(hint);
        Random r = new Random();
        for (int i = 0; i < maxNumberMutation; i++) {
            MutationType mutationType = MutationType.getRandomMutationType();
            int randomNumber = r.nextInt(hint.length());
            switch (mutationType) {
                case CHANGE:
                    stb.replace(randomNumber, randomNumber + 1, generateRandomCharString(r,charSet));
                    break;
                case ADDITION:
                    stb.replace(randomNumber, randomNumber, generateRandomCharString(r,charSet));
                    break;
                case DELETE:
                    //TODO: What if stb.size() == 0?
                    stb.replace(randomNumber, randomNumber + 1, "");
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
        if(r.nextDouble()<0.5){
            mutatedMail = hint;
        } else{
            mutatedMail = emailParts[0];
            if(emailParts.length >1){
                mutatedMail +=emailParts[1];
            }
        }

        return mutateString(mutatedMail, maxNumberMutation,SET_OF_LOW_LETTERS+SET_OF_NUMBERS+SET_OF_SPECIAL_SIGNS);
    }

    private static String mutatePhone(String hint, int maxNumberMutation) {
        hint = hint.replace("/","");
        return mutateNumber(hint, maxNumberMutation);
    }

    private static String mutateNumber(String hint, int maxNumberMutation){
        try{
            int number = Integer.parseInt(hint);
            Random r = new Random();
           int randomGenNumber= r.nextInt(hint.length()*2)-hint.length();
           number +=randomGenNumber;
           return String.valueOf(number);
        } catch (NumberFormatException e){
            return mutateString(hint, maxNumberMutation);
        }
    }

    private static String mutateDecNumber(String number, int maxNumberMutation){
        return mutateString(number, maxNumberMutation);
    }

    private static String mutateDate(String number, int maxNumberMutation){
        return mutateString(number, maxNumberMutation);
    }
    private static String mutateTime(String number, int maxNumberMutation){
        return mutateString(number, maxNumberMutation);
    }



    private static String generateRandomCharString(Random r, String charSet) {
        return String.valueOf(charSet.charAt(r.nextInt(charSet.length())));
    }
}
