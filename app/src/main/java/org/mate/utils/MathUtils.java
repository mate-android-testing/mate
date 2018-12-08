package org.mate.utils;

public class MathUtils {
    public static final double EPS = 1e-10;

    public static boolean isEpsEq(double a, double b) {
        return Math.abs(a - b) < EPS;
    }

    public static boolean isEpsEq(double a) {
        return Math.abs(a) < EPS;
    }
}
