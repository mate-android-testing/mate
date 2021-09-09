package org.mate.utils;

import java.util.List;

import static java.lang.Math.sqrt;

public class MathUtils {
    public static final double EPS = 1e-10;

    public static boolean isEpsEq(double a, double b) {
        return Math.abs(a - b) < EPS;
    }

    public static boolean isEpsEq(double a) {
        return Math.abs(a) < EPS;
    }

    public static double dotProduct(final List<Double> vector1, final List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Both vectors need to have the same size.");
        } else {
            double sum = 0.0;
            final int bound = vector1.size();
            for (int i = 0; i < bound; i++) {
                sum += vector1.get(i) * vector2.get(i);
            }
            return sum;
        }
    }

    public static double norm(final List<Double> vector) {
        double sum = 0.0;
        for (final Double x : vector) {
            double v = x * x;
            sum += v;
        }
        return sqrt(sum);
    }

    public static double cosineSimilarity(final List<Double> vector1, final List<Double> vector2) {
        return dotProduct(vector1, vector2) / (norm(vector1) * norm(vector2));
    }
}
