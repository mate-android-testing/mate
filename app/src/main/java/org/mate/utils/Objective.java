package org.mate.utils;

/**
 * The objectives used for MIO/MOSA.
 */
public enum Objective {
    LINES,
    BRANCHES, // in combination with branch distance (requires graph)
    BLOCKS;
}
