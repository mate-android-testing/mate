package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.RandomWalk;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.StatementCoverageFitnessFunction;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.model.TestCase;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomWalk {

        @Test
        public void useAppContext() throws Exception {
            MATE.log_acc("Starting Random Walk ....");

            MATE mate = new MATE();

            MATE.log("Starting random walk now ...");

            final GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                    .withAlgorithm(RandomWalk.ALGORITHM_NAME)
                    .withChromosomeFactory(AndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID)
                    .withMutationFunction(CutPointMutationFunction.MUTATION_FUNCTION_ID)
                    .withTerminationCondition(NeverTerminationCondition.TERMINATION_CONDITION_ID)
                    .withFitnessFunction(StatementCoverageFitnessFunction.FITNESS_FUNCTION_ID)
                    .withMaxNumEvents(Properties.MAX_NUM_EVENTS());


            final IGeneticAlgorithm<TestCase> randomWalk = builder.build();

            mate.testApp(randomWalk);
        }
}
