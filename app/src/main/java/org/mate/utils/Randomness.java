package org.mate.utils;

import org.mate.Properties;

import java.util.List;
import java.util.Random;

public class Randomness {
    private static Random rnd;

    static {
        if (Properties.RANDOM_SEED != null) {
            rnd = new MersenneTwister(Properties.RANDOM_SEED);
        } else {
            rnd = new MersenneTwister();
        }
    }

    public static void setRnd(Random rnd) {
        Randomness.rnd = rnd;
    }

    public static Random getRnd() {
        return Randomness.rnd;
    }

    public static <T> T randomElement(List<T> list) {
        return list.get(rnd.nextInt(list.size()));
    }
}
