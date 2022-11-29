package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.model.TestCase;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEPrimitiveStandardGA {

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("Primitive StandardGeneticAlgorithm implementation");

        MATE mate = new MATE();

        final IGeneticAlgorithm<TestCase> genericGA = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.STANDARD_GA)
                .withChromosomeFactory(ChromosomeFactory.PRIMITIVE_ANDROID_RANDOM_CHROMOSOME_FACTORY)
                .withSelectionFunction(Properties.SELECTION_FUNCTION())
                .withCrossoverFunction(CrossOverFunction.PRIMITIVE_TEST_CASE_MERGE_CROSS_OVER)
                .withMutationFunction(MutationFunction.PRIMITIVE_SHUFFLE_MUTATION)
                .withFitnessFunction(Properties.FITNESS_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .build();

        mate.testApp(genericGA);
    }
}
