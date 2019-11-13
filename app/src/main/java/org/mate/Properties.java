package org.mate;

import org.mate.utils.Coverage;

/**
 * Created by geyan on 27/06/2017.
 */

public class Properties {

    public static int ANT_GENERATION = 10;

    public static int ANT_NUMBER = 5;

    public static int ANT_LENGTH = 8;

    public static float EVAPORATION_RATE = 0.1f;

    public static float INITIALIZATION_PHEROMONE = 5.0f;

    public static float PROBABILITY_SELECT_BEST_ACTION = 0.5f;

    public static float BEST_ANT = 3;


    /**
     *  Added by vin on 24/05/2018
     */

    public static float NOVELTY_THRESHOLD = 0; // if 0 any point will be added to the archive

    public static int K_VALUE = 2;

    public static double RANK_BIAS = 1.7;//10;

    public static int ARCHIVE_SIZE = 10;


    public static double GREEDY_EPSILON = 0.7;//10;

    /*
     * Misc properties
     */

    public static Long RANDOM_SEED = null;

    /*
     * Genetic Algorithm properties
     */

    public static int POPULATION_SIZE = 20;

    public static int NUMBER_TESTCASES = 2;

    public static int MAX_NUMBER_EVENTS = 5;

    public static double P_CROSSOVER = 0.7;

    public static double P_MUTATE = 0.3;

    public static double P_INNER_MUTATE = 0.3; // for mutation functions that apply multiple mutations based on the given probability

    public static double P_SAMPLE_RANDOM = 0.5;

    public static double P_FOCUSED_SEARCH_START = 0.5;

    public static int EVO_ITERATIONS_NUMBER = 10;

    /*
     * Coverage properties
     */

    public static boolean STORE_COVERAGE = true;

    // use LINE_COVERAGE as default
    public static Coverage COVERAGE = Coverage.BRANCH_COVERAGE;


    // Primitive actions or widget based actions?
    public static boolean WIDGET_BASED_ACTIONS = true;
}
