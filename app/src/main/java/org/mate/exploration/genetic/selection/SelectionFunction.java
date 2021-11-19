package org.mate.exploration.genetic.selection;

/**
 * The set of available selection functions.
 */
public enum SelectionFunction {

    RANDOM_SELECTION,
    FITNESS_SELECTION,
    FITNESS_PROPORTIONATE_SELECTION,
    RANK_SELECTION,
    TOURNAMENT_SELECTION,
    CROWDED_TOURNAMENT_SELECTION;
    NOVELTY_RANK_SELECTION;
}
