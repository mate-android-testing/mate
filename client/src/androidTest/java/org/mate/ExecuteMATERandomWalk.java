package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomWalk {

        @Test
        public void useAppContext() {

            MATE.log_acc("Starting Random Walk ....");
            MATE mate = new MATE();

            final GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                    .withAlgorithm(Algorithm.RANDOM_WALK)
                    .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                    .withMutationFunction(Properties.MUTATION_FUNCTION())
                    .withFitnessFunction(Properties.FITNESS_FUNCTION())
                    .withTerminationCondition(Properties.TERMINATION_CONDITION());

            final IGeneticAlgorithm randomWalk = builder.build();
            mate.testApp(randomWalk);
        }
}
