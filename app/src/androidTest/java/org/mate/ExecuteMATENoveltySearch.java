package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.model.TestCase;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATENoveltySearch {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Novelty Search...");

        MATE mate = new MATE();

        final IGeneticAlgorithm<TestCase> noveltySearch = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.NOVELTY_SEARCH)
                .withChromosomeFactory(ChromosomeFactory.ANDROID_RANDOM_CHROMOSOME_FACTORY)
                .withSelectionFunction(SelectionFunction.RANDOM_SELECTION)
                .withMutationFunction(MutationFunction.TEST_CASE_CUT_POINT_MUTATION)
                .withCrossoverFunction(CrossOverFunction.TEST_CASE_MERGE_CROSS_OVER)
                .withFitnessFunction(FitnessFunction.BRANCH_DISTANCE)
                .withTerminationCondition(TerminationCondition.CONDITIONAL_TERMINATION)
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .build();

        mate.testApp(noveltySearch);
    }
}
