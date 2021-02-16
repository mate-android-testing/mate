package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.model.TestCase;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomSearchGA {

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Random Search GA ....");

        MATE mate = new MATE();

        final IGeneticAlgorithm<TestCase> randomSearchGA = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.RANDOM_SEARCH)
                .withChromosomeFactory(ChromosomeFactory.ANDROID_RANDOM_CHROMOSOME_FACTORY)
                .withFitnessFunction(Properties.FITNESS_FUNCTION())
                .withTerminationCondition(TerminationCondition.CONDITIONAL_TERMINATION)
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .build();

        mate.testApp(randomSearchGA);
    }
}
