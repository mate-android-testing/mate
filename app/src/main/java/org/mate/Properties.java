package org.mate;

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

    public static int MAX_NUM_TCS = 2;// 10;

    public static int MAX_NUM_EVENTS = 5;

    public static float NOVELTY_THRESHOLD = 0; // if 0 any point will be added to the archive

    public static int K_VALUE = 2;

    public static int ARCHIVE_SIZE = 10;

    public static int EVO_ITERATIONS_NUMBER = 10;//10;

    public static double RANK_BIAS = 1.7;//10;

    public static Long RANDOM_SEED = null;

    public static double GREEDY_EPSILON = 0.7;//10;

    /**
     * Coverage related
     */

    public static final boolean STORE_COVERAGE = true;
}
