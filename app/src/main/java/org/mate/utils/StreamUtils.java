package org.mate.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class StreamUtils {

    private StreamUtils() {
        throw new UnsupportedOperationException("Cannot instantiate class StreamUtils!");
    }

    public static <T> Predicate<T> distinctByKey(final Function<? super T, Object> keyExtractor) {
        // Taken from:  https://stackoverflow.com/a/27872086
        final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
