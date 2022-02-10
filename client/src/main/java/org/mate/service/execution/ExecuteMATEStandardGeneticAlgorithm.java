package org.mate.service.execution;

import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.model.TestCase;

public class ExecuteMATEStandardGeneticAlgorithm {


    public static void run(String packageName, Context context) {
        MATELog.log_acc("Starting Evolutionary Search...");
        MATELog.log_acc("StandardGeneticAlgorithm implementation");

        MATE mate = new MATE(packageName, context);

        final IGeneticAlgorithm<TestCase> genericGA = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.STANDARD_GA)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withSelectionFunction(Properties.SELECTION_FUNCTION())
                .withCrossoverFunction(Properties.CROSSOVER_FUNCTION())
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withFitnessFunction(Properties.FITNESS_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .build();

        mate.testApp(genericGA);
    }
}
