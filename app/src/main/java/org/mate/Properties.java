package org.mate;

import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.exploration.genetic.util.ge.AndroidListBasedBiasedMapping;
import org.mate.graph.GraphType;
import org.mate.state.executables.StateEquivalenceLevel;
import org.mate.utils.GenericParser;
import org.mate.utils.Objective;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.testcase.OptimisationStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Properties {

    // the timeout in minutes
    public static int TIMEOUT() { return propertyOr(5); }

    public static int ANT_GENERATION() {
        return propertyOr(5);
    }
    public static int ANT_NUMBER() {
        return propertyOr(5);
    }

    public static int ANT_LENGTH() {
        return propertyOr(8);
    }

    public static float EVAPORATION_RATE() {
        return propertyOr(0.1f);
    }

    public static float INITIALIZATION_PHEROMONE() {
        return propertyOr(5.0f);
    }

    public static float PROBABILITY_SELECT_BEST_ACTION() {
        return propertyOr(0.5f);
    }

    public static float BEST_ANT() {
        return propertyOr(3);
    }

    /**
     * Defines the equivalence check for two screen states.
     *
     * @return Returns the selected state equivalence check.
     */
    public static StateEquivalenceLevel STATE_EQUIVALENCE_LEVEL() {
        return propertyOr(StateEquivalenceLevel.WIDGET);
    }

    /**
     * Whether the surrogate model should be used or not.
     *
     * @return Returns {@code true} if the surrogate model should be used, otherwise {@code false}
     *          is returned.
     */
    public static boolean SURROGATE_MODEL() { return propertyOr(false); }

    /*
    * Intent fuzzing related properties.
     */
    public static float RELATIVE_INTENT_AMOUNT() { return propertyOr(0.0f); }

    /**
     * The optimisation strategy that should be applied.
     *
     * @return Returns the applied optimisation strategy.
     */
    public static OptimisationStrategy OPTIMISATION_STRATEGY() {
        return propertyOr(OptimisationStrategy.NO_OPTIMISATION);
    }

    /*
    * Whether to record stats about test cases or not.
     */
    public static boolean RECORD_TEST_CASE_STATS() {
        return propertyOr(false);
    }

    /*
    * Whether to serialize a test case. Default: off.
     */
    public static boolean RECORD_TEST_CASE() {
        return propertyOr(false);
    }

    /*
     * Misc properties
     */
    public static Long RANDOM_SEED() {
        return propertyOr(null);
    }

    /*
     * Genetic Algorithm properties
     */
    public static int POPULATION_SIZE() {
        return propertyOr(20);
    }

    public static int BIG_POPULATION_SIZE() { return propertyOr(40); }

    public static int NUMBER_TESTCASES() {
        return propertyOr(2);
    }

    public static int MAX_NUMBER_EVENTS() {
        return propertyOr(50);
    }

    public static double P_CROSSOVER() {
        return propertyOr(0.7);
    }

    public static double P_MUTATE() {
        return propertyOr(0.3);
    }

    public static double P_SAMPLE_RANDOM() {
        return propertyOr(0.5);
    }

    public static double P_FOCUSED_SEARCH_START() {
        return propertyOr(0.5);
    }

    public static int EVO_ITERATIONS_NUMBER() {
        return propertyOr(10);
    }

    public static int MUTATION_RATE() { return propertyOr(1); }

    public static int TOURNAMENT_SIZE() { return propertyOr(2); }

    public static int DEFAULT_SELECTION_SIZE() { return propertyOr(2); }

    public static FitnessFunction FITNESS_FUNCTION() {
        return propertyOr(null);
    }

    /**
     * Determines a list of fitness functions from the properties file.
     *
     * @return a list of fitness functions.
     */
    public static FitnessFunction[] FITNESS_FUNCTIONS() {
        return propertyOr(null);
    }

    /**
     * In the context of GE, we have two fitness functions. While {@link #FITNESS_FUNCTION()}
     * provides the mapping from geno to phenotype, this property specifies the core fitness function.
     *
     * @return Returns the core fitness function used in the context of GE.
     */
    public static FitnessFunction GE_FITNESS_FUNCTION() { return propertyOr(null); }

    public static SelectionFunction SELECTION_FUNCTION() { return propertyOr(null); }

    public static MutationFunction MUTATION_FUNCTION() {
        return propertyOr(null);
    }

    public static CrossOverFunction CROSSOVER_FUNCTION() { return propertyOr(null); }

    public static TerminationCondition TERMINATION_CONDITION() { return propertyOr(null); }

    public static ChromosomeFactory CHROMOSOME_FACTORY() { return propertyOr(null); }

    public static Algorithm ALGORITHM() { return propertyOr(null); }

    /*
     * Begin Greybox Fuzzing properties
     */

    /**
     * The coverage type that should steer the exploration.
     *
     * @return Returns the coverage type that steers the exploration, defaults to activity coverage.
     */
    public static Coverage GREY_BOX_COVERAGE_CRITERION() {
        return propertyOr(Coverage.ACTIVITY_COVERAGE);
    }

    /**
     * The initial size of the seed corpus S.
     *
     * @return Returns the initial size of the seed corpus.
     */
    public static int SEED_CORPUS_SIZE() { return propertyOr(10); }

    /**
     * The maximal assignable energy p.
     *
     * @return Returns the maximal assignable energy.
     */
    public static int MAX_ENERGY() { return propertyOr(10); }

    /*
     * End Greybox Fuzzing properties
     */

    /**
     * Indicates which objective should be used for the multi-/many-objective
     * search, e.g. branches or lines.
     *
     * @return Returns the objective or {@code null} if none was specified.
     */
    public static Objective OBJECTIVE() {
        return propertyOr(null);
    }

    /*
     * Coverage properties
     */
    public static Coverage COVERAGE() {
        return propertyOr(Coverage.NO_COVERAGE);
    }

    /*
    * Begin Graph properties
     */

    // the graph type, e.g. CFG or SGD
    public static GraphType GRAPH_TYPE() {
        return propertyOr(null);
    }

    // the path to the APK file
    public static String APK() {
        return propertyOr(null);
    }

    // specifies the method name when an intra CFG should be constructed
    public static String METHOD_NAME() {
        return propertyOr(null);
    }

    // whether basic blocks should be used or not
    public static boolean BASIC_BLOCKS() {
        return propertyOr(true);
    }

    // whether only AUT classes should be resolved
    public static boolean RESOLVE_ONLY_AUT_CLASSES() {
        return propertyOr(true);
    }

    // whether ART classes should be excluded when constructing the graph
    public static boolean EXCLUDE_ART_CLASSES() {
        return propertyOr(true);
    }

    // how and which target vertex should be selected, e.g. a random branch vertex
    public static String TARGET() { return propertyOr("no_target"); }

    // whether to draw raw graph or 'extended' graph
    public static boolean DRAW_RAW_GRAPH() { return propertyOr(true); }

    /*
    * End Graph properties
     */

    // Primitive actions or widget based actions?
    public static boolean WIDGET_BASED_ACTIONS() {
        return propertyOr(true);
    }

    // stack trace
    public static boolean RECORD_STACK_TRACE() {
        return propertyOr(false);
    }

    /*
    * Begin GE properties
     */

    public static int GE_SEQUENCE_LENGTH() {
        return propertyOr(100);
    }

    public static int GE_TEST_CASE_ENDING_BIAS_PER_TEN_THOUSAND() {
        return propertyOr(AndroidListBasedBiasedMapping.BIAS_50_PERCENT);
    }

    public static int GE_MUTATION_COUNT() {
        return propertyOr(3);
    }

    /*
    * End GE properties
     */

    /**
     * Novelty Search - Defines the novelty threshold. A value of 0 indicates that every
     * chromosome will be added to the archive.
     *
     * @return Returns the novelty threshold T.
     */
    public static double NOVELTY_THRESHOLD() { return propertyOr(0.0); }

    /**
     * Novelty Search - Defines the maximal size of the archive.
     *
     * @return Returns the archive size L.
     */
    public static int ARCHIVE_LIMIT() { return propertyOr(10); }

    /**
     * Novelty Search - Defines the number of nearest neighbours that should be considered
     * for the novelty metric.
     *
     * @return Returns the number of nearest neighbours k.
     */
    public static int NEAREST_NEIGHBOURS() { return propertyOr(3); }

    /**
     * Controls whether quick launch is enabled or disabled.
     *
     * @return Returns {@code true} if quick launch is enabled, otherwise {@code false} is returned.
     */
    public static boolean QUICK_LAUNCH() { return propertyOr(true); }

    /*
     * Begin AimDroid properties
     */

    /**
     * The epsilon used in the epsilon greedy learning policy.
     *
     * @return Returns the epsilon used in the learning policy.
     */
    public static double EPSILON() { return propertyOr(0.1d);}

    /**
     * The alpha used in the SARSA equation.
     *
     * @return Returns the alpha used in the SARSA equation.
     */
    public static double ALPHA() { return propertyOr(0.8d); }

    /**
     * The gamma used in the SARSA equation.
     *
     * @return Returns the gamma used in the SARSA equation.
     */
    public static double GAMMA() { return propertyOr(0.8d); }

    /**
     * The minL constant used in the bound method (the minimal number of actions).
     *
     * @return Returns the minL constant.
     */
    public static int MIN_L() { return propertyOr(20); }

    /**
     * The maxL constant used in the bound method (the maximal number of actions).
     *
     * @return Returns the maxL constant.
     */
    public static int MAX_L() { return propertyOr(50); }

    /*
     * End AimDroid properties
     */

    /*
     * Begin AutoBlackTest properties
     */

    /**
     * The epsilon used in the epsilon greedy learning policy.
     *
     * @return Returns the epsilon used in the greedy learning policy.
     */
    public static float ABT_EPSILON() { return propertyOr(0.8f); }

    /**
     * The static discount factor used in equation (1).
     *
     * @return Returns the static discount factor.
     */
    public static float ABT_DISCOUNT_FACTOR() { return propertyOr(0.9f); }

    /**
     * The maximal number of episodes (testcases).
     *
     * @return Returns the maximal number of episodes.
     */
    public static int ABT_MAX_NUM_OF_EPISODES() { return propertyOr(100); }

    /**
     * The maximal length of an episode (a test case).
     *
     * @return Returns the maximal episode length.
     */
    public static int ABT_MAX_EPISODE_LENGTH() { return propertyOr(50); }

    /*
     * End AutoBlackTest properties
     */

    /*
    * Begin AutoDroid properties
     */

    /**
     * The probability for pressing the home button.
     *
     * @return Returns the probability for selecting the home button.
     */
    public static float P_HOME_BUTTON() { return propertyOr(0.05f); }

    /**
     * The initial q-value for a new action.
     *
     * @return Returns the initial q-value for a new action.
     */
    public static float INITIAL_Q_VALUE() { return propertyOr(500f); }

    /**
     * The maximal number of episodes (testcases).
     *
     * @return Returns the maximal number of episodes.
     */
    public static int MAX_NUM_OF_EPISODES() { return propertyOr(100); }

    /**
     * The maximal length of an episode (a test case).
     *
     * @return Returns the maximal episode length.
     */
    public static int MAX_EPISODE_LENGTH() { return propertyOr(50); }

    /*
     * End AutoDroid properties
     */

    /**
     * Looks up the value of the property in the Properties object stored in the Registry using the
     * name of the caller method as the key of the property. If no property with that key is stored
     * the given default value will be returned.
     * @param defaultValue Default value of the property
     * @param <T> Type of the property
     * @return Value of the property if stored otherwise the given default value
     */
    @SuppressWarnings("unchecked")
    private static <T> T propertyOr(T defaultValue) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callerName = stackTrace[3].getMethodName().toLowerCase();
        Properties propertiesInstance = Registry.getProperties();
        if (propertiesInstance.store.containsKey(callerName)) {
            return ((T) propertiesInstance.store.get(callerName));
        }
        return defaultValue;
    }

    private final Map<String, Object> store;

    public Properties(Map<String, String> properties) {
        store = new HashMap<>();
        readProperties(properties);
    }

    /**
     * Overrides a given property.
     *
     * @param key The property name.
     * @param value The value for the property.
     */
    // TODO: Remove once all properties are enforced via the mate.properties file!
    public static void setProperty(String key, Object value) {
        Registry.getProperties().store.put(key, value);
    }

    private void readProperties(Map<String, String> properties) {
        Map<String, Class<?>> propertiesInfo = new HashMap<>();

        for (Method declaredMethod : Properties.class.getDeclaredMethods()) {
            if (Modifier.isPublic(declaredMethod.getModifiers())
                    && Modifier.isStatic(declaredMethod.getModifiers())) {
                propertiesInfo.put(
                        declaredMethod.getName().toLowerCase(),
                        declaredMethod.getReturnType());
            }
        }

        for (Map.Entry<String, String> property : properties.entrySet()) {
            String key = property.getKey().toLowerCase();
            if (propertiesInfo.containsKey(key)) {
                try {
                    Object parsedObj = GenericParser.parse(
                            propertiesInfo.get(key),
                            property.getValue());
                    store.put(key, parsedObj);
                } catch (Exception e) {
                    MATE.log_acc(
                            "Failure while trying to parse \""
                                    + property.getValue()
                                    + "\" as instance of class "
                                    + propertiesInfo.get(key).getCanonicalName());
                }
            } else {
                MATE.log_acc("Unknown property with key: " + property.getKey());
            }
        }
    }
}
