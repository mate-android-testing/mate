package org.mate.utils.input_generation;

/**
 * Enum containing possible letter combinations.
 */
public enum Letters {

    /**
     * A set consisting of lower case letters.
     */
    SET_OF_LOW_LETTERS("abcdefghijklmnopqrstuvwxyz"),

    /**
     * A set consisting of capital letters.
     */
    SET_OF_BIG_LETTERS("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),

    /**
     * A set consisting of numbers.
     */
    SET_OF_NUMBERS("0123456789"),

    /**
     * A set consisting of special characters.
     */
    SET_OF_SPECIAL_SIGNS("+-*/!\"§$%&/()=?´`_.,@€<>|{[]}\\:;^°"),

    /**
     * A set consisting of a linebreak.
     */
    NEW_LINE("\n"),

    /**
     * Union of quantities with lower case, upper case and numbers.
     */
    SET_OF_LOW_BIG_NUMBER_LETTERS(SET_OF_LOW_LETTERS, SET_OF_BIG_LETTERS, SET_OF_NUMBERS);

    /**
     * The possible letters of a set.
     */
    private final String possibleLetters;

    /**
     * Constructor that takes the possible letters.
     *
     * @param possibleLetters The possible letters.
     */
    Letters(String possibleLetters) {
        this.possibleLetters = possibleLetters;
    }

    /**
     * Constructor that takes a set of {@link Letters} and unites them.
     *
     * @param letters The sets of letters, that will be united.
     */
    Letters(Letters... letters) {
        this.possibleLetters = generatePossibleLetters(letters);
    }

    /**
     * Getter for the possible Letters.
     *
     * @return The string of possible letters.
     */
    public String getLetters() {
        return possibleLetters;
    }

    /**
     * Unites a set of letters.
     *
     * @param letters The letters that should be united.
     * @return The string representation of the united letters.
     */
    public static String generatePossibleLetters(Letters... letters) {
        StringBuilder stb = new StringBuilder();
        for (Letters l : letters) {
            stb.append(l.getLetters());
        }
        return stb.toString();
    }
}
