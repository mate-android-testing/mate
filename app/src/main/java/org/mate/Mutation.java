package org.mate;

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
