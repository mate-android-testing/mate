package org.mate;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.model.TestCase;


@RunWith(AndroidJUnit4.class)
public class ExecuteMATEOnePlusOne {


    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("One-plus-one (new) algorithm");

        MATE mate = new MATE();

        final IGeneticAlgorithm<TestCase> onePlusOne = new GeneticAlgorithmBuilder()
                .withAlgorithm(org.mate.exploration.genetic.algorithm.OnePlusOne.ALGORITHM_NAME)
                .withChromosomeFactory(AndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID)
                .withSelectionFunction(FitnessSelectionFunction.SELECTION_FUNCTION_ID)
                .withMutationFunction(CutPointMutationFunction.MUTATION_FUNCTION_ID)
                .withFitnessFunction(BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID)
                .withTerminationCondition(ConditionalTerminationCondition.TERMINATION_CONDITION_ID)
                .withMaxNumEvents(Properties.MAX_NUM_EVENTS())
                .build();

        mate.testApp(onePlusOne);

        //Report
        //Vector<TestCase> ts = new Vector<>(OnePlusOne.testsuite.values());
        //MATE.log_acc("Final Report: test cases number = "+ts.size());

        //MATE.log_acc(OnePlusOne.coverageArchive.keySet().toString());
        //MATE.log_acc("Visited GUI States number = "+ OnePlusOne.coverageArchive.keySet().size());
        //MATE.log_acc("Covered GUI States = "+ OnePlusOne.testsuite.get("0").getVisitedStates().size());
    }
}
