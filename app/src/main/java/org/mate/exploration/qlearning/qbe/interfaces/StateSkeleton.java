package org.mate.exploration.qlearning.qbe.interfaces;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.utils.MathUtils;
import org.mate.utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.N)
public abstract class StateSkeleton<A extends Action> implements State<A> {

    protected static final double COSINE_SIMILARITY_THRESHOLD = 0.95;

    /*
     * Matches the keys of both map. Missing values are set to 0.
     * Runs in expected O(n) where n = features1.size() + features2.size() assuming both maps are
     * implemented as hash maps. If any map is implemented as a tree map the algorithms runs in
     *  O(n*log(n)).
     */
    private static Pair<List<Double>, List<Double>> featureMapToContentVectors(
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

    protected abstract int getNumberOfComponent();

    protected abstract Map<String, Integer> getFeatureMap();

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof StateSkeleton<?>)) {
            return false;
        } else {
            final StateSkeleton<?> o = (StateSkeleton<?>) other;
            if (!this.getActions().equals(o.getActions())
                    || this.getNumberOfComponent() != o.getNumberOfComponent()) {
                return false;
            } else {
                final Pair<List<Double>, List<Double>> contentVectors = featureMapToContentVectors(
                        this.getFeatureMap(), o.getFeatureMap());
                return MathUtils.cosineSimilarity(contentVectors.first, contentVectors.second)
                        > COSINE_SIMILARITY_THRESHOLD;
            }
        }
    }

    @Override
    public final int hashCode() {
        // This hash function is correct, because equal states imply equal actions and equal number
        // of components. These in turn imply a equal hash value.
        //
        // The hash function does not use the feature map because, different features maps could
        // still imply equality if the two maps are similar enough (according to cosine similarity).
        final int prime = 31;
        int hash = Long.hashCode(getActions().stream().mapToInt(Object::hashCode).sum());
        hash = prime * hash + Integer.hashCode(getNumberOfComponent());
        return hash;
    }

}
