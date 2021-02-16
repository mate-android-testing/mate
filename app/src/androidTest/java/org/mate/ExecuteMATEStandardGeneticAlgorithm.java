package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.StandardGeneticAlgorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.model.TestCase;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEStandardGeneticAlgorithm {


    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("StandardGeneticAlgorithm implementation");

        MATE mate = new MATE();

        final IGeneticAlgorithm<TestCase> genericGA = new GeneticAlgorithmBuilder()
                .withAlgorithm(StandardGeneticAlgorithm.ALGORITHM_NAME)
                .withChromosomeFactory(AndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID)
                .withSelectionFunction(SelectionFunction.FITNESS_PROPORTIONATE_SELECTION)
                .withCrossoverFunction(CrossOverFunction.TEST_CASE_MERGE_CROSS_OVER)
                .withMutationFunction(MutationFunction.TEST_CASE_CUT_POINT_MUTATION)
                .withFitnessFunction(FitnessFunction.NUMBER_OF_ACTIVITIES)
                .withTerminationCondition(TerminationCondition.NEVER_TERMINATION)
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .build();

        mate.testApp(genericGA);
    }
}
