package org.mate.utils;

import java.util.List;

public class ListUtils {
    private ListUtils() {}

    /**
     * Returns element at given index. Wraps if index is out of bounds. Works for positive and
     * negative integers
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
}
