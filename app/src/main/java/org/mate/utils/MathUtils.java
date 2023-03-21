package org.mate.utils;

import java.util.List;

import static java.lang.Math.sqrt;

/**
 * Provides a set of mathematical utility functions.
 */
public final class MathUtils {

    private MathUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * A small epsilon.
     */
    public static final double EPS = 1e-10;

    @SuppressWarnings("unused")
    public static boolean isEpsEq(double a, double b) {
        return Math.abs(a - b) < EPS;
    }

    @SuppressWarnings("unused")
    public static boolean isEpsEq(double a) {
        return Math.abs(a) < EPS;
    }

    /**
     * Computes the dot product for the given two vectors.
     *
     * @param vector1 The first vector.
     * @param vector2 The second vector.
     * @return Returns the dot product for the given two vectors.
     */
    public static double dotProduct(final List<Double> vector1, final List<Double> vector2) {

        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Both vectors need to have the same size!");
        } else {
            double dotProduct = 0.0;
            final int bound = vector1.size();
            for (int i = 0; i < bound; i++) {
                dotProduct += vector1.get(i) * vector2.get(i);
            }
            return dotProduct;
        }
    }

    /**
     * Computes the euclidean norm for the given vector.
     *
     * @param vector The given vector.
     * @return Returns the euclidean norm for the given vector.
     */
    public static double norm(final List<Double> vector) {
        double sum = 0.0;
        for (final Double x : vector) {
            double v = x * x;
            sum += v;
        }
        return sqrt(sum);
    }

    /**
     * Computes the cosine similarity between the given two vectors.
     *
     * @param vector1 The first vector.
     * @param vector2 The second vector.
     * @return Returns the cosine similarity between the given two vectors.
     */
    public static double cosineSimilarity(final List<Double> vector1, final List<Double> vector2) {
        return dotProduct(vector1, vector2) / (norm(vector1) * norm(vector2));
    }
}
