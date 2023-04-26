package org.mate.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides a set of utility functions for {@link java.util.stream.Stream} operations.
 */
public final class StreamUtils {

    private StreamUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class StreamUtils!");
    }

    /**
     * Provides a predicate that checks whether the given key (supplied by a function) is distinct.
     *
     * @param keyExtractor The key supplier function.
     * @param <T> The type of the key.
     * @return Returns {@code true} if the key is distinct, otherwise {@code false} is returned.
     */
    public static <T> Predicate<T> distinctByKey(final Function<? super T, Object> keyExtractor) {
        // Taken from:  https://stackoverflow.com/a/27872086
        final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
