package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Mio;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.TestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.model.TestCase;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEMio {


    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("Mio implementation");

        MATE mate = new MATE();

        final GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(Mio.ALGORITHM_NAME)
                .withChromosomeFactory(AndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID)
                .withCrossoverFunction(TestCaseMergeCrossOverFunction.CROSSOVER_FUNCTION_ID)
                .withMutationFunction(CutPointMutationFunction.MUTATION_FUNCTION_ID)
                .withSelectionFunction(SelectionFunction.RANDOM_SELECTION)
                .withTerminationCondition(NeverTerminationCondition.TERMINATION_CONDITION_ID)
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .withPSampleRandom(Properties.P_SAMPLE_RANDOM())
                .withFocusedSearchStart(Properties.P_FOCUSED_SEARCH_START());

        List<String> objectives = Registry.getEnvironmentManager().getObjectives(Properties.OBJECTIVE());

        for (String objective : objectives) {
            builder.withFitnessFunction(Properties.FITNESS_FUNCTION(), objective);
        }

        final IGeneticAlgorithm<TestCase> mio = builder.build();

        mate.testApp(mio);
    }
}
