package org.mate.exploration.genetic.algorithm;

import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.List;

/**
 * Provides an implementation of the random search algorithm. In random search, a random chromosome
 * is initially sampled. Then, in the evolution process, another random chromosome is sampled
 * and the one with the better fitness is kept while the other one is discarded.
 *
 * @param <T> The type of the chromosomes.
 */
public class RandomSearch<T> extends GeneticAlgorithm<T> {

    /**
     * Initialises the random search algorithm with the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param fitnessFunctions The used fitness functions. Only a single fitness function is used here.
     * @param terminationCondition The used termination condition.
     */
    public RandomSearch(IChromosomeFactory<T> chromosomeFactory,
                        List<IFitnessFunction<T>> fitnessFunctions,
                        ITerminationCondition terminationCondition) {
        super(chromosomeFactory,
                null,
                null,
                null,
                fitnessFunctions,
                terminationCondition,
                1,
                2,
                0,
                0);
    }

    /**
     * In the evolve step of random search, a second random chromosome is created and the one
     * with the better fitness value is kept in the population.
     */
    @Override
    public void evolve() {

        MATELog.log_acc("Creating population #" + (currentGenerationNumber + 1));

        // Add temporary a second random chromosome.
        population.add(chromosomeFactory.createChromosome());

        // Discard old chromosome if not better than new one.
        IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);
        double compared = fitnessFunction.getNormalizedFitness(population.get(0))
                - fitnessFunction.getNormalizedFitness(population.get(1));

        logCurrentFitness();

        if (fitnessFunction.isMaximizing()) {
            population.remove(compared > 0 ? 1 : 0);
        } else {
            population.remove(compared < 0 ? 1 : 0);
        }

        currentGenerationNumber++;
    }

    /**
     * Logs the fitness of the chromosomes in the current population.
     */
    @Override
    protected void logCurrentFitness() {

        for (int i = 0; i < Math.min(fitnessFunctions.size(), 5); i++) {
            MATELog.log_acc("Fitness function " + (i + 1) + ":");
            IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(i);
            for (int j = 0; j < population.size(); j++) {
                IChromosome<T> chromosome = population.get(j);
                MATELog.log_acc("Chromosome " + (j + 1) + " Fitness: "
                        + fitnessFunction.getNormalizedFitness(chromosome));

                if (Properties.COVERAGE() != Coverage.NO_COVERAGE
                        && Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {
                    MATELog.log_acc("Chromosome " + (j + 1) + " Coverage: "
                            + CoverageUtils.getCoverage(Properties.COVERAGE(),
                            chromosome));
                }
            }
        }

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
            MATELog.log_acc("Accumulated Coverage: "
                    + CoverageUtils.getCombinedCoverage(Properties.COVERAGE()));
        }
    }

}
