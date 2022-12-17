package org.mate.utils.input_generation;

import org.mate.utils.Randomness;
import org.mate.utils.input_generation.format_types.DateFormat;
import org.mate.utils.input_generation.format_types.InputFieldType;
import org.mate.utils.input_generation.format_types.TimeFormat;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mate.utils.input_generation.Letters.SET_OF_LOW_BIG_NUMBER_LETTERS;
import static org.mate.utils.input_generation.Letters.SET_OF_LOW_LETTERS;
import static org.mate.utils.input_generation.Letters.SET_OF_NUMBERS;
import static org.mate.utils.input_generation.Letters.SET_OF_SPECIAL_SIGNS;

/**
 * Generates random input strings.
 */
public final class DataGenerator {

    /**
     * The probability for a line break.
     */
    public static final double PROB_LINEBREAK = 0.2;

    /**
     * The maximal length of number string.
     */
    public static final int BOUND_NUMBERS = 8;

    /**
     * The maximal length of a string.
     */
    public static final int BOUND_STRING = 10;

    /**
     * The maximal length of a string with multi lines.
     */
    public static final int MAX_STRING_LENGTH_MULTILINE = 50;

    /**
     * The loaded words of dictionary.
     */
    private static final List<String> words = Dictionary.getWords();

    /**
     * Generates some random date for a given {@link InputFieldType}.
     *
     * @param type The type for generation of random data.
     * @return The generated data.
     */
    public static String generateRandomData(InputFieldType type) {

        switch (type) {
            case TEXT_FLAG_MULTI_LINE:
                return generateMultiLine(Randomness.getRnd().nextInt(MAX_STRING_LENGTH_MULTILINE));

            case TEXT_VARIATION_POSTAL_ADDRESS:
                return generateMultiLine(Randomness.getRnd().nextInt(MAX_STRING_LENGTH_MULTILINE),
                        Letters.SET_OF_BIG_LETTERS,
                        Letters.SET_OF_LOW_LETTERS,
                        Letters.SET_OF_NUMBERS);

            case TEXT_VARIATION_PERSON_NAME:
                return generateRandomString(BOUND_STRING, SET_OF_LOW_BIG_NUMBER_LETTERS);
            case TEXT_VARIATION_PASSWORD:
                return generateRandomString(BOUND_STRING, SET_OF_LOW_BIG_NUMBER_LETTERS, SET_OF_SPECIAL_SIGNS);
            case TEXT_VARIATION_EMAIL:
                return generateRandomEmail();
            case CLASS_PHONE:
                return generateRandomPhone();
            case NUMBER_FLAG_SIGNED:
                return generateRandomNumber(true, BOUND_NUMBERS);
            case NUMBER_VARIATION_PASSWORD:
                return generateRandomNumber(false, BOUND_NUMBERS);

            case CLASS_NUMBER:
            case NUMBER_FLAG_DECIMAL:
                return generateRandomDecNumber(BOUND_NUMBERS);
            case DATETIME_VARIATION_TIME:
                return generateRandomTime();
            case DATETIME_VARIATION_DATE:
                return generateRandomDate();
            default:
                return generateRandomString(BOUND_STRING);
        }
    }

    /**
     * Generates a multi line string with some words of the {@link Dictionary} or randomly generated
     * strings.
     *
     * @param maxLength The maximal length of the multiline string.
     * @param letters The possible letters.
     * @return A multi line string.
     */
    private static String generateMultiLine(final int maxLength, Letters... letters) {

        Random random = Randomness.getRnd();
        StringBuilder stb = new StringBuilder();
        boolean finished = false;

        // Generate some random words of a file with random linebreak.
        while (!finished) {

            // First a random word is selected of the dictionary, and with a certain probability
            // replaced by a set of letters or a linebreak.
            String nextWord = Randomness.randomElement(words);
            if (random.nextDouble() < 0.25) {
                nextWord = generateRandomString(nextWord.length(), letters);
            } else if (random.nextDouble() < PROB_LINEBREAK) {
                nextWord = "\n";
            }

            // Adds this generated word to the rest.
            if (stb.length() <= maxLength - 1 - nextWord.length()) {
                stb.append(nextWord).append(" ");
            } else {
                finished = true;
            }
        }

        // If no word with given length was found.
        if (stb.length() == 0) {

            // pick any word that is not longer than maxLength
            Set<String> wordsUpToMaxLength = words.stream()
                    .filter(word -> word.length() < maxLength)
                    .collect(Collectors.toSet());

            if (!wordsUpToMaxLength.isEmpty()) {
                stb.append(Randomness.randomElement(wordsUpToMaxLength));
            }
        } else {

            // Remove the last white space.
            stb.replace(stb.length() - 2, stb.length(), "");
        }
        return stb.toString();
    }

    /**
     * Generates a random one line String. If no letters are given a random word of the
     * {@link Dictionary} is taken, otherwise a random letter string is generated.
     *
     * @param maxLength The maximal length of the one line string.
     * @param letters The given letters.
     * @return A generated string.
     */
    private static String generateRandomString(int maxLength, Letters... letters) {
        StringBuilder stb = new StringBuilder();

        // If no specific letters are given, we choose a random word from the dictionary.
        if (letters == null || letters.length == 0) {
            String randomWord = Randomness.randomElement(words);
            if (randomWord.length() < maxLength) {
                stb.append(randomWord);
                return stb.toString();
            } else {

                // If no matching word is found, we generate a random word with all possible letters.
                return generateLetterString(maxLength, Letters.values());
            }
        }

        // Otherwise, we create a random word with our letters.
        return generateLetterString(maxLength, letters);
    }

    /**
     * Generates a letter string consisting of letters with a maxLength.
     *
     * @param maxLength The maximal length of the letter string.
     * @param letters The letters where a string should build.
     * @return The generated random letter string.
     */
    private static String generateLetterString(int maxLength, Letters... letters) {
        if (maxLength <= 0) {
            return "";
        }
        StringBuilder stb = new StringBuilder();
        String possibleLetters = Letters.generatePossibleLetters(letters);
        for (int i = stb.length(); i < maxLength; i++) {
            int indexLetter = Randomness.getRnd().nextInt(possibleLetters.length());
            stb.append(possibleLetters.charAt(indexLetter));
        }
        return stb.toString();
    }

    /**
     * Generates a random email address with full stops and the @ sign in the middle.
     *
     * @return A random and valid email address is returned.
     */
    private static String generateRandomEmail() {
        StringBuilder stb = new StringBuilder();
        Random random = Randomness.getRnd();
        stb.append(generateLetterString(random.nextInt(8), SET_OF_LOW_BIG_NUMBER_LETTERS))
                .append(".")
                .append(generateLetterString(random.nextInt(8), SET_OF_LOW_BIG_NUMBER_LETTERS))
                .append("@")
                .append(generateLetterString(random.nextInt(5), SET_OF_LOW_BIG_NUMBER_LETTERS))
                .append(".")
                .append(generateLetterString(random.nextInt(3), SET_OF_LOW_LETTERS));
        return stb.toString();
    }

    /**
     * Generates a random phone number. The country code is sometimes added at front of the number.
     * Prefix and identifier number are separated by / sign.
     *
     * @return A valid random phone number.
     */
    private static String generateRandomPhone() {
        Random random = Randomness.getRnd();
        StringBuilder stb = new StringBuilder();
        if (random.nextBoolean()) {
            stb.append("(+");
            stb.append(generateRandomString(2, SET_OF_NUMBERS));
            stb.append(") ");
        }
        stb.append(generateRandomString(2 + random.nextInt(4), SET_OF_NUMBERS))
                .append("/")
                .append(generateRandomString(2 + random.nextInt(8), SET_OF_NUMBERS));

        return stb.toString();

    }

    /**
     * Generates a signed random number string.
     *
     * @param signed Indicates, if the number should be signed or not.
     * @param bound The maximal length of the number string.
     * @return Returns a random number.
     */
    private static String generateRandomNumber(boolean signed, int bound) {
        bound--;
        if (bound <= 1) {
            bound = 1;
        }
        String number = generateRandomString(1 + Randomness.getRnd().nextInt(bound), SET_OF_NUMBERS);
        if (signed) {
            return Randomness.getRnd().nextBoolean() ? number : "-" + number;
        }
        return number;
    }

    /**
     * Generates a random decimal number with a given maximal length.
     *
     * @param bound The maximal length.
     * @return A random decimal number.
     */
    private static String generateRandomDecNumber(int bound) {
        int countFirstNumbers = bound - Randomness.getRnd().nextInt(bound) - 1;
        StringBuilder stb = new StringBuilder(generateRandomNumber(true, countFirstNumbers));
        if (Randomness.getRnd().nextBoolean()) {
            stb.append(".");
        }
        stb.append(generateRandomNumber(false, Math.max(1, bound - countFirstNumbers)));
        return stb.toString();
    }

    /**
     * Generates a random date for a random regex given in {@link DateFormat}.
     *
     * @return A valid date string.
     */
    private static String generateRandomDate() {
        Date d = Date.from(timestamp());
        DateFormat df = Randomness.randomElement(Arrays.asList(DateFormat.values().clone()));
        SimpleDateFormat sf = new SimpleDateFormat(df.getPattern());
        return sf.format(d);
    }

    /**
     * Generates a random time for a random regex given in {@link TimeFormat}.
     *
     * @return A valid time string.
     */
    private static String generateRandomTime() {
        Date d = Date.from(timestamp());
        TimeFormat tf = Randomness.randomElement(Arrays.asList(TimeFormat.values().clone()));
        SimpleDateFormat sf = new SimpleDateFormat(tf.getPattern());
        return sf.format(d);
    }

    /**
     * Calculates  a random timestamp.
     *
     * @return A random timestamp.
     */
    private static Instant timestamp() {
        return Instant.ofEpochSecond(Randomness.getRnd().nextInt());
    }
}
