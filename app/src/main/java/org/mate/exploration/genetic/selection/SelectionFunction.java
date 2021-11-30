package org.mate.exploration.genetic.selection;

/**
 * The list of available selection functions for genetic algorithms.
 */
public enum SelectionFunction {

    RANDOM_SELECTION,
    FITNESS_SELECTION,
    FITNESS_PROPORTIONATE_SELECTION,
    RANK_SELECTION,
    TOURNAMENT_SELECTION,
    CROWDED_TOURNAMENT_SELECTION,
    NOVELTY_RANK_SELECTION;
}
