package org.mate.state.equivalence.checks;

import org.mate.Properties;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.state.IScreenState;
import org.mate.state.equivalence.IStateEquivalence;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Compares two {@link IScreenState}s for similarity based on the extracted features. The
 * implementation follows the description in https://ieeexplore.ieee.org/abstract/document/8786935.
 * If the computed cosine similarity coefficient is above the specified cosine similarity threshold,
 * see {@link Properties#COSINE_SIMILARITY_THRESHOLD()}, the states are considered equal.
 */
public class CosineSimilarity implements IStateEquivalence {

    @Override
    public boolean checkEquivalence(IScreenState first, IScreenState second) {

        Objects.requireNonNull(first, "First screen state must be not null!");
        Objects.requireNonNull(second, "Second screen state must be not null!");

        Map<String, Integer> firstVector = extractFeatureVector(first);
        Map<String, Integer> secondVector = extractFeatureVector(second);

        double cosineSimilarityCoefficient;
        Set<String> keys
                = Stream.concat(firstVector.keySet().stream(), secondVector.keySet().stream())
                .collect(Collectors.toSet());

        /*
         * The formula for computing the number of equal features can be simplified by looking at
         * the distinct keys.
         */
        int numberOfEqualFeatures = firstVector.size() + secondVector.size() - keys.size();

        /*
         * The original formula can be simplified as follows. Since all entries in the feature
         * vector are either 0 or 1, the square operation is redundant. Similarly, the sum over the
         * squared feature vector entries is simply the size of the vector as we only track the
         * defined features (v(i) = 1). This corresponds to the Otsuka–Ochiai similarity as we use
         * binary vectors essentially.
         */
        cosineSimilarityCoefficient = numberOfEqualFeatures /
                (Math.sqrt(firstVector.size()) * Math.sqrt(secondVector.size()));

        return cosineSimilarityCoefficient >= Properties.COSINE_SIMILARITY_THRESHOLD();
    }

    /**
     * Extracts the feature vector for the given state.
     *
     * @param state The state for which the feature vector should be extracted.
     * @return Returns the feature vector for the given state.
     */
    private Map<String, Integer> extractFeatureVector(IScreenState state) {

        Map<String, Integer> featureVector = new HashMap<>();

        for (Widget widget : state.getWidgets()) {

            /*
             * An entry in the feature vector is the string concatenation of the widget's class,
             * depth and text/content description attribute. It seems like the description of buttons
             * for instance is primarily saved in the text instead of the content description field.
             * Thus, we prefer the text attribute if the content description is empty.
             */
            String text = widget.getContentDesc().isEmpty() ? widget.getText() : widget.getContentDesc();

            String key = String.format("%s@%s@%s", widget.getClazz(), widget.getDepth(), text);
            featureVector.put(key, 1);
        }

        return featureVector;
    }
}

