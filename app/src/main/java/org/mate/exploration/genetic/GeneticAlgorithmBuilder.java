package org.mate.exploration.genetic;

import java.util.Properties;

public class GeneticAlgorithmBuilder {
    // Separator symbol must not appear in any id (e.g. not in the id
    // AndroidStateFitnessFunction.FITNESS_FUNCTION_ID)
    public static final String SEPARATOR = ",";
    public static final String TRUE_STRING = "true";
    public static final String ALGORITHM_KEY = "algorithm";
    public static final String FITNESS_FUNCTIONS_KEY = "fitness_functions";
    public static final String CHROMOSOME_FACTORY_KEY = "chromosome_factory";
    public static final String MAX_NUM_EVENTS_KEY = "num_events";
    public static final String USE_DEFAULTS_KEY = "use_defaults";
    public static final String TERMINATION_CONDITION_KEY = "termination_condition";
    public static final String NUMBER_ITERATIONS_KEY = "number_of_iterations";
    public static final String SELECTION_FUNCTION_KEY = "selection_function";
    public static final String CROSSOVER_FUNCTION_KEY = "crossover_function";
    public static final String MUTATION_FUNCTION_KEY = "mutation_function";

    private Properties properties;

    public GeneticAlgorithmBuilder() {
        properties = new Properties();
        properties.setProperty(USE_DEFAULTS_KEY, TRUE_STRING);
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
        String oldValue = properties.getProperty(FITNESS_FUNCTIONS_KEY);
        if (oldValue == null) {
            properties.setProperty(FITNESS_FUNCTIONS_KEY, fitnessFunctionId);
        } else {
            properties.setProperty(FITNESS_FUNCTIONS_KEY,
                    oldValue + SEPARATOR + fitnessFunctionId);
        }
        return this;
    }

    public GeneticAlgorithmBuilder withTerminationCondition(String terminationConditionId) {
        properties.setProperty(TERMINATION_CONDITION_KEY, terminationConditionId);
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

    public <T> IGeneticAlgorithm<T> build() {
        return GeneticAlgorithmProvider.getGeneticAlgorithm(properties);
    }
}
