package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.RandomWalk;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.model.TestCase;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomWalkStateCoverage {
    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Random Walk ...");

        MATE mate = new MATE();

        MATE.log("Starting random walk now ...");

        final GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(RandomWalk.ALGORITHM_NAME)
                .withChromosomeFactory(ChromosomeFactory.ANDROID_RANDOM_CHROMOSOME_FACTORY)
                .withMutationFunction(MutationFunction.TEST_CASE_CUT_POINT_MUTATION)
                .withTerminationCondition(TerminationCondition.NEVER_TERMINATION)
                .withFitnessFunction(FitnessFunction.NUMBER_OF_STATES)
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS());


        final IGeneticAlgorithm<TestCase> randomWalk = builder.build();

        mate.testApp(randomWalk);
    }
}
