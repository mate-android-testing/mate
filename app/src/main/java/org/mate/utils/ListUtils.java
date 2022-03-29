package org.mate.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility functions for arbitrary {@link List}s.
 */
public class ListUtils {

    private ListUtils() {}

    /**
     * Returns element at given index. Wraps if index is out of bounds. Works for positive and
     * negative integers.
     *
     * @param list List to get element from
     * @param ind index of target element; can be out of bounds
     * @param <T> type of the list
     * @return element of the List at target index
     */
    public static <T> T wrappedGet(List<T> list, int ind) {
        if (ind >= 0) {
            ind = ind % list.size();
        } else {
            ind = list.size() - (~ind % list.size()) - 1;
        }
        return list.get(ind);
    }

    /**
     * Retrieves the positions (indices) of the maxima in the list.
     *
     * @param list The input list.
     * @param <T> The generic list element type.
     * @return Returns the indices of maxima in the list.
     */
    public static <T extends Comparable<T>> List<Integer> getMaximaPositions(List<T> list) {

        assert !list.isEmpty() : "Can't get maxima from empty list!";

        List<Integer> maximaPositions = new ArrayList<>();

        // TODO: make more efficient
        T maxValue = Collections.max(list);

        for (int i = 0; i < list.size(); i++) {
            T element = list.get(i);
            if (element.equals(maxValue)) {
                maximaPositions.add(i);
            }
        }

        return maximaPositions;
    }

    /**
     * Converts the given list to a {@link Set}.
     *
     * @param list The list to be converted.
     * @param <T> The type of the list elements.
     * @return Returns the converted set.
     */
    public static <T> Set<T> toSet(List<T> list) {
        return new HashSet<>(list);
    }
}
