package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;

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
                .withCrossoverFunctions(Properties.CROSSOVER_FUNCTIONS())
                .withMutationFunctions(Properties.MUTATION_FUNCTIONS())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER());

        if (Properties.ALGORITHM() == Algorithm.MIO || Properties.ALGORITHM() == Algorithm.MOSA) {

            int numberOfObjectives
                    = Registry.getEnvironmentManager().getNumberOfObjectives(Properties.OBJECTIVE());

            // we need to associate with each objective (branch, line) a fitness function
            for (int i = 0; i < numberOfObjectives; i++) {
                builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION());
            for (String objective : objectives) {
                builder = builder.withFitnessFunctions(Properties.FITNESS_FUNCTIONS(), objective);
            }
        } else if (Properties.ALGORITHM() == Algorithm.NOVELTY_SEARCH) {
            FitnessFunction[] novelty = {FitnessFunction.NOVELTY};

            builder = builder.withFitnessFunctions(novelty, Properties.OBJECTIVE().name());
        } else {
            builder = builder.withFitnessFunctions(Properties.FITNESS_FUNCTIONS());
        }

        final IGeneticAlgorithm genericGA = builder.build();
        mate.testApp(genericGA);
    }
}
