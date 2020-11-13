package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.NSGAII;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.AndroidSuiteRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.UniformSuiteCrossoverFunction;
import org.mate.exploration.genetic.fitness.AmountCrashesFitnessFunction;
import org.mate.exploration.genetic.fitness.StatementCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.TestLengthFitnessFunction;
import org.mate.exploration.genetic.mutation.SapienzSuiteMutationFunction;
import org.mate.exploration.genetic.selection.RandomSelectionFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.model.TestSuite;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATESapienzImpl {


    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("Sapienz implementation");

        MATE mate = new MATE();

        MATE.log_acc("Activities");
        for (String s : Registry.getEnvironmentManager().getActivityNames()) {
            MATE.log_acc("\t" + s);
        }

        final IGeneticAlgorithm<TestSuite> sapienz =
                new GeneticAlgorithmBuilder()
                .withAlgorithm(NSGAII.ALGORITHM_NAME)
                .withChromosomeFactory(AndroidSuiteRandomChromosomeFactory.CHROMOSOME_FACTORY_ID)
                .withCrossoverFunction(UniformSuiteCrossoverFunction.CROSSOVER_FUNCTION_ID)
                .withSelectionFunction(RandomSelectionFunction.SELECTION_FUNCTION_ID)
                .withMutationFunction(SapienzSuiteMutationFunction.MUTATION_FUNCTION_ID)
                .withFitnessFunction(StatementCoverageFitnessFunction.FITNESS_FUNCTION_ID)
                .withFitnessFunction(AmountCrashesFitnessFunction.FITNESS_FUNCTION_ID)
                .withFitnessFunction(TestLengthFitnessFunction.FITNESS_FUNCTION_ID)
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
