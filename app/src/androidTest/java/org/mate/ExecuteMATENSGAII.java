package org.mate;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATENSGAII {


    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("NSGA-II algorithm");

        MATE mate = new MATE();

        IGeneticAlgorithm nsga = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.NSGAII)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withSelectionFunction(SelectionFunction.CROWDED_TOURNAMENT_SELECTION)
                .withCrossoverFunction(Properties.CROSSOVER_FUNCTION())
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withFitnessFunction(FitnessFunction.NUMBER_OF_ACTIVITIES)
                .withFitnessFunction(FitnessFunction.TEST_LENGTH)
                .withFitnessFunction(FitnessFunction.NUMBER_OF_STATES)
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .build();

        mate.testApp(nsga);
    }
}
