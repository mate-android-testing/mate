package org.mate.exploration.genetic.selection;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.NoveltyFitnessFunction;

import java.util.List;

/**
 * A selection function for {@link org.mate.exploration.genetic.algorithm.NoveltySearch} that
 * is based on rank selection.
 *
 * @param <T> The type of the chromosome.
 */
public class NoveltyRankSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Since the selection is based on the novelty scores, the underlying fitness function needs
     * to be invoked. The invocation requires an additional parameter, thus we cannot use the
     * default select() method, but rather need to provide a customized select() method. See for
     * information {@link #select(List, List, int, List)}.
     *
     * @param population The given population.
     * @return Returns an {@link UnsupportedOperationException}.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * Performs a rank-based selection grounded on novelty scores.
     *
     * @param population The current population.
     * @param archive The current archive.
     * @param nearestNeighbours The number of the nearest neighbours k.
     * @param fitnessFunctions The list of fitness functions. Only the first fitness function is
     *                         used here.
     * @return Returns the population ordered based on the rank of each individual.
     */
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IChromosome<T>> archive,
                                       int nearestNeighbours, List<IFitnessFunction<T>> fitnessFunctions) {

        NoveltyFitnessFunction<T> noveltyFitnessFunction = (NoveltyFitnessFunction<T>) fitnessFunctions.get(0);
        List<Double> noveltyVector = noveltyFitnessFunction.getFitness(population, archive, nearestNeighbours);

        // TODO: assign each chromosome in the population its rank and choose as in roulette-wheel then

        throw new UnsupportedOperationException("Do not call this method!");
    }
}
