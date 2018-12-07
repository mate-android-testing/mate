package org.mate.exploration.genetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GeneticAlgorithmProvider {
    private boolean useDefaults;
    private Properties properties;

    public static <T> GeneticAlgorithm<T> getGeneticAlgorithm(Properties properties) {
        GeneticAlgorithmProvider gaProvider = new GeneticAlgorithmProvider(properties);
        return gaProvider.getGeneticAlgorithm();
    }

    private GeneticAlgorithmProvider(Properties properties) {
        this.properties = properties;
        setUseDefaults();
    }

    private void setUseDefaults() {
        useDefaults = properties.getProperty(GeneticAlgorithmBuilder.USE_DEFAULTS_KEY)
                .equals(GeneticAlgorithmBuilder.TRUE_STRING);
    }

    private <T> GeneticAlgorithm<T> getGeneticAlgorithm() {
        String algorithmName = properties.getProperty(GeneticAlgorithmBuilder.ALGORITHM_KEY);
        if (algorithmName == null) {
            throw new IllegalArgumentException("No algorithm specified");
        }
        switch (algorithmName) {
            case OnePlusOne.ALGORITHM_NAME:
                return initializeOnePlusOne();
            default:
                throw new UnsupportedOperationException("Unknown algorithm: " + algorithmName);
        }

    }

    private <T> OnePlusOne<T> initializeOnePlusOne() {
        return new OnePlusOne<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition());
    }

    private <T> IChromosomeFactory<T> initializeChromosomeFactory() {
        String chromosomeFactoryId
                = properties.getProperty(GeneticAlgorithmBuilder.CHROMOSOME_FACTORY_KEY);
        if (chromosomeFactoryId == null) {
            return null;
        }
        switch (chromosomeFactoryId) {
            case AndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new AndroidRandomChromosomeFactory(getNumEvents());
            default:
                throw new UnsupportedOperationException("Unknown chromosome factory: "
                        + chromosomeFactoryId);
        }
    }

    private <T> ISelectionFunction<T> initializeSelectionFunction() {
        String selectionFunctionId
                = properties.getProperty(GeneticAlgorithmBuilder.SELECTION_FUNCTION_KEY);
        if (selectionFunctionId == null) {
            return null;
        } else {
            switch (selectionFunctionId) {
                case FitnessSelectionFunction.SELECTION_FUNCTION_ID:
                    return new FitnessSelectionFunction<T>();
                default:
                    throw new UnsupportedOperationException("Unknown selection function: "
                            + selectionFunctionId);
            }
        }
    }

    private <T> ICrossOverFunction<T> initializeCrossOverFunction() {
        String crossOverFunctionId
                = properties.getProperty(GeneticAlgorithmBuilder.CROSSOVER_FUNCTION_KEY);
        if (crossOverFunctionId == null) {
            return null;
        } else {
            switch (crossOverFunctionId) {
                default:
                    throw new UnsupportedOperationException("Unknown crossover function: "
                            + crossOverFunctionId);
            }
        }
    }

    private <T> IMutationFunction<T> initializeMutationFunction() {
        String mutationFunctionId
                = properties.getProperty(GeneticAlgorithmBuilder.MUTATION_FUNCTION_KEY);
        if (mutationFunctionId == null) {
            return null;
        } else {
            switch (mutationFunctionId) {
                case CutPointMutationFunction.MUTATION_FUNCTION_ID:
                    // Force cast. Only works if T is TestCase. This fails if other properties expect a
                    // different T for their chromosomes
                    return (IMutationFunction<T>) new CutPointMutationFunction(getNumEvents());
                default:
                    throw new UnsupportedOperationException("Unknown mutation function: "
                            + mutationFunctionId);
            }
        }
    }

    private <T> List<IFitnessFunction<T>> initializeFitnessFunctions() {
        String fitnessFunctionIds
                = properties.getProperty(GeneticAlgorithmBuilder.FITNESS_FUNCTIONS_KEY);
        if (fitnessFunctionIds == null) {
            return null;
        } else if (fitnessFunctionIds.isEmpty()) {
            return new ArrayList<>();
        } else {
            List<IFitnessFunction<T>> fitnessFunctions = new ArrayList<>();
            for (String fitnessFunctionId
                    : fitnessFunctionIds.split(GeneticAlgorithmBuilder.SEPARATOR)) {
                fitnessFunctions.add(this.<T>initializeFitnessFunction(fitnessFunctionId));
            }
            return fitnessFunctions;
        }
    }

    private <T> IFitnessFunction<T> initializeFitnessFunction(String fitnessFunctionId) {
        switch (fitnessFunctionId) {
            case AndroidStateFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new AndroidStateFitnessFunction();
            default:
                throw new UnsupportedOperationException("Unknown fitness function: "
                        + fitnessFunctionId);
        }
    }

    private ITerminationCondition initializeTerminationCondition() {
        String terminationConditionId
                = properties.getProperty(GeneticAlgorithmBuilder.TERMINATION_CONDITION_KEY);
        if (terminationConditionId == null) {
            return null;
        }
        switch (terminationConditionId) {
            case IterTerminationCondition.TERMINATION_CONDITION_ID:
                return new IterTerminationCondition(getNumberIterations());
            default:
                throw new UnsupportedOperationException("Unknown termination condition: "
                        + terminationConditionId);
        }
    }

    private int getNumEvents() {
        String numEvents = properties.getProperty(GeneticAlgorithmBuilder.MAX_NUM_EVENTS_KEY);
        if (numEvents == null) {
            if (useDefaults) {
                return org.mate.Properties.MAX_NUM_EVENTS;
            } else {
                throw new IllegalArgumentException(
                        "Without using defaults: maximum number of events not specified");
            }
        } else {
            return Integer.valueOf(numEvents);
        }
    }

    private int getNumberIterations() {
        String numberIterations
                = properties.getProperty(GeneticAlgorithmBuilder.NUMBER_ITERATIONS_KEY);
        if (numberIterations == null) {
            if (useDefaults) {
                return org.mate.Properties.EVO_ITERATIONS_NUMBER;
            } else {
                throw new IllegalArgumentException(
                        "Without using defaults: number of iterations not specified");
            }
        } else {
            return Integer.valueOf(numberIterations);
        }
    }
}
