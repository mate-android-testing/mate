package org.mate;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.NSGAII;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.IterTerminationCondition;
import org.mate.model.TestCase;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATENSGAII {


    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("NSGA-II algorithm");

        MATE mate = new MATE();

        IGeneticAlgorithm<TestCase> nsga = new GeneticAlgorithmBuilder()
                .withAlgorithm(NSGAII.ALGORITHM_NAME)
                .withChromosomeFactory(AndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID)
                .withSelectionFunction(SelectionFunction.FITNESS_SELECTION)
                .withMutationFunction(CutPointMutationFunction.MUTATION_FUNCTION_ID)
                .withFitnessFunction(FitnessFunction.NUMBER_OF_ACTIVITIES)
                .withFitnessFunction(FitnessFunction.NUMBER_OF_STATES)
                .withTerminationCondition(IterTerminationCondition.TERMINATION_CONDITION_ID)
                .build();

        mate.testApp(nsga);
    }
}
