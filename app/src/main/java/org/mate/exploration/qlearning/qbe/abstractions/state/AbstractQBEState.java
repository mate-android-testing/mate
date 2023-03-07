package org.mate.exploration.qlearning.qbe.abstractions.state;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.utils.MathUtils;
import org.mate.utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines an abstract QBE state.
 *
 * @param <A> The generic action type.
 */
public abstract class AbstractQBEState<A extends Action> implements State<A> {

    /**
     * The cosine similarity threshold.
     */
    private static final double COSINE_SIMILARITY_THRESHOLD = 0.95;

    /**
     * Matches the keys of both map. Missing values are set to 0. Runs in expected O(n) where
     * n = features1.size() + features2.size() assuming both maps are implemented as hash maps.
     * If any map is implemented as a tree map the algorithms runs in O(n*log(n)).
     *
     * @param features1 The first feature map.
     * @param features2 The second feature map.
     * @return Returns a pair consisting of content vectors.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private Pair<List<Double>, List<Double>> featureMapToContentVectors(
            final Map<String, Integer> features1, final Map<String, Integer> features2) {

        final Set<String> keys = new HashSet<>(features1.size() + features2.size());
        keys.addAll(features1.keySet());
        keys.addAll(features2.keySet());

        final List<Double> vector1 = new ArrayList<>(keys.size());
        final List<Double> vector2 = new ArrayList<>(keys.size());

        keys.forEach(key -> {
            vector1.add((double) features1.getOrDefault(key, 0));
            vector2.add((double) features2.getOrDefault(key, 0));
        });
        return new Pair<>(vector1, vector2);
    }

    /**
     * Retrieves the number of GUI components on the underlying screen.
     *
     * @return Returns the number of GUI components.
     */
    protected abstract int getNumberOfComponents();

    /**
     * Retrieves the feature map.
     *
     * @return Returns the feature map.
     */
    public abstract Map<String, Integer> getFeatureMap();

    /**
     * Compares two {@link AbstractQBEState}s for equality. See section IV.A) for more details.
     * Two states v and v' are considered equal iff they:
     *
     *      (1) share the same set of enabled actions
     *      (2) have the same number of components
     *      (3) have a cosine similarity (content vectors) above 0.95
     *
     * @param o The other {@link AbstractQBEState}.
     * @return Returns {@code true} if all of above three criterion are satisfied, otherwise
     *          {@code false} is returned.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof AbstractQBEState<?>)) {
            return false;
        } else {
            final AbstractQBEState<?> other = (AbstractQBEState<?>) o;
            if (!this.getActions().equals(other.getActions())
                    || this.getNumberOfComponents() != other.getNumberOfComponents()) {
                return false;
            } else {
                final Pair<List<Double>, List<Double>> contentVectors = featureMapToContentVectors(
                        this.getFeatureMap(), other.getFeatureMap());
                return MathUtils.cosineSimilarity(contentVectors.first, contentVectors.second)
                        > COSINE_SIMILARITY_THRESHOLD;
            }
        }
    }

    /**
     * Computes the hash code of the underlying state.
     *
     * @return Returns the hash code associated with the underlying state.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public final int hashCode() {
        // This hash function is correct, because equal states imply equal actions and equal number
        // of components. This in turn implies a equal hash value.
        //
        // The hash function does not use the feature map, because different features maps could
        // still imply equality if the two maps are similar enough (according to cosine similarity).
        int hash = Integer.hashCode(getActions().stream().mapToInt(Object::hashCode).sum());
        hash = 31 * hash + Integer.hashCode(getNumberOfComponents());
        return hash;
    }

}
