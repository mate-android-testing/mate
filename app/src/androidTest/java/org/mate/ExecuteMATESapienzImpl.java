package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.NSGAII;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.AndroidSuiteRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.model.TestSuite;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATESapienzImpl {


    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("Sapienz implementation");

        MATE mate = new MATE();

        final IGeneticAlgorithm<TestSuite> sapienz =
                new GeneticAlgorithmBuilder()
                .withAlgorithm(NSGAII.ALGORITHM_NAME)
                .withChromosomeFactory(AndroidSuiteRandomChromosomeFactory.CHROMOSOME_FACTORY_ID)
                .withCrossoverFunction(CrossOverFunction.TEST_SUITE_UNIFORM_CROSS_OVER)
                .withSelectionFunction(SelectionFunction.RANDOM_SELECTION)
                .withMutationFunction(MutationFunction.SAPIENZ_MUTATION)
                .withFitnessFunction(FitnessFunction.LINE_COVERAGE)
                .withFitnessFunction(FitnessFunction.NUMBER_OF_CRASHES)
                .withFitnessFunction(FitnessFunction.TEST_LENGTH)
                .withTerminationCondition(NeverTerminationCondition.TERMINATION_CONDITION_ID)
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPInnerMutate(Properties.P_INNER_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .withNumTestCases(Properties.NUMBER_TESTCASES())
                .build();

        mate.testApp(sapienz);
    }
}
