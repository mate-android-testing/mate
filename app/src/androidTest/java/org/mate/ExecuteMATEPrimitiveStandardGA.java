package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.StandardGeneticAlgorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.PrimitiveAndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.PrimitiveTestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.fitness.LineCoverageFitnessFunction;
import org.mate.exploration.genetic.mutation.PrimitiveTestCaseShuffleMutationFunction;
import org.mate.exploration.genetic.selection.FitnessProportionateSelectionFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.model.TestCase;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEPrimitiveStandardGA {

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("Primitive StandardGeneticAlgorithm implementation");

        MATE mate = new MATE();

        final IGeneticAlgorithm<TestCase> genericGA = new GeneticAlgorithmBuilder()
                .withAlgorithm(StandardGeneticAlgorithm.ALGORITHM_NAME)
                .withChromosomeFactory(PrimitiveAndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID)
                .withSelectionFunction(FitnessProportionateSelectionFunction.SELECTION_FUNCTION_ID)
                .withCrossoverFunction(PrimitiveTestCaseMergeCrossOverFunction.CROSSOVER_FUNCTION_ID)
                .withMutationFunction(PrimitiveTestCaseShuffleMutationFunction.MUTATION_FUNCTION_ID)
                .withFitnessFunction(LineCoverageFitnessFunction.FITNESS_FUNCTION_ID)
                .withTerminationCondition(NeverTerminationCondition.TERMINATION_CONDITION_ID)
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .build();

        mate.testApp(genericGA);
    }
}
