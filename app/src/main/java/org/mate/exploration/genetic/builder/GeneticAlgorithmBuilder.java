package org.mate.exploration.genetic.builder;

import org.mate.MATE;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.exploration.genetic.util.ge.GEMappingFunction;

import java.util.Locale;
import java.util.Properties;

/**
 * Provides a genetic algorithm builder for {@link org.mate.exploration.genetic.core.GeneticAlgorithm}s.
 */
public class GeneticAlgorithmBuilder {

    public static final Locale FORMAT_LOCALE = Locale.US;
    public static final String TRUE_STRING = "true";
    public static final String ALGORITHM_KEY = "algorithm";
    public static final String FITNESS_FUNCTION_KEY_FORMAT = "fitness_function_%d";
    public static final String FITNESS_FUNCTION_ARG_KEY_FORMAT = "fitness_function_%d_arg";
    public static final String CROSSOVER_FUNCTION_KEY_FORMAT = "crossover_function_%d";
    public static final String MUTATION_FUNCTION_KEY_FORMAT = "mutation_function_%d";
    public static final String CHROMOSOME_FACTORY_KEY = "chromosome_factory";
    public static final String MAX_NUM_EVENTS_KEY = "num_events";
    public static final String USE_DEFAULTS_KEY = "use_defaults";
    public static final String TERMINATION_CONDITION_KEY = "termination_condition";
    public static final String NUMBER_ITERATIONS_KEY = "number_of_iterations";
    public static final String SELECTION_FUNCTION_KEY = "selection_function";
    public static final String NUM_TESTCASES_KEY = "num_test_cases";
    public static final String POPULATION_SIZE_KEY = "population_size";
    public static final String P_MUTATE_KEY = "p_mutate";
    public static final String P_CROSSOVER_KEY = "p_crossover";
    public static final String AMOUNT_FITNESS_FUNCTIONS_KEY = "amount_fitness_functions";
    public static final String AMOUNT_CROSSOVER_FUNCTIONS_KEY = "amount_crossover_functions";
    public static final String AMOUNT_MUTATION_FUNCTIONS_KEY = "amount_mutation_functions";
    public static final String BIG_POPULATION_SIZE_KEY = "big_population_size";
    public static final String P_SAMPLE_RANDOM_KEY = "p_sample_random";
    public static final String FOCUSED_SEARCH_START_KEY = "focused_search_start";
    public static final String MUTATION_RATE_KEY = "mutation_rate";
    public static final String NOVELTY_THRESHOLD_KEY = "novelty_threshold";
    public static final String ARCHIVE_LIMIT_KEY = "archive_limit";
    public static final String NEAREST_NEIGHBOURS_KEY = "nearest_neighbours";
    public static final String GE_MAPPING_FUNCTION_KEY = "ge_mapping_function";
    public static final String GENO_TO_PHENO_TYPE_MAPPING_KEY = "geno_to_pheno_type_mapping";

    /**
     * The list of properties..
     */
    private final Properties properties;

    /**
     * Initialises the genetic algorithm builder.
     */
    public GeneticAlgorithmBuilder() {
        properties = new Properties();
        properties.setProperty(USE_DEFAULTS_KEY, TRUE_STRING);
        properties.setProperty(AMOUNT_FITNESS_FUNCTIONS_KEY, "0");
        properties.setProperty(AMOUNT_CROSSOVER_FUNCTIONS_KEY, "0");
        properties.setProperty(AMOUNT_MUTATION_FUNCTIONS_KEY, "0");
    }

    /**
     * Disables the usage of default values.
     *
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder noDefaults() {
        properties.remove(USE_DEFAULTS_KEY);
        return this;
    }

    /**
     * Specifies the genetic algorithm.
     *
     * @param algorithm The genetic algorithm that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withAlgorithm(Algorithm algorithm) {

        properties.setProperty(ALGORITHM_KEY, algorithm.name());

        // TODO: Remove once all properties are enforced via the mate.properties file!
        if (org.mate.Properties.ALGORITHM() == null) {
            org.mate.Properties.setProperty("algorithm", algorithm);
        }

        return this;
    }

    /**
     * Specifies the chromosome factory of the genetic algorithm.
     *
     * @param chromosomeFactory The chromosome factory that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withChromosomeFactory(ChromosomeFactory chromosomeFactory) {

        properties.setProperty(CHROMOSOME_FACTORY_KEY, chromosomeFactory.name());

        // TODO: Remove once all properties are enforced via the mate.properties file!
        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            org.mate.Properties.setProperty("chromosome_factory", chromosomeFactory);
        }

        return this;
    }

    /**
     * Specifies the selection function of the genetic algorithm.
     *
     * @param selectionFunction The selection function that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withSelectionFunction(SelectionFunction selectionFunction) {

        properties.setProperty(SELECTION_FUNCTION_KEY, selectionFunction.name());

        // TODO: Remove once all properties are enforced via the mate.properties file!
        if (org.mate.Properties.SELECTION_FUNCTION() == null) {
            org.mate.Properties.setProperty("selection_function", selectionFunction);
        }

        return this;
    }

    /**
     * Specifies the crossover function of the genetic algorithm.
     *
     * @param crossoverFunction The crossover function that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withCrossoverFunction(CrossOverFunction crossoverFunction) {

        if (crossoverFunction == null) {
            // there might be no crossover function required
            return this;
        } else {
            return withCrossoverFunctions(new CrossOverFunction[]{crossoverFunction});
        }
    }

    /**
     * Specifies the crossover functions of the genetic algorithm.
     *
     * @param crossoverFunctions A list of crossover functions that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withCrossoverFunctions(CrossOverFunction[] crossoverFunctions) {

        if (crossoverFunctions == null) {
            // there might be no crossover function(s) required
            return this;
        }

        for (int i = 0; i < crossoverFunctions.length; i++) {
            String key = String.format(FORMAT_LOCALE, CROSSOVER_FUNCTION_KEY_FORMAT, i);
            properties.setProperty(key, crossoverFunctions[i].name());
        }

        properties.setProperty(AMOUNT_CROSSOVER_FUNCTIONS_KEY, String.valueOf(crossoverFunctions.length));

        // TODO: Remove once all properties are enforced via the mate.properties file!
        if (org.mate.Properties.CROSSOVER_FUNCTIONS() == null) {
            org.mate.Properties.setProperty("crossover_functions", crossoverFunctions);
        }

        return this;
    }

    /**
     * Specifies the mutation function of the genetic algorithm.
     *
     * @param mutationFunction The mutation function that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withMutationFunction(MutationFunction mutationFunction) {
        
        if (mutationFunction == null) {
            // there might be no mutation function required
            return this;
        } else {
            return withMutationFunctions(new MutationFunction[]{mutationFunction});
        }
    }

    /**
     * Specifies the mutation functions of the genetic algorithm.
     *
     * @param mutationFunctions A list of mutation functions that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withMutationFunctions(MutationFunction[] mutationFunctions) {

        if (mutationFunctions == null) {
            // there might be no mutation function(s) required
            return this;
        }

        for (int i = 0; i < mutationFunctions.length; i++) {
            String key = String.format(FORMAT_LOCALE, MUTATION_FUNCTION_KEY_FORMAT, i);
            properties.setProperty(key, mutationFunctions[i].name());
        }

        properties.setProperty(AMOUNT_MUTATION_FUNCTIONS_KEY, String.valueOf(mutationFunctions.length));

        // TODO: Remove once all properties are enforced via the mate.properties file!
        if (org.mate.Properties.MUTATION_FUNCTIONS() == null) {
            org.mate.Properties.setProperty("mutation_functions", mutationFunctions);
        }

        return this;
    }

    /**
     * Specifies the fitness function of the genetic algorithm.
     *
     * @param fitnessFunction The fitness function that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withFitnessFunction(FitnessFunction fitnessFunction) {
        return withFitnessFunctions(new FitnessFunction[]{fitnessFunction});
    }

    /**
     * Specifies the fitness function of the genetic algorithm.
     *
     * @param fitnessFunction The fitness function that should be used.
     * @param arg An optional argument for the fitness function.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withFitnessFunction(FitnessFunction fitnessFunction, String arg) {
        return withFitnessFunctions(new FitnessFunction[]{fitnessFunction}, new String[]{arg});
    }

    /**
     * Specifies the fitness functions in a many-objective search algorithm like MIO or MOSA.
     *
     * @param fitnessFunction The fitness function that should be used for each objective.
     * @param numberOfObjectives The number of objectives, e.g. branches.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withFitnessFunctions(FitnessFunction fitnessFunction, int numberOfObjectives) {

        FitnessFunction[] fitnessFunctions = new FitnessFunction[numberOfObjectives];

        for (int i = 0; i < numberOfObjectives; i++) {
            String key = String.format(FORMAT_LOCALE, FITNESS_FUNCTION_KEY_FORMAT, i);
            properties.setProperty(key, fitnessFunction.name());
            fitnessFunctions[i] = fitnessFunction;
        }

        properties.setProperty(AMOUNT_FITNESS_FUNCTIONS_KEY, String.valueOf(numberOfObjectives));

        // TODO: Remove once all properties are enforced via the mate.properties file!
        if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            org.mate.Properties.setProperty("fitness_functions", fitnessFunctions);
        }

        return this;
    }

    /**
     * Specifies the fitness functions of the genetic algorithm.
     *
     * @param fitnessFunctions A list of fitness functions that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withFitnessFunctions(FitnessFunction[] fitnessFunctions) {

        for (int i = 0; i < fitnessFunctions.length; i++) {
            String key = String.format(FORMAT_LOCALE, FITNESS_FUNCTION_KEY_FORMAT, i);
            properties.setProperty(key, fitnessFunctions[i].name());
        }

        properties.setProperty(AMOUNT_FITNESS_FUNCTIONS_KEY, String.valueOf(fitnessFunctions.length));

        // TODO: Remove once all properties are enforced via the mate.properties file!
        if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            org.mate.Properties.setProperty("fitness_functions", fitnessFunctions);
        }

        return this;
    }

    /**
     * Specifies that a geno to pheno type mapping is required.
     *
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withGenoToPhenoTypeMapping() {

        properties.setProperty(GENO_TO_PHENO_TYPE_MAPPING_KEY, "true");

        if (!org.mate.Properties.GENO_TO_PHENO_TYPE_MAPPING()) {
            org.mate.Properties.setProperty("geno_to_pheno_type_mapping", true);
        }

        return this;
    }

    /**
     * Specifies the fitness functions of the genetic algorithm with a custom fitness function argument.
     *
     * @param args A custom fitness function argument for each fitness function.
     * @param fitnessFunctions A list of fitness function that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withFitnessFunctions(FitnessFunction[] fitnessFunctions, String[] args) {

        // each fitness function has a single argument
        assert fitnessFunctions.length == args.length;

        for (int i = 0; i < args.length; i++) {
            String key = String.format(FORMAT_LOCALE, FITNESS_FUNCTION_ARG_KEY_FORMAT, i);
            properties.setProperty(key, args[i]);
        }

        withFitnessFunctions(fitnessFunctions);
        return this;
    }

    /**
     * Specifies the termination condition of the genetic algorithm.
     *
     * @param terminationCondition The termination condition that should be used.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withTerminationCondition(TerminationCondition terminationCondition) {

        properties.setProperty(TERMINATION_CONDITION_KEY, terminationCondition.name());

        // TODO: Remove once all properties are enforced via the mate.properties file!
        if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            org.mate.Properties.setProperty("termination_condition", terminationCondition);
        }

        return this;
    }

    /**
     * Specifies the number of test cases per test suite.
     *
     * @param numTestCases The number of test cases per test suite.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withNumTestCases(int numTestCases) {
        properties.setProperty(NUM_TESTCASES_KEY, String.valueOf(numTestCases));
        return this;
    }

    /**
     * Specifies the maximal number of actions per test case.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withMaxNumEvents(int maxNumEvents) {
        properties.setProperty(MAX_NUM_EVENTS_KEY, String.valueOf(maxNumEvents));
        return this;
    }

    /**
     * Specifies the number of iterations for the
     * {@link org.mate.exploration.genetic.termination.IterTerminationCondition}.
     *
     * @param numberIterations The number of iterations.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withNumberIterations(int numberIterations) {
        properties.setProperty(NUMBER_ITERATIONS_KEY, String.valueOf(numberIterations));
        return this;
    }

    /**
     * Specifies the population size.
     *
     * @param populationSize The population size.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withPopulationSize(int populationSize) {
        properties.setProperty(POPULATION_SIZE_KEY, String.valueOf(populationSize));
        return this;
    }

    /**
     * Specifies the big population size.
     *
     * @param bigPopulationSize The big population size.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withBigPopulationSize(int bigPopulationSize) {
        properties.setProperty(BIG_POPULATION_SIZE_KEY, String.valueOf(bigPopulationSize));
        return this;
    }

    /**
     * Specifies the probability for mutation.
     *
     * @param pMutate The probability for mutation.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withPMutate(double pMutate) {
        properties.setProperty(P_MUTATE_KEY, String.valueOf(pMutate));
        return this;
    }

    /**
     * Specifies the probability for crossover.
     *
     * @param pCrossover The probability for crossover.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withPCrossover(double pCrossover) {
        properties.setProperty(P_CROSSOVER_KEY, String.valueOf(pCrossover));
        return this;
    }

    /**
     * Specifies the probability for random sampling used in
     * {@link org.mate.exploration.genetic.algorithm.MIO}.
     *
     * @param pSampleRandom The probability for random sampling.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withPSampleRandom(double pSampleRandom) {
        properties.setProperty(P_SAMPLE_RANDOM_KEY, String.valueOf(pSampleRandom));
        return this;
    }

    /**
     * Specifies the percentage of the search budget after which the focused search should start,
     * which is used in {@link org.mate.exploration.genetic.algorithm.MIO}.
     *
     * @param focusedSearchStart The percentage after which the focused search should start.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withFocusedSearchStart(double focusedSearchStart) {
        properties.setProperty(FOCUSED_SEARCH_START_KEY, String.valueOf(focusedSearchStart));
        return this;
    }

    /**
     * Specifies the novelty threshold for {@link org.mate.exploration.genetic.algorithm.NoveltySearch}.
     *
     * @param noveltyThreshold The novelty threshold.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withNoveltyThreshold(double noveltyThreshold) {
        properties.setProperty(NOVELTY_THRESHOLD_KEY, String.valueOf(noveltyThreshold));
        return this;
    }

    /**
     * Specifies the archive size limit used in {@link org.mate.exploration.genetic.algorithm.NoveltySearch}.
     *
     * @param archiveLimit The archive size limit.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withArchiveLimit(int archiveLimit) {
        properties.setProperty(ARCHIVE_LIMIT_KEY, String.valueOf(archiveLimit));
        return this;
    }

    /**
     * Specifies the number of nearest neighbours used in {@link org.mate.exploration.genetic.algorithm.NoveltySearch}.
     *
     * @param nearestNeighbours The number of nearest neighbours k.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withNearestNeighbours(int nearestNeighbours) {
        properties.setProperty(NEAREST_NEIGHBOURS_KEY, String.valueOf(nearestNeighbours));
        return this;
    }

    /**
     * Specifies the mutation rate used in {@link org.mate.exploration.genetic.algorithm.MIO}.
     *
     * @param mutationRate The mutation rate.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withMutationRate(int mutationRate) {
        properties.setProperty(MUTATION_RATE_KEY, String.valueOf(mutationRate));
        return this;
    }

    /**
     * Specifies the mapping function used in GE.
     *
     * @param mappingFunction The GE mapping function.
     * @return Returns the current builder state.
     */
    public GeneticAlgorithmBuilder withGEMappingFunction(GEMappingFunction mappingFunction) {
        properties.setProperty(GE_MAPPING_FUNCTION_KEY, mappingFunction.name());

        // TODO: Remove once all properties are enforced via the mate.properties file!
        if (org.mate.Properties.GE_MAPPING_FUNCTION() == null) {
            org.mate.Properties.setProperty("ge_mapping_function", mappingFunction);
        }

        return this;
    }

    /**
     * Builds the genetic algorithm by consuming the specified properties.
     *
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the constructed genetic algorithm.
     */
    public <T> IGeneticAlgorithm<T> build() {
        MATE.log_acc("Building genetic algorithm with properties:");
        for (String key : properties.stringPropertyNames()) {
            if ((key.startsWith("fitness_function")
                    && Integer.parseInt(properties.getProperty(AMOUNT_FITNESS_FUNCTIONS_KEY)) > 5)
                || (key.startsWith("crossover_function")
                    && Integer.parseInt(properties.getProperty(AMOUNT_CROSSOVER_FUNCTIONS_KEY)) > 5)
                || (key.startsWith("mutation_function")
                    && Integer.parseInt(properties.getProperty(AMOUNT_MUTATION_FUNCTIONS_KEY)) > 5)) {
                continue;
            }
            MATE.log_acc("Key: " + key + ", Value: " + properties.getProperty(key));
        }
        return GeneticAlgorithmProvider.getGeneticAlgorithm(properties);
    }
}
