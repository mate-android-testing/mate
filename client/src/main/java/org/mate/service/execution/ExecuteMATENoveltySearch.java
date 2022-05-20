package org.mate.service.execution;

import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;

public class ExecuteMATENoveltySearch {

    public static void run(String packageName, Context context) {

        MATELog.log_acc("Starting Novelty Search...");

        MATE mate = new MATE(packageName, context);

        final IGeneticAlgorithm noveltySearch = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.NOVELTY_SEARCH)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withSelectionFunction(SelectionFunction.NOVELTY_RANK_SELECTION)
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withCrossoverFunction(Properties.CROSSOVER_FUNCTION())
                .withFitnessFunction(FitnessFunction.NOVELTY, Properties.OBJECTIVE().name())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .withNoveltyThreshold(Properties.NOVELTY_THRESHOLD())
                .withArchiveLimit(Properties.ARCHIVE_LIMIT())
                .withNearestNeighbours(Properties.NEAREST_NEIGHBOURS())
                .build();

        mate.testApp(noveltySearch);
    }
}
