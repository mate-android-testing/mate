package org.mate.representation.input_generation;

import org.mate.commons.input_generation.Mutation;
import org.mate.commons.input_generation.StaticStrings;
import org.mate.commons.input_generation.StaticStringsParser;
import org.mate.commons.input_generation.format_types.InputFieldType;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.representation.ExplorationInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TextDataGenerator {

    /**
     * The probability for considering the hint for the input generation.
     */
    private static final double PROB_HINT = 0.5;

    /**
     * The probability for mutating a given hint.
     */
    private static final double PROB_HINT_MUTATION = 0.5;

    /**
     * The probability for using a static string or the input generation.
     */
    private static final double PROB_STATIC_STRING = 0.5;

    /**
     * The probability for mutating a static string.
     */
    private static final double PROB_STATIC_STRING_MUTATION = 0.25;

    /**
     * Contains the static strings extracted from the byte code.
     */
    private final StaticStrings staticStrings;

    private static TextDataGenerator textDataGenerator;

    private TextDataGenerator() {
        this.staticStrings = StaticStringsParser.parseStaticStrings();
    }

    public static TextDataGenerator getInstance() {
        if (textDataGenerator == null) {
            textDataGenerator = new TextDataGenerator();
        }

        return textDataGenerator;
    }


    /**
     * Generates a text input for the given editable widget.
     *
     * @param widget The editable widget.
     * @param maxLength The maximal input length.
     * @return Returns a text input for the editable widget.
     */
    public String generateTextData(final Widget widget,
                                    final int maxLength) {

        final String activityName = widget.getActivity();

        final InputFieldType inputFieldType = InputFieldType.getFieldTypeByNumber(widget.getInputType());
        final Random random = ExplorationInfo.getInstance().getRandom();

        /*
         * If a hint is present and with probability PROB_HINT we select the hint as input. Moreover,
         * with probability PROB_HINT_MUTATION we mutate the given hint.
         */
        if (widget.isHintPresent()) {
            if (inputFieldType.isValid(widget.getHint()) && random.nextDouble() < PROB_HINT) {
                if (inputFieldType != InputFieldType.NOTHING && random.nextDouble() < PROB_HINT_MUTATION) {
                    return Mutation.mutateInput(inputFieldType, widget.getHint());
                } else {
                    return widget.getHint();
                }
            }
        }
        if (staticStrings.isInitialised()) {
            /*
             * If the static strings from the bytecode were supplied and with probability
             * PROB_STATIC_STRING we try to find a static string matching the input field type.
             */
            if (random.nextDouble() < PROB_STATIC_STRING) {

                // consider both the string constants from the current activity and visible fragments
                List<String> uiComponents = new ArrayList<>();
                uiComponents.add(activityName);
                uiComponents.addAll(ExplorationInfo.getInstance().getCurrentFragments());

                String randomStaticString;

                if (inputFieldType != InputFieldType.NOTHING) {

                    // get a random string matching the input field type from one of the ui classes
                    randomStaticString = staticStrings.getRandomStringFor(inputFieldType, uiComponents);

                    /*
                     * If there was no match, we consider a random string from any class matching
                     * the given input field type.
                     */
                    if (randomStaticString == null) {
                        randomStaticString = staticStrings.getRandomStringFor(inputFieldType);
                    }

                    // mutate the string with probability PROB_STATIC_STRING_MUTATION
                    if (randomStaticString != null) {
                        if (random.nextDouble() < PROB_STATIC_STRING_MUTATION) {
                            randomStaticString = Mutation.mutateInput(inputFieldType, randomStaticString);
                        }
                        return randomStaticString;
                    }
                }

                /*
                 * If the input field type couldn't be determined or no static string could be
                 * derived so far, we try to use a random string from either the current activity
                 * or any of the visible fragments.
                 */
                randomStaticString = staticStrings.getRandomStringFor(uiComponents);
                if (randomStaticString != null) {
                    return randomStaticString;
                }
            }
        }

        // fallback mechanism
        return generateRandomInput(inputFieldType, maxLength);
    }

    /**
     * Generates a random input as a fallback mechanism. A random string is generated and shortened
     * to the maximum length if it is too long.
     *
     * @param inputFieldType The field for which the string is to be generated.
     * @param maxLength The maximum length of the result string.
     * @return A random string matching the given {@link InputFieldType} with at most maxLength
     *         length.
     */
    private String generateRandomInput(InputFieldType inputFieldType, int maxLength) {
        String randomData = DataGenerator.generateRandomData(inputFieldType);
        if (maxLength > 0 && randomData.length() > maxLength) {
            randomData = randomData.substring(0, maxLength);
        }
        return randomData;
    }
}
