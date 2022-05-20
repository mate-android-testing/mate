package org.mate.commons.utils;

import java.util.Set;

/**
 * Utility functions for {@link java.util.Set}s.
 */
public final class SetUtils {

    private SetUtils() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    /**
     * Retrieves the element specified by {@param index} in a given set.
     *
     * @param set The set from which an element is requested.
     * @param index The position of the desired element in the given set.
     * @return Returns the element located at the given index from a set.
     */
    public static <T> T getElementAtIndex(Set<T> set, int index) {

        int counter = 0;

        for (T element : set) {
            if (counter == index) {
                return element;
            } else {
                counter++;
            }
        }
        throw new IllegalStateException("Couldn't retrieve element at position " + index
                + " from set with size: " + set.size());
    }
}
