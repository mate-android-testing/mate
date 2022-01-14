package org.mate.exploration.genetic.mutation;

/**
 * The set of supported mutation functions in genetic algorithms.
 */
public enum MutationFunction {

    TEST_CASE_CUT_POINT_MUTATION,
    TEST_SUITE_CUT_POINT_MUTATION,
    SHUFFLE_MUTATION,
    PRIMITIVE_SHUFFLE_MUTATION,
    SAPIENZ_MUTATION,
    INTEGER_SEQUENCE_POINT_MUTATION,
    INTEGER_SEQUENCE_LENGTH_MUTATION;
}
