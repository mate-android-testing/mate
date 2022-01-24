package org.mate.utils;

/**
 * The objectives used for MIO/MOSA or novelty search.
 */
public enum Objective {
    LINES,
    BRANCHES, // in combination with branch distance (requires graph)
    BLOCKS,
    METHODS;
}
