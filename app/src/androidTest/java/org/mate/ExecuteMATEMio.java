package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.model.TestCase;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEMio {


    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("MIO algorithm");

        MATE mate = new MATE();

        GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.MIO)
                .withChromosomeFactory(ChromosomeFactory.ANDROID_RANDOM_CHROMOSOME_FACTORY)
                .withMutationFunction(MutationFunction.TEST_CASE_CUT_POINT_MUTATION)
                .withTerminationCondition(TerminationCondition.NEVER_TERMINATION)
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .withPSampleRandom(Properties.P_SAMPLE_RANDOM())
                .withFocusedSearchStart(Properties.P_FOCUSED_SEARCH_START())
                .withMutationRate(Properties.MUTATION_RATE());

        List<String> objectives = Registry.getEnvironmentManager().getObjectives(Properties.OBJECTIVE());

        for (String objective : objectives) {
            builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION(), objective);
        }

        final IGeneticAlgorithm<TestCase> mio = builder.build();

        mate.testApp(mio);
    }
}
