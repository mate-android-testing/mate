package org.mate.utils;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link Comparator} for {@link Float} values.
 */
public class FloatComparator implements Comparator<Float> {

    /**
     * Compares two float values.
     *
     * @param o1 The first float value.
     * @param o2 The second float value.
     * @return Returns the comparison result.
     */
    @Override
    public int compare(Float o1, Float o2) {
        return Float.compare(o1, o2);
    }

    /**
     * Provides a comparator for float values.
     *
     * @param keyExtractor A mapping function from a generic type to float.
     * @param <T> The generic type of the mapping function.
     * @return Returns a float comparator.
     */
    public static <T> Comparator<T> comparingFloat(final Function<T, Float> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable)
                (c1, c2) -> Float.compare(keyExtractor.apply(c1), keyExtractor.apply(c2));
    }
}
