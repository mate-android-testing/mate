package org.mate.service.execution;


import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;


public class ExecuteMATEOnePlusOne {

    public static void run(String packageName) {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("One-plus-one algorithm");

        MATE mate = new MATE(packageName);

        final IGeneticAlgorithm onePlusOne = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.ONE_PLUS_ONE)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withFitnessFunction(Properties.FITNESS_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .build();

        mate.testApp(onePlusOne);
    }
}
