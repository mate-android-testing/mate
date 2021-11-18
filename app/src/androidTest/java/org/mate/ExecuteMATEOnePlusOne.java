package org.mate;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;


@RunWith(AndroidJUnit4.class)
public class ExecuteMATEOnePlusOne {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("One-plus-one algorithm");

        MATE mate = new MATE();

        final IGeneticAlgorithm onePlusOne = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.ONE_PLUS_ONE)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withSelectionFunction(Properties.SELECTION_FUNCTION())
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withFitnessFunction(Properties.FITNESS_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .build();

        mate.testApp(onePlusOne);
    }
}
