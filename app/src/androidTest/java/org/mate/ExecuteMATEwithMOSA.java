package org.mate;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.MOSA;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.TestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.selection.RandomSelectionFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.model.TestCase;
import org.mate.utils.Coverage;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEwithMOSA {

    @Test
    public void useAppContext() throws Exception {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc(MOSA.ALGORITHM_NAME + " algorithm");

        MATE mate = new MATE();

        if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {
            // init the CFG
            boolean isInit = Registry.getEnvironmentManager().initCFG();

            if (!isInit) {
                MATE.log("Couldn't initialise CFG! Aborting.");
                throw new IllegalStateException("Graph initialisation failed!");
            }
        }

        final GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(MOSA.ALGORITHM_NAME)
                .withChromosomeFactory(AndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID)
                .withCrossoverFunction(TestCaseMergeCrossOverFunction.CROSSOVER_FUNCTION_ID)
                .withMutationFunction(CutPointMutationFunction.MUTATION_FUNCTION_ID)
                .withSelectionFunction(RandomSelectionFunction.SELECTION_FUNCTION_ID) //todo: use better selection function
                .withTerminationCondition(NeverTerminationCondition.TERMINATION_CONDITION_ID)
                .withPopulationSize(Properties.MOSA_POPULATION_SIZE())
                .withBigPopulationSize(Properties.MOSA_BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUM_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER());

        // get the set of branches (branch == objective)
        List<String> branches = Registry.getEnvironmentManager().getBranches();

        // if there are no branches, we can stop
        if (branches.isEmpty()) {
            throw new IllegalStateException("No branches available! Aborting.");
        }

        MATE.log("Branches: " + branches);

        // we need to associate with each branch a fitness function
        for (String branch : branches) {
            builder.withFitnessFunction(BranchDistanceFitnessFunctionMultiObjective.FITNESS_FUNCTION_ID, branch);
        }

        final IGeneticAlgorithm<TestCase> mosa = builder.build();

        mate.testApp(mosa);

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE
                && Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {

            // store coverage of test case interrupted by timeout
            Registry.getEnvironmentManager().storeCoverageData(Properties.COVERAGE(),
                    "lastIncompleteTestCase", null);

            // get combined coverage
            MATE.log_acc("Total coverage: "
                    + Registry.getEnvironmentManager()
                    .getCombinedCoverage(Properties.COVERAGE()));
        }
    }
}
