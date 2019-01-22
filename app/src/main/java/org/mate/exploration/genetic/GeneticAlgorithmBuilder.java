package org.mate.exploration.genetic;

import org.mate.MATE;

import java.util.Locale;
import java.util.Properties;

public class GeneticAlgorithmBuilder {
    public static final Locale FORMAT_LOCALE = Locale.US;
    public static final String TRUE_STRING = "true";
    public static final String ALGORITHM_KEY = "algorithm";
    public static final String FITNESS_FUNCTION_KEY_FORMAT = "fitness_function_%d";
    public static final String FITNESS_FUNCTION_ARG_KEY_FORMAT = "fitness_function_%d_arg";
    public static final String CHROMOSOME_FACTORY_KEY = "chromosome_factory";
    public static final String MAX_NUM_EVENTS_KEY = "num_events";
    public static final String USE_DEFAULTS_KEY = "use_defaults";
    public static final String TERMINATION_CONDITION_KEY = "termination_condition";
    public static final String NUMBER_ITERATIONS_KEY = "number_of_iterations";
    public static final String SELECTION_FUNCTION_KEY = "selection_function";
    public static final String CROSSOVER_FUNCTION_KEY = "crossover_function";
    public static final String MUTATION_FUNCTION_KEY = "mutation_function";
    public static final String NUM_TEST_CASES_KEY = "num_test_cases";
    public static final String POPULATION_SIZE_KEY = "population_size";
    public static final String P_MUTATE_KEY = "p_mutate";
    public static final String P_INNER_MUTATE_KEY = "p_inner_mutate";
    public static final String P_CROSSOVER_KEY = "p_crossover";
    public static final String AMOUNT_FITNESS_FUNCTIONS_KEY = "amount_fitness_functions";
    public static final String BIG_POPULATION_SIZE_KEY = "big_population_size";
    public static final String P_SAMPLE_RANDOM_KEY = "p_sample_random";
    public static final String FOCUSED_SEARCH_START_KEY = "focused_search_start";

    private Properties properties;

    public GeneticAlgorithmBuilder() {
        properties = new Properties();
        properties.setProperty(USE_DEFAULTS_KEY, TRUE_STRING);
        properties.setProperty(AMOUNT_FITNESS_FUNCTIONS_KEY, "0");
    }

    public GeneticAlgorithmBuilder noDefaults() {
        properties.remove(USE_DEFAULTS_KEY);
        return this;
    }

    public GeneticAlgorithmBuilder withAlgorithm(String algorithmName) {
        properties.setProperty(ALGORITHM_KEY, algorithmName);
        return this;
    }

    public GeneticAlgorithmBuilder withChromosomeFactory(String chromosomeFactoryId) {
        properties.setProperty(CHROMOSOME_FACTORY_KEY, chromosomeFactoryId);
        return this;
    }

    public GeneticAlgorithmBuilder withSelectionFunction(String selectionFunctionId) {
        properties.setProperty(SELECTION_FUNCTION_KEY, selectionFunctionId);
        return this;
    }

    public GeneticAlgorithmBuilder withCrossoverFunction(String crossoverFunctionId) {
        properties.setProperty(CROSSOVER_FUNCTION_KEY, crossoverFunctionId);
        return this;
    }

    public GeneticAlgorithmBuilder withMutationFunction(String mutationFunctionId) {
        properties.setProperty(MUTATION_FUNCTION_KEY, mutationFunctionId);
        return this;
    }

    public GeneticAlgorithmBuilder withFitnessFunction(String fitnessFunctionId) {
        int amountFitnessFunctions = Integer.valueOf(
                properties.getProperty(AMOUNT_FITNESS_FUNCTIONS_KEY));
        properties.setProperty(AMOUNT_FITNESS_FUNCTIONS_KEY,
                String.valueOf(amountFitnessFunctions + 1));

        String key = String.format(FORMAT_LOCALE, FITNESS_FUNCTION_KEY_FORMAT,
                amountFitnessFunctions);
        properties.setProperty(key, fitnessFunctionId);
        return this;
    }

    public GeneticAlgorithmBuilder withFitnessFunction(String fitnessFunctionId, String arg1) {
        int amountFitnessFunctions = Integer.valueOf(
                properties.getProperty(AMOUNT_FITNESS_FUNCTIONS_KEY));

        String key = String.format(FORMAT_LOCALE, FITNESS_FUNCTION_ARG_KEY_FORMAT,
                amountFitnessFunctions);

        properties.setProperty(key, arg1);

        withFitnessFunction(fitnessFunctionId);
        return this;
    }

    public GeneticAlgorithmBuilder withTerminationCondition(String terminationConditionId) {
        properties.setProperty(TERMINATION_CONDITION_KEY, terminationConditionId);
        return this;
    }

    public GeneticAlgorithmBuilder withNumTestCases(int numTestCases) {
        properties.setProperty(NUM_TEST_CASES_KEY, String.valueOf(numTestCases));
        return this;
    }

    public GeneticAlgorithmBuilder withMaxNumEvents(int maxNumEvents) {
        properties.setProperty(MAX_NUM_EVENTS_KEY, String.valueOf(maxNumEvents));
        return this;
    }

    public GeneticAlgorithmBuilder withNumberIterations(int numberIterations) {
        properties.setProperty(NUMBER_ITERATIONS_KEY, String.valueOf(numberIterations));
        return this;
    }

    public GeneticAlgorithmBuilder withPopulationSize(int populationSize) {
        properties.setProperty(POPULATION_SIZE_KEY, String.valueOf(populationSize));
        return this;
    }

    public GeneticAlgorithmBuilder withBigPopulationSize(int generationSurvivorCount) {
        properties.setProperty(BIG_POPULATION_SIZE_KEY, String.valueOf(generationSurvivorCount));
        return this;
    }

    public GeneticAlgorithmBuilder withPMutate(double pMutate) {
        properties.setProperty(P_MUTATE_KEY, String.valueOf(pMutate));
        return this;
    }

    public GeneticAlgorithmBuilder withPInnerMutate(double pInnerMutate) {
        properties.setProperty(P_INNER_MUTATE_KEY, String.valueOf(pInnerMutate));
        return this;
    }

    public GeneticAlgorithmBuilder withPCrossover(double pCrossover) {
        properties.setProperty(P_CROSSOVER_KEY, String.valueOf(pCrossover));
        return this;
    }

    public GeneticAlgorithmBuilder withPSampleRandom(double pSampleRandom) {
        properties.setProperty(P_SAMPLE_RANDOM_KEY, String.valueOf(pSampleRandom));
        return this;
    }

    public GeneticAlgorithmBuilder withFocusedSearchStart(double focusedSearchStart) {
        properties.setProperty(FOCUSED_SEARCH_START_KEY, String.valueOf(focusedSearchStart));
        return this;
    }

    public <T> IGeneticAlgorithm<T> build() {
        MATE.log_acc("Building genetic algorithm with properties:");
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("fitness_function") && Integer.valueOf(properties.getProperty(AMOUNT_FITNESS_FUNCTIONS_KEY)) > 5) {
                continue;
            }
            MATE.log_acc("Key: " + key + ", Value: " + properties.getProperty(key));
        }
        return GeneticAlgorithmProvider.getGeneticAlgorithm(properties);
    }
}
