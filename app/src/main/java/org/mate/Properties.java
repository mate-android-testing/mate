package org.mate;

import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.graph.GraphType;
import org.mate.utils.GenericParser;
import org.mate.utils.Objective;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.testcase.OptimisationStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
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


    /*
    * Intent fuzzing related properties.
     */
    public static float RELATIVE_INTENT_AMOUNT() { return propertyOr(1.0f); }

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

    /**
     *  Added by vin on 24/05/2018
     */
    // if 0 any point will be added to the archive
    public static float NOVELTY_THRESHOLD() {
        return propertyOr(0);
    }

    public static int K_VALUE() {
        return propertyOr(2);
    }

    //10;
    public static double RANK_BIAS() {
        return propertyOr(1.7);
    }

    public static int ARCHIVE_SIZE() {
        return propertyOr(10);
    }

    //10;
    public static double GREEDY_EPSILON() {
        return propertyOr(0.7);
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
    // for mutation functions that apply multiple mutations based on the given probability
    public static double P_INNER_MUTATE() {
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

    public static FitnessFunction FITNESS_FUNCTION() {
        return propertyOr(null);
    }

    public static SelectionFunction SELECTION_FUNCTION() { return propertyOr(null); }

    public static MutationFunction MUTATION_FUNCTION() {
        return propertyOr(null);
    }

    public static CrossOverFunction CROSSOVER_FUNCTION() { return propertyOr(null); }

    public static TerminationCondition TERMINATION_CONDITION() { return propertyOr(null); }

    public static ChromosomeFactory CHROMOSOME_FACTORY() { return propertyOr(null); }

    /**
     * Indicates which objective should be used for the multi-/many-objective
     * search, i.e. branches or lines.
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

    /**
     * Added by stockinger on 28/09/2020
     */
    public static int BIG_POPULATION_SIZE() { return propertyOr(100); }

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
                    MATE.log(
                            "Failure while trying to parse \""
                                    + property.getValue()
                                    + "\" as instance of class "
                                    + propertiesInfo.get(key).getCanonicalName());
                }
            } else {
                MATE.log("Unknown property with key: " + property.getKey());
            }
        }
    }
}
