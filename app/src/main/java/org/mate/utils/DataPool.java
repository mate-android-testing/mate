package org.mate.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Provides for the most data types pre-defined values.
 */
public final class DataPool {

    // int/Integer data type
    public static final Integer[] INTEGER_ARRAY = {0,1,-1, Integer.MIN_VALUE, Integer.MAX_VALUE, null};
    public static final Integer[] INTEGER_ARRAY_WITH_NULL = {0,1,-1, Integer.MIN_VALUE, Integer.MAX_VALUE, null};
    public static final List<Integer> INTEGER_LIST = Arrays.asList(INTEGER_ARRAY);
    public static final List<Integer> INTEGER_LIST_WITH_NULL = Arrays.asList(INTEGER_ARRAY_WITH_NULL);

    // float
    public static final Float[] FLOAT_ARRAY = {0.0f, 1f, -1f, Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE};
    public static final Float[] FLOAT_ARRAY_WITH_NULL = {0.0f, 1f, -1f, Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE, null};
    public static final List<Float> FLOAT_LIST = Arrays.asList(FLOAT_ARRAY);
    public static final List<Float> FLOAT_LIST_WITH_NULL = Arrays.asList(FLOAT_ARRAY_WITH_NULL);


    // string
    public static final String[] STRING_ARRAY = {"android", "is", "stupid"};
    public static final String[] STRING_ARRAY_WITH_NULL = {"android", "is", "stupid", null};
    public static final List<String> STRING_LIST = Arrays.asList(STRING_ARRAY);
    public static final List<String> STRING_LIST_WITH_NULL = Arrays.asList(STRING_ARRAY_WITH_NULL);




}
