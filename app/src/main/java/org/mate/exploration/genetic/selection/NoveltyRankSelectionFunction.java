package org.mate.exploration.genetic.selection;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A selection function for {@link org.mate.exploration.genetic.algorithm.NoveltySearch} that
 * is based on rank selection.
 *
 * @param <T> The type of the chromosome.
 */
public class NoveltyRankSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Do not call this method, consider {@link #select(List, Map)} instead.
     *
     * @param population The given population.
     * @return Returns an {@link UnsupportedOperationException}.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population,
                                       List<IFitnessFunction<T>> fitnessFunctions) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * Performs a rank-based selection grounded on the novelty scores.
     *
     * @param population The current population.
     * @param noveltyScores A mapping of chromosome to its novelty.
     * @return Returns the selected chromosomes.
     */
    public List<IChromosome<T>> select(List<IChromosome<T>> population,
                                       Map<IChromosome<T>, Double> noveltyScores) {

        List<IChromosome<T>> selection = new ArrayList<>();

        // initially all chromosomes represent candidates
        List<IChromosome<T>> candidates = new LinkedList<>(population);

        // assign each chromosome in the population its rank and perform roulette-wheel selection
        for (int i = 0; i < population.size(); i++) {

            /*
             * We shuffle the list that chromosomes with an identical fitness value get a 'fair'
             * chance to be selected. Otherwise, the rank of those chromosomes is fixed by the
             * order they are handed-in to this method.
             */
            Randomness.shuffleList(candidates);

            // sort the chromosomes based on their fitness (novelty) in order to assign a rank
            Collections.sort(candidates, (o1, o2) -> noveltyScores.get(o1).compareTo(noveltyScores.get(o2)));

            // evaluate the maximal spectrum of the roulette wheel
            int sum = 0;
            int rank = 1;

            for (IChromosome<T> chromosome : candidates) {
                sum += rank;
                rank++;
            }

            /*
             * The maximal spectrum of the roulette wheel is defined by the range [0.0,sum].
             * Thus, we pick a random number in that spectrum. The candidate that covers the
             * random number represents the selected chromosome.
             */
            int rnd = Randomness.getRnd().nextInt(sum + 1);
            IChromosome<T> selected = null;

            int start = 0;
            rank = 1;

            for (IChromosome<T> chromosome : candidates) {

                int end = start + rank;

                if (rnd <= end) {
                    selected = chromosome;
                    break;
                } else {
                    start = end;
                    rank++;
                }
            }

            selection.add(selected);

            // remove selected chromosome from roulette wheel
            candidates.remove(selected);
        }

        return selection;
    }
}
