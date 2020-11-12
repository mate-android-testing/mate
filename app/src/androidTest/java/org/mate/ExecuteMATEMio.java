package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Mio;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.TestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.selection.RandomSelectionFunction;
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
                .withSelectionFunction(RandomSelectionFunction.SELECTION_FUNCTION_ID) //todo: use better selection function
                .withTerminationCondition(NeverTerminationCondition.TERMINATION_CONDITION_ID)
                .withPopulationSize(Properties.EVOLUTIONARY_SEARCH_POPULATION_SIZE())
                .withBigPopulationSize(Properties.EVOLUTIONARY_SEARCH_BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUM_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .withPSampleRandom(Properties.P_SAMPLE_RANDOM())
                .withFocusedSearchStart(Properties.P_FOCUSED_SEARCH_START());

        // TODO: move to constructor but ensure that emulator is properly initialized before
        if (Properties.GRAPH_TYPE() != null) {
            // initialise a graph
            MATE.log_acc("Initialising graph!");
            Registry.getEnvironmentManager().initGraph();
        }

        List<String> objectives = Registry.getEnvironmentManager().getObjectives(Properties.OBJECTIVE());

        for (String objective : objectives) {
            // TODO: use property 'FITNESS_FUNCTION'
            builder.withFitnessFunction(LineCoveredPercentageFitnessFunction.FITNESS_FUNCTION_ID, objective);
        }

        final IGeneticAlgorithm<TestCase> mio = builder.build();

        mate.testApp(mio);
    }
}
