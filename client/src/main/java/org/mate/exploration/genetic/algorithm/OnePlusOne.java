package org.mate.exploration.genetic.algorithm;

import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;

import java.util.List;

/**
 * Provides an implementation of a 1+1 genetic algorithm. This algorithm initially creates a single
 * chromosome. Then a single offspring is sampled by mutation and the chromosome with the better
 * fitness value is kept in the population. This procedure is repeated until the termination condition
 * is met.
 *
 * @param <T> The type of the chromosomes.
 */
public class OnePlusOne<T> extends GeneticAlgorithm<T> {

    /**
     * Initialises the 1+1 genetic algorithm with the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param mutationFunction The used mutation function.
     * @param fitnessFunctions The used fitness functions. Only a single fitness function is used here.
     * @param terminationCondition The used termination condition.
     */
    public OnePlusOne(IChromosomeFactory<T> chromosomeFactory,
                      IMutationFunction<T> mutationFunction,
                      List<IFitnessFunction<T>> fitnessFunctions,
                      ITerminationCondition terminationCondition) {
        super(chromosomeFactory,
                null,
                null,
                mutationFunction,
                fitnessFunctions,
                terminationCondition,
                1,
                1,
                0,
                1);
    }

    /**
     * In 1+1 a single offspring is sampled by mutation. If the offspring has a better fitness
     * than the parent, the offspring is kept in the population and the parent is removed or
     * vice versa.
     */
    @Override
    public void evolve() {

        MATELog.log_acc("Creating population #" + (currentGenerationNumber + 1));

        // sample a single offspring by mutation
        IChromosome<T> offspring = mutationFunction.mutate(population.get(0));
        population.add(offspring);

        // evaluate fitness
        IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);
        double compared = fitnessFunction.getNormalizedFitness(population.get(0))
                - fitnessFunction.getNormalizedFitness(population.get(1));

        logCurrentFitness();

        // discard the worse chromosome
        if (fitnessFunction.isMaximizing()) {
            population.remove(compared > 0 ? 1 : 0);
        } else {
            population.remove(compared < 0 ? 1 : 0);
        }

        currentGenerationNumber++;
    }
}
