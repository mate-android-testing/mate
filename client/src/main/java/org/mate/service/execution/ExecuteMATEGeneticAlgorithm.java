package org.mate.service.execution;

import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

import java.util.List;

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

            List<String> objectives = Registry.getEnvironmentManager()
                    .getObjectives(Properties.OBJECTIVE());

            // we need to associate with each objective (branch, line) a fitness function
            for (String objective : objectives) {
                builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION(), objective);
            }
        } else {
            builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION());
        }

        final IGeneticAlgorithm genericGA = builder.build();
        mate.testApp(genericGA);
    }
}
