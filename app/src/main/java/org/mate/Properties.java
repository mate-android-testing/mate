package org.mate;

import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.exploration.genetic.util.ge.AndroidListBasedBiasedMapping;
import org.mate.exploration.genetic.util.ge.GEMappingFunction;
import org.mate.graph.DrawType;
import org.mate.graph.GraphType;
import org.mate.model.fsm.sosm.novelty.NoveltyEstimator;
import org.mate.model.util.DotConverter;
import org.mate.state.equivalence.StateEquivalenceLevel;
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
    public static int TIMEOUT() {
        return propertyOr(5);
    }

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
     * Returns the specified cosine similarity coefficient.
     *
     * @return Returns the cosine similarity threshold.
     */
    public static float COSINE_SIMILARITY_THRESHOLD() {
        return propertyOr(0.95f);
    }

    /**
     * Whether the surrogate model should be used or not.
     *
     * @return Returns {@code true} if the surrogate model should be used, otherwise {@code false}
     *         is returned.
     */
    public static boolean SURROGATE_MODEL() {
        return propertyOr(false);
    }

    /*
     * Intent fuzzing related properties.
     */
    public static float RELATIVE_INTENT_AMOUNT() {
        return propertyOr(0.0f);
    }

    /**
     * The relative amount of motif actions in contrast to regular ui actions.
     *
     * @return Returns the relative amount of motif actions.
     */
    public static float RELATIVE_MOTIF_ACTION_AMOUNT() {
        return propertyOr(0.0f);
    }

    /**
     * The optimisation strategy that should be applied.
     *
     * @return Returns the applied optimisation strategy.
     */
    public static OptimisationStrategy OPTIMISATION_STRATEGY() {
        return propertyOr(OptimisationStrategy.NO_OPTIMISATION);
    }

    /**
     * Whether additional stats about a test case should be recorded/logged.
     *
     * @return Returns {@code true} if additional stats should be recorded/logged, otherwise
     *         {@code false} is returned.
     */
    public static boolean RECORD_TEST_CASE_STATS() {
        return propertyOr(false);
    }

    /**
     * Whether a {@link org.mate.model.TestCase} should be serialized to a XML representation
     * using the XStream library.
     *
     * @return Returns {@code true} if a test case should be serialized to XML, otherwise
     *         {@code false} is returned.
     */
    public static boolean RECORD_TEST_CASE() {
        return propertyOr(false);
    }

    /**
     * Whether a {@link org.mate.model.TestCase} should be converted to an espresso test.
     *
     * @return Returns {@code true} if a test case should be converted to an espresso test,
     *         otherwise {@code false} is returned.
     */
    public static boolean CONVERT_TEST_CASE_TO_ESPRESSO_TEST() {
        return propertyOr(false);
    }

    /*
     * Begin QBE properties
     */

    /**
     * Whether the QBE model should be used or not.
     *
     * @return Returns {@code true} if the QBE model should be used, otherwise {@code false}
     *          is returned.
     */
    public static boolean QBE_MODEL() { return propertyOr(false); }

    /**
     * Whether to record (and serialize) transition systems when using any oft the ExecuteMATEQBE*
     * strategies. Has no effect for other exploration algorithms.
     *
     * @return Whether to record (and serialize) the transition system.
     */
    public static boolean QBE_RECORD_TRANSITION_SYSTEM() {
        return propertyOr(false);
    }

    /**
     * Which exploration strategy to use. Can be either random, or one of the pre-defined QBE
     * matrices. By default, the random strategy is used.
     *
     * @return Which exploration strategy to use for QBE.
     */
    public static String QBE_EXPLORATION_STRATEGY() {
        return propertyOr(null);
    }

    /*
     * End QBE properties
     */

    /**
     * The initial random seed for the random number generator.
     *
     * @return Returns the initial random seed for the random number generator.
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

    public static int BIG_POPULATION_SIZE() {
        return propertyOr(40);
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

    public static double P_SAMPLE_RANDOM() {
        return propertyOr(0.5);
    }

    public static double P_FOCUSED_SEARCH_START() {
        return propertyOr(0.5);
    }

    public static int EVO_ITERATIONS_NUMBER() {
        return propertyOr(10);
    }

    public static int MUTATION_RATE() {
        return propertyOr(1);
    }

    public static double PIPE_LEARNING_RATE() { return propertyOr(0.01); }

    public static double PIPE_NEGATIVE_LEARNING_RATE() { return propertyOr(0.2); }

    public static double PIPE_CLR() { return propertyOr(0.1); }

    public static double PIPE_PROB_ELITIST_LEARNING() { return propertyOr(0.1); }

    public static double PIPE_EPSILON() { return propertyOr(0.000001); }

    public static double PIPE_PROB_MUTATION() { return propertyOr(0.4); }

    public static double PIPE_MUTATION_RATE() { return propertyOr(0.4); }

    public static boolean PIPE_RECORD_PPT() { return propertyOr(false); }

    public static boolean PROMISING_ACTIONS() { return propertyOr(true); }

    public static int TOURNAMENT_SIZE() { return propertyOr(2); }

    public static int DEFAULT_SELECTION_SIZE() {
        return propertyOr(2);
    }

    /**
     * Convenience function to specify a single fitness function. Ideally you should specify the
     * fitness function via the property {@link #FITNESS_FUNCTIONS()}.
     *
     * @return Returns the specified fitness function or {@code null} if none was specified.
     */
    public static FitnessFunction FITNESS_FUNCTION() {

        /*
         * NOTE: We can't call another property method that relies upon propertyOr(), since the
         * property name is derived from the stack trace from a fixed position, which is corrupted
         * when nested property methods are called. Thus, we require the hard-coded property name.
         */
        FitnessFunction fitnessFunction = propertyOrNull("fitness_function");

        if (fitnessFunction == null) {
            FitnessFunction[] fitnessFunctions = propertyOrNull("fitness_functions");
            if (fitnessFunctions != null) {
                fitnessFunction = fitnessFunctions[0];
            }
        }

        return fitnessFunction;
    }

    public static FitnessFunction[] FITNESS_FUNCTIONS() {
        return propertyOr(null);
    }

    public static SelectionFunction SELECTION_FUNCTION() {
        return propertyOr(null);
    }

    /**
     * Convenience function to specify a single mutation function. Ideally you should specify the
     * mutation function via the property {@link #MUTATION_FUNCTIONS()}.
     *
     * @return Returns the specified mutation function or {@code null} if none was specified.
     */
    public static MutationFunction MUTATION_FUNCTION() {

        /*
        * NOTE: We can't call another property method that relies upon propertyOr(), since the
        * property name is derived from the stack trace from a fixed position, which is corrupted
        * when nested property methods are called. Thus, we require the hard-coded property name.
         */
        MutationFunction mutationFunction = propertyOrNull("mutation_function");

        if (mutationFunction == null) {
            MutationFunction[] mutationFunctions = propertyOrNull("mutation_functions");
            if (mutationFunctions != null) {
                mutationFunction = mutationFunctions[0];
            }
        }

        return mutationFunction;
    }

    public static MutationFunction[] MUTATION_FUNCTIONS() {
        return propertyOr(null);
    }

    /**
     * Convenience function to specify a single crossover function. Ideally you should specify the
     * crossover function via the property {@link #CROSSOVER_FUNCTIONS()}.
     *
     * @return Returns the specified crossover function or {@code null} if none was specified.
     */
    public static CrossOverFunction CROSSOVER_FUNCTION() {

        /*
         * NOTE: We can't call another property method that relies upon propertyOr(), since the
         * property name is derived from the stack trace from a fixed position, which is corrupted
         * when nested property methods are called. Thus, we require the hard-coded property name.
         */
        CrossOverFunction crossOverFunction = propertyOrNull("crossover_function");

        if (crossOverFunction == null) {
            CrossOverFunction[] mutationFunctions = propertyOrNull("crossover_functions");
            if (mutationFunctions != null) {
                crossOverFunction = mutationFunctions[0];
            }
        }

        return crossOverFunction;
    }

    public static CrossOverFunction[] CROSSOVER_FUNCTIONS() { return propertyOr(null); }

    public static TerminationCondition TERMINATION_CONDITION() { return propertyOr(null); }

    public static ChromosomeFactory CHROMOSOME_FACTORY() { return propertyOr(null); }

    public static Algorithm ALGORITHM() { return propertyOr(null); }

    /**
     * Whether a screen state should return {@link org.mate.interaction.action.ui.UIAction}s and
     * {@link org.mate.interaction.action.ui.WidgetAction}s.
     *
     * @return Returns {@code true} (default option) when a screen state should return ui actions.
     */
    public static boolean USE_UI_ACTIONS() { return propertyOr(true); }

    /**
     * Whether a screen state should return {@link org.mate.interaction.action.intent.SystemAction}s
     * and {@link org.mate.interaction.action.intent.IntentBasedAction}s.
     *
     * @return Returns {@code true} (default option) when a screen state should return intent actions.
     */
    public static boolean USE_INTENT_ACTIONS() { return propertyOr(true); }

    /**
     * Whether a screen state should return {@link org.mate.interaction.action.ui.MotifAction}s.
     *
     * @return Returns {@code true} (default option) when a screen state should return motif actions.
     */
    public static boolean USE_MOTIF_ACTIONS() { return propertyOr(true); }

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
    public static int SEED_CORPUS_SIZE() {
        return propertyOr(10);
    }

    /**
     * The maximal assignable energy p.
     *
     * @return Returns the maximal assignable energy.
     */
    public static int MAX_ENERGY() {
        return propertyOr(10);
    }

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
    public static String TARGET() {
        return propertyOr("all_branches");
    }

    // how the graph should be drawn
    public static DrawType DRAW_GRAPH() {
        return propertyOr(null);
    }

    /*
     * End Graph properties
     */

    /*
     * Begin Subjective Logic (SOSM) properties
     */

    /**
     * Whether the Subjective Opinion State Machine (SOSM) model should be used.
     *
     * @return Returns {@code true} if the SOSM model should be used, otherwise {@code false}.
     */
    public static boolean SOSM_MODEL() { return propertyOr(false); }

    /**
     * Determines the number of times a state has to be traversed for the SOSM probabilities to be
     * deemed certain.
     *
     * @return Returns the certainty threshold alpha using during SOSM inference.
     */
    public static double SOSM_CERTAINTY_THRESHOLD() {
        return propertyOr(10.0);
    }

    /**
     * Determines how much weight should be placed on uncertainty when computing the SOSM-based
     * novelty function.
     */
    public static double SOSM_NOVELTY_DISBELIEF_WEIGHT() {
        return propertyOr(0.0);
    }

    /**
     * Determines the weight factor for novelty in the combined novelty and coverage fitness function.
     *
     * @return Returns the weight factor for novelty in the combined novelty and coverage fitness
     *         function.
     */
    public static double NOVELTY_AND_COVERAGE_COMBINATION_WEIGHT() {
        return propertyOr(0.5);
    }

    /**
     * Determines which novelty estimator function should be used.
     *
     * @return Returns the name of the selected novelty estimator function.
     */
    public static NoveltyEstimator SOSM_NOVELTY_ESTIMATOR() {
        return propertyOr(NoveltyEstimator.MAX_NOVEL_SUBSEQUENCE);
    }

    /*
     * End Subjective Logic (SOSM) properties
     */

    /**
     * Whether {@link org.mate.interaction.action.ui.PrimitiveAction}s should be used instead of
     * {@link org.mate.interaction.action.ui.WidgetAction}s. This is only necessary when using an
     * algorithm like {@link org.mate.exploration.genetic.algorithm.Sapienz} that relies upon
     * primitive actions.
     *
     * @return Returns {@code true} if primitive actions should be used instead of widget actions,
     *         otherwise {@code false} is returned.
     */
    public static boolean USE_PRIMITIVE_ACTIONS() {
        return propertyOr(false);
    }

    /**
     * Whether to record internally the stack trace of a discovered crash.
     *
     * @return Returns {@code false} by default, i.e. no stack trace is recorded.
     */
    public static boolean RECORD_STACK_TRACE() {
        return propertyOr(false);
    }

    /**
     * Whether a recorded stack trace should be written to file.
     *
     * @return Returns {@code false} by default, i.e. no stack trace is written to file.
     */
    public static boolean WRITE_STACK_TRACE_TO_FILE() { return propertyOr(false); }

    /**
     * The stack trace file name required for crash reproduction.
     *
     * @return Returns the file name of the stack trace file.
     */
    public static String STACK_TRACE_PATH() { return propertyOr("stack_trace.txt"); }

    /**
     * Whether some data retrieved from the stack trace should be used as input for input fields.
     * This functionality is only relevant in the context of crash reproduction.
     *
     * @return Returns {@code false} by default, i.e. no user input is derived from the stack trace.
     */
    public static boolean STACK_TRACE_USER_INPUT_SEEDING() { return propertyOr(false); }

    /*
     * Begin GE properties
     */

    /**
     * Whether a mapping from geno to pheno type is required.
     *
     * @return Returns {@code true} if a mapping is required, otherwise {@code false} is returned.
     */
    public static boolean GENO_TO_PHENO_TYPE_MAPPING() {
        return propertyOr(false);
    }

    /**
     * Specifies the mapping function which is used for the geno to pheno type mapping.
     *
     * @return Returns the geno to pheno type mapping function.
     */
    public static GEMappingFunction GE_MAPPING_FUNCTION() {
        return propertyOr(null);
    }

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
    public static double NOVELTY_THRESHOLD() {
        return propertyOr(0.0);
    }

    /**
     * Novelty Search - Defines the maximal size of the archive.
     *
     * @return Returns the archive size L.
     */
    public static int ARCHIVE_LIMIT() {
        return propertyOr(10);
    }

    /**
     * Novelty Search - Defines the number of nearest neighbours that should be considered
     * for the novelty metric.
     *
     * @return Returns the number of nearest neighbours k.
     */
    public static int NEAREST_NEIGHBOURS() {
        return propertyOr(3);
    }

    /**
     * Controls whether quick launch is enabled or disabled.
     *
     * @return Returns {@code true} if quick launch is enabled, otherwise {@code false} is returned.
     */
    public static boolean QUICK_LAUNCH() {
        return propertyOr(true);
    }

    /*
     * Begin AimDroid properties
     */

    /**
     * The epsilon used in the epsilon greedy learning policy.
     *
     * @return Returns the epsilon used in the learning policy.
     */
    public static double EPSILON() {
        return propertyOr(0.1d);
    }

    /**
     * The alpha used in the SARSA equation.
     *
     * @return Returns the alpha used in the SARSA equation.
     */
    public static double ALPHA() {
        return propertyOr(0.8d);
    }

    /**
     * The gamma used in the SARSA equation.
     *
     * @return Returns the gamma used in the SARSA equation.
     */
    public static double GAMMA() {
        return propertyOr(0.8d);
    }

    /**
     * The minL constant used in the bound method (the minimal number of actions).
     *
     * @return Returns the minL constant.
     */
    public static int MIN_L() {
        return propertyOr(20);
    }

    /**
     * The maxL constant used in the bound method (the maximal number of actions).
     *
     * @return Returns the maxL constant.
     */
    public static int MAX_L() {
        return propertyOr(50);
    }

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
    public static float ABT_EPSILON() {
        return propertyOr(0.8f);
    }

    /**
     * The static discount factor used in equation (1).
     *
     * @return Returns the static discount factor.
     */
    public static float ABT_DISCOUNT_FACTOR() {
        return propertyOr(0.9f);
    }

    /**
     * The maximal number of episodes (testcases).
     *
     * @return Returns the maximal number of episodes.
     */
    public static int ABT_MAX_NUM_OF_EPISODES() {
        return propertyOr(100);
    }

    /**
     * The maximal length of an episode (a test case).
     *
     * @return Returns the maximal episode length.
     */
    public static int ABT_MAX_EPISODE_LENGTH() {
        return propertyOr(50);
    }

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
    public static float P_HOME_BUTTON() {
        return propertyOr(0.05f);
    }

    /**
     * The initial q-value for a new action.
     *
     * @return Returns the initial q-value for a new action.
     */
    public static float INITIAL_Q_VALUE() {
        return propertyOr(500f);
    }

    /**
     * The maximal number of episodes (testcases).
     *
     * @return Returns the maximal number of episodes.
     */
    public static int MAX_NUM_OF_EPISODES() {
        return propertyOr(100);
    }

    /**
     * The maximal length of an episode (a test case).
     *
     * @return Returns the maximal episode length.
     */
    public static int MAX_EPISODE_LENGTH() {
        return propertyOr(50);
    }

    /*
     * End AutoDroid properties
     */

    /*
     * Begin Dot Graph properties
     */

    /**
     * Whether the gui model should be converted to a dot file and how often.
     *
     * @return Returns the conversion option.
     */
    public static DotConverter.Option CONVERT_GUI_TO_DOT() { return propertyOr(DotConverter.Option.NONE); }

    /**
     * Determines if the generated dot graph should include screenshots or plain labels for the states.
     *
     * @return Returns {@code true} if the dot graph should include screenshots,
     *          otherwise {@code false} is returned.
     */
    public static boolean DOT_GRAPH_WITH_SCREENSHOTS() {return propertyOr(false); }

    /*
     * End Dot Graph properties
     */

    /**
     * Looks up the value of the property in the Properties object stored in the Registry using the
     * name of the caller method as the key of the property. If no property with that key is stored
     * the given default value will be returned.
     *
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

    @SuppressWarnings("unchecked")
    private static <T> T propertyOrNull(String key) {
        Properties propertiesInstance = Registry.getProperties();
        if (propertiesInstance.store.containsKey(key)) {
            return ((T) propertiesInstance.store.get(key));
        }
        return null;
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
