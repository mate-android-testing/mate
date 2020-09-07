package org.mate.utils;

import org.mate.MATE;
import org.mate.Registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

public class Randomness {
    public static Random getRnd() {
        return Registry.getRandom();
    }

    public static <T> T randomElement(List<T> list) {
        return list.get(Registry.getRandom().nextInt(list.size()));
    }

    public static <T> int randomIndex(List<T> list) {
        return Registry.getRandom().nextInt(list.size());
    }

    /**
     * Randomly retrieves an element from a given set.
     *
     * @param set The input set.
     * @param <T> The element type.
     * @return Returns a random element from a given set.
     */
    public static <T> T randomElement(Set<T> set) {
        int index = new Random().nextInt(set.size());
        Iterator<T> iter = set.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }

        return iter.next();
    }

    /**
     * Selects with a probability of 0.5 either a random element
     * from a given set or {@code null}.
     *
     * @param set The input set.
     * @param <T> The element type.
     * @return Returns a random element from a given set or {@code null}.
     */
    public static <T> T randomElementOrNull(Set<T> set) {

        double random = Math.random();

        if (random < 0.5) {
            return randomElement(set);
        } else {
            return null;
        }
    }

    /**
     * Randomly retrieves {@param count} elements from a given set.
     *
     * @param set The input set.
     * @param count The number of elements that should be retrieved.
     * @param <T> The element type.
     * @return Returns a list with {@param count} random elements from a given set.
     */
    public static <T> List<T> randomElements(Set<T> set, int count) {

        List<T> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(randomElementOrNull(set));
        }
        return result;
    }

    public static int getInRangeStd(int range) {
        return getInRangeStd(range, 2.0/15.0 * range);
    }

    public static int getInRangeStd(int range, double std) {
        int x;
        do {
            x = (int) Math.round(Registry.getRandom().nextGaussian() * std + (range - 1) / 2.0);
        } while (x < 0 || x >= range);
        return x;
    }

    public static <T> void shuffleList(List<T> list) {
        List<T> pickList = new ArrayList<>(list);
        list.clear();
        int size = pickList.size();

        for (int i = 0; i < size; i++) {
            int choice = randomIndex(pickList);
            list.add(pickList.get(choice));
            pickList.remove(choice);
        }
    }

    public static List<Integer> getRandomIntegers(int count, int bound) {

        // API 28: IntStream.generate(() -> new Random().nextInt(100)).limit(100).toArray();
        Random random = new Random();
        List<Integer> result = new ArrayList<>(count);

        for (int i = 0; i < count; i++)
        {
            result.add(random.nextInt(bound));
        }

        return result;
    }

    public static List<Integer> getRandomIntegersWithNull(int count, int bound) {

        // API 28: IntStream.generate(() -> new Random().nextInt(100)).limit(100).toArray();
        Random random = new Random();
        List<Integer> result = new ArrayList<>(count);

        for (int i = 0; i < count; i++)
        {
            result.add(random.nextInt(bound));

            // insert with some probability null values between the other values
            double rnd = Math.random();

            if (rnd > 0.5) {
                result.add(null);
            }
        }

        return result;
    }

    public static int[] getRandomIntArray(int count, int bound) {

        Random random = new Random();
        int[] result = new int[count];

        for (int i = 0; i < count; i++)
        {
            result[i] = random.nextInt(bound);
        }

        return result;
    }

    public static float[] getRandomFloatArray(int count) {

        Random random = new Random();
        float[] result = new float[count];

        for (int i = 0; i < count; i++)
        {
            result[i] = random.nextFloat();
        }

        return result;
    }

    public static double[] getRandomDoubleArray(int count) {

        Random random = new Random();
        double[] result = new double[count];

        for (int i = 0; i < count; i++)
        {
            result[i] = random.nextDouble();
        }

        return result;
    }

    public static long[] getRandomLongArray(int count) {

        Random random = new Random();
        long[] result = new long[count];

        for (int i = 0; i < count; i++)
        {
            result[i] = random.nextLong();
        }

        return result;
    }

    public static short[] getRandomShortArray(int count) {

        Random random = new Random();
        short[] result = new short[count];

        for (int i = 0; i < count; i++)
        {
            https://stackoverflow.com/a/10189329/6110448
            result[i] = (short) random.nextInt(1 << 16);
        }

        return result;
    }

    public static byte[] getRandomByteArray(int count) {

        Random random = new Random();
        byte[] result = new byte[count];
        random.nextBytes(result);
        return result;
    }

    public static boolean[] getRandomBooleanArray(int count) {

        Random random = new Random();
        boolean[] result = new boolean[count];

        for (int i = 0; i < count; i++)
        {
            result[i] = random.nextBoolean();
        }

        return result;
    }

    public static char[] getRandomCharArray(int count) {

        Random random = new Random();
        char[] result = new char[count];

        for (int i = 0; i < count; i++)
        {
            // a-z: https://stackoverflow.com/a/2627801/6110448
            result[i] = (char)(random.nextInt(26) + 'a');
        }

        return result;
    }
}
