package org.mate.commons.utils;

public class Optional<T> {
    private final T value;
    private final boolean hasValue;
    private static final Optional<?> NONE = new Optional();

    private Optional() {
        value = null;
        hasValue = false;
    }

    private Optional(T value) {
        this.value = value;
        hasValue = true;
    }

    public T getValue() {
        if (hasValue) {
            return value;
        } else {
            throw new IllegalStateException("Cannot get value from Optional.NONE");
        }
    }

    public static <T> Optional<T> some(T value) {
        return new Optional<>(value);
    }

    public boolean hasValue() {
        return hasValue;
    }

    public static<T> Optional<T> none() {
        @SuppressWarnings("unchecked")
        Optional<T> t = (Optional<T>) NONE;
        return t;
    }
}
