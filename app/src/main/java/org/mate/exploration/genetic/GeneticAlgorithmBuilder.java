package org.mate.exploration.genetic;

import java.util.Properties;

public class GeneticAlgorithmBuilder {
    private final String SEPARATOR = ",";
    private final String ALGORITHM_KEY = "algorithm";
    private final String FITNESS_FUNCTIONS_KEY = "fitness_functions";
    private Properties properties;

    public GeneticAlgorithmBuilder withAlgorithm(String algorithmName) {
        properties.setProperty(ALGORITHM_KEY, algorithmName);
        return this;
    }

    public GeneticAlgorithmBuilder withFitnessFunction(String fitnessFunctionName) {
        String oldValue = properties.getProperty(FITNESS_FUNCTIONS_KEY);
        if (oldValue == null) {
            properties.setProperty(FITNESS_FUNCTIONS_KEY, fitnessFunctionName);
        } else {
            properties.setProperty(FITNESS_FUNCTIONS_KEY,
                    oldValue + SEPARATOR + fitnessFunctionName);
        }
        return this;
    }

    public <T> IGeneticAlgorithm<T> build() {
        return GeneticAlgorithmProvider.getGeneticAlgorithm(properties);
    }
}
