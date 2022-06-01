package org.mate.service.execution;

import android.content.Context;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;

public class ExecuteMATEGeneticAlgorithm {

    public static void run(String packageName, Context context) {
        MATELog.log_acc("Starting Genetic Algorithm...");

        MATE mate = new MATE(packageName, context);

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

            int numberOfObjectives
                    = Registry.getEnvironmentManager().getNumberOfObjectives(Properties.OBJECTIVE());

            // we need to associate with each objective (branch, line) a fitness function
            for (int i = 0; i < numberOfObjectives; i++) {
                builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION());
            }
        } else if (Properties.ALGORITHM() == Algorithm.NOVELTY_SEARCH) {
            builder = builder.withFitnessFunction(FitnessFunction.NOVELTY, Properties.OBJECTIVE().name());
        } else {
            builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION());
        }

        final IGeneticAlgorithm genericGA = builder.build();
        mate.testApp(genericGA);
    }
}
