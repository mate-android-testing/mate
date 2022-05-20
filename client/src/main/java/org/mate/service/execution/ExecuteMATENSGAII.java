package org.mate.service.execution;


import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;

public class ExecuteMATENSGAII {


    public static void run(String packageName, Context context) {

        MATELog.log_acc("Starting Evolutionary Search...");
        MATELog.log_acc("NSGA-II algorithm");

        MATE mate = new MATE(packageName, context);

        IGeneticAlgorithm nsga = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.NSGAII)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withSelectionFunction(SelectionFunction.CROWDED_TOURNAMENT_SELECTION)
                .withCrossoverFunction(Properties.CROSSOVER_FUNCTION())
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withFitnessFunction(FitnessFunction.NUMBER_OF_ACTIVITIES)
                .withFitnessFunction(FitnessFunction.TEST_LENGTH)
                .withFitnessFunction(FitnessFunction.NUMBER_OF_CRASHES)
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .build();

        mate.testApp(nsga);
    }
}
