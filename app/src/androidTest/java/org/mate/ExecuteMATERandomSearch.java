package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomSearch {

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Random Search GA ....");

        MATE mate = new MATE();

        final IGeneticAlgorithm randomSearch = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.RANDOM_SEARCH)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withFitnessFunction(Properties.FITNESS_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .build();

        mate.testApp(randomSearch);
    }
}
