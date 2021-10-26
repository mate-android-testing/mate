package org.mate;

import android.text.InputType;
import android.util.Log;

import org.mate.interaction.action.ui.Widget;

import java.util.Random;

public class Mutation {


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
            case InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_CLASS_TEXT: //input field for person name
                return mutateString(hint, 2);
            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT: //input field for email address
                return mutateEmailAddress(hint, 2);
            case InputType.TYPE_CLASS_PHONE:                                            //for phone number
                return mutatePhone(hint, 2);
            case InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_CLASS_TEXT: // input field for cip code
                return mutateCIPCode(hint, 2);
            case InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT:   //for pw
                return mutateString(hint, 2);
            case InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT:      // for more lines input
                return mutateString(hint,2);
            case InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER: // for more lines input
                return mutateString(hint,2);
            case InputType.TYPE_DATETIME_VARIATION_TIME | InputType.TYPE_CLASS_DATETIME:
                return mutateString(hint,2);
            case InputType.TYPE_DATETIME_VARIATION_DATE | InputType.TYPE_CLASS_DATETIME:
                return mutateString(hint,2);
            case InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER:
                return mutateString(hint,2);
            case InputType.TYPE_CLASS_NUMBER:
                return mutatePhone(hint,2);
            case InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER:
                return mutatePhone(hint,2);
            default:
                Log.d("inputType", hint + ":" + inputType + " (nonSucc)");
                return mutateString(hint + "", 2);
        }
    }

    public static String mutateEmailAddress(String hint, int maxNumberMutation) {
        return mutateString(hint, maxNumberMutation);
    }

    public static String mutatePhone(String hint, int maxNumberMutation) {
        return mutateString(hint, maxNumberMutation);
    }

    public static String mutateString(String hint, int maxNumberMutation) {
        StringBuilder stb = new StringBuilder(hint);
        Random r = new Random();
        for (int i = 0; i < maxNumberMutation; i++) {
            MutationType mutationType = MutationType.getRandomMutationType();
            int randomNumber = r.nextInt(hint.length());
            switch (mutationType) {
                case CHANGE:
                    stb.replace(randomNumber, randomNumber + 1, generateRandomCharString(r));
                    break;
                case ADDITION:
                    stb.replace(randomNumber, randomNumber, generateRandomCharString(r));
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

    private static String generateRandomCharString(Random r) {
        return String.valueOf(((char) (r.nextInt(26) + 'a')));
    }

    public static String mutateCIPCode(String hint, int mutationNumber) {
        return mutateString(hint,mutationNumber);
    }
}
