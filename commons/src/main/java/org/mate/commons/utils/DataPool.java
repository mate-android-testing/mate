package org.mate.commons.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Provides for the most data types pre-defined values.
 */
public final class DataPool {

    // int/Integer data type
    public static final Integer[] INTEGER_ARRAY = {0,1,-1, Integer.MIN_VALUE, Integer.MAX_VALUE};
    public static final Integer[] INTEGER_ARRAY_WITH_NULL = {0,1,-1, Integer.MIN_VALUE, Integer.MAX_VALUE, null};
    public static final List<Integer> INTEGER_LIST = Arrays.asList(INTEGER_ARRAY);
    public static final List<Integer> INTEGER_LIST_WITH_NULL = Arrays.asList(INTEGER_ARRAY_WITH_NULL);

    // float
    public static final Float[] FLOAT_ARRAY = {0.0f, 1f, -1f, Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE};
    public static final Float[] FLOAT_ARRAY_WITH_NULL = {0.0f, 1f, -1f, Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE, null};
    public static final List<Float> FLOAT_LIST = Arrays.asList(FLOAT_ARRAY);
    public static final List<Float> FLOAT_LIST_WITH_NULL = Arrays.asList(FLOAT_ARRAY_WITH_NULL);

    // double
    public static final Double[] DOUBLE_ARRAY = {0.0d, 1.0d, -1.0d, Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE};
    public static final Double[] DOUBLE_ARRAY_WITH_NULL = {0.0d, 1.0d, -1.0d, Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE, null};
    public static final List<Double> DOUBLE_LIST = Arrays.asList(DOUBLE_ARRAY);
    public static final List<Double> DOUBLE_LIST_WITH_NULL = Arrays.asList(DOUBLE_ARRAY_WITH_NULL);

    // long
    public static final Long[] LONG_ARRAY = {0L, 1L, -1L, Long.MIN_VALUE, Long.MAX_VALUE};
    public static final Long[] LONG_ARRAY_WITH_NULL = {0L, 1L, -1L, Long.MIN_VALUE, Long.MAX_VALUE, null};
    public static final List<Long> LONG_LIST = Arrays.asList(LONG_ARRAY);
    public static final List<Long> LONG_LIST_WITH_NULL = Arrays.asList(LONG_ARRAY_WITH_NULL);

    // short
    public static final Short[] SHORT_ARRAY = {0, 1, -1, Short.MIN_VALUE, Short.MAX_VALUE};
    public static final Short[] SHORT_ARRAY_WITH_NULL = {0, 1, -1, Short.MIN_VALUE, Short.MAX_VALUE, null};
    public static final List<Short> SHORT_LIST = Arrays.asList(SHORT_ARRAY);
    public static final List<Short> SHORT_LIST_WITH_NULL = Arrays.asList(SHORT_ARRAY_WITH_NULL);

    // byte
    public static final Byte[] BYTE_ARRAY = {0, 1, -1, Byte.MIN_VALUE, Byte.MAX_VALUE};
    public static final Byte[] BYTE_ARRAY_WITH_NULL = {0, 1, -1, Byte.MIN_VALUE, Byte.MAX_VALUE, null};
    public static final List<Byte> BYTE_LIST = Arrays.asList(BYTE_ARRAY);
    public static final List<Byte> BYTE_LIST_WITH_NULL = Arrays.asList(BYTE_ARRAY_WITH_NULL);

    // boolean
    public static final Boolean[] BOOLEAN_ARRAY = {false, true};
    public static final Boolean[] BOOLEAN_ARRAY_WITH_NULL = {false, true, null};
    public static final List<Boolean> BOOLEAN_LIST = Arrays.asList(BOOLEAN_ARRAY);
    public static final List<Boolean> BOOLEAN_LIST_WITH_NULL = Arrays.asList(BOOLEAN_ARRAY_WITH_NULL);

    // char
    public static final Character[] CHAR_ARRAY = {'a', 'b', 'c', 'd', 'e', 'z'};
    public static final Character[] CHAR_ARRAY_WITH_NULL = {'a', 'b', 'c', 'd', 'e', 'z', null};
    public static final List<Character> CHAR_LIST = Arrays.asList(CHAR_ARRAY);
    public static final List<Character> CHAR_LIST_WITH_NULL = Arrays.asList(CHAR_ARRAY_WITH_NULL);


    // string
    public static final String[] STRING_ARRAY = {"android", "is", "stupid"};
    public static final String[] STRING_ARRAY_WITH_NULL = {"android", "is", "stupid", null};
    public static final List<String> STRING_LIST = Arrays.asList(STRING_ARRAY);
    public static final List<String> STRING_LIST_WITH_NULL = Arrays.asList(STRING_ARRAY_WITH_NULL);




}
