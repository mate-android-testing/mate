package org.mate.utils.input_generation;

public enum Letters {
    SET_OF_LOW_LETTERS("abcdefghijklmnopqrstuvwxyz"),
    SET_OF_BIG_LETTERS("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    SET_OF_NUMBERS("0123456789"),
    SET_OF_SPECIAL_SIGNS("+-*/!\"§$%&/()=?´`_.,@€<>|{[]}\\:;^°"),
    NEW_LINE("\n"),
    SET_OF_LOW_BIG_NUMBER_LETTERS(SET_OF_LOW_LETTERS, SET_OF_BIG_LETTERS, SET_OF_NUMBERS);

    private final String possibleLetters;

    Letters(String possibleLetters) {
        this.possibleLetters = possibleLetters;
    }

    Letters(Letters... letters) {
        this.possibleLetters = generatePossibleLetters(letters);
    }

    public String getLetters() {
        return possibleLetters;
    }


    public static String generatePossibleLetters(Letters... letters) {
        StringBuilder stb = new StringBuilder();
        for (Letters l : letters) {
            stb.append(l.getLetters());
        }
        return stb.toString();
    }
}
