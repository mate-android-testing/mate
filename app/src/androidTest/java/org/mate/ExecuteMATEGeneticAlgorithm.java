package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEGeneticAlgorithm {

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Genetic Algorithm...");

        MATE mate = new MATE();

        GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(Properties.ALGORITHM())
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withSelectionFunction(Properties.SELECTION_FUNCTION())
                .withCrossoverFunction(Properties.CROSSOVER_FUNCTION())
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER());

        if (Properties.ALGORITHM() == Algorithm.MIO || Properties.ALGORITHM() == Algorithm.MOSA) {

            List<String> objectives = Registry.getEnvironmentManager()
                    .getObjectives(Properties.OBJECTIVE());

            // we need to associate with each objective (branch, line) a fitness function
            for (String objective : objectives) {
                builder = builder.withFitnessFunctions(Properties.FITNESS_FUNCTIONS(), objective);
            }
        } else if (Properties.ALGORITHM() == Algorithm.NOVELTY_SEARCH) {
            builder = builder.withFitnessFunction(FitnessFunction.NOVELTY, Properties.OBJECTIVE().name());
        } else {
            builder = builder.withFitnessFunctions(Properties.FITNESS_FUNCTIONS());
        }

        final IGeneticAlgorithm genericGA = builder.build();
        mate.testApp(genericGA);
    }
}
