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
                .withMutationFunctions(Properties.MUTATION_FUNCTIONS())
                .withFitnessFunctions(Properties.FITNESS_FUNCTIONS())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .build();

        mate.testApp(onePlusOne);
    }
}
