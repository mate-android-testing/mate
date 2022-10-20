package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATENoveltySearch {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Novelty Search...");

        MATE mate = new MATE();

        FitnessFunction[] novelty = {FitnessFunction.NOVELTY};

        final IGeneticAlgorithm noveltySearch = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.NOVELTY_SEARCH)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withSelectionFunction(SelectionFunction.NOVELTY_RANK_SELECTION)
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withCrossoverFunction(Properties.CROSSOVER_FUNCTION())
                .withFitnessFunctions(novelty, Properties.OBJECTIVE().name())
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
