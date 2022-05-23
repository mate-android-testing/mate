package org.mate.exploration.genetic.selection;

import android.util.Pair;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A selection function for {@link org.mate.exploration.genetic.algorithm.NoveltySearch} that
 * is based on rank selection.
 *
 * @param <T> The type of the chromosome.
 */
public class NoveltyRankSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Do not call this method, consider {@link #select(List)}  instead.
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
     * Performs a rank-based selection, i.e. the chromosomes in the population are first ranked
     * based on their novelty and afterwards a selection as in roulette-wheel selection is performed.
     *
     * @param noveltyPairs A list of pairs where each pair associates a chromosome with its novelty.
     *                     This list contains a pair for each chromosome in the current population.
     * @return Returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
     */
    public List<IChromosome<T>> select(List<Pair<IChromosome<T>, Double>> noveltyPairs) {

        List<IChromosome<T>> selection = new ArrayList<>();

        // initially all chromosomes represent candidates
        List<Pair<IChromosome<T>, Double>> candidates = new LinkedList<>(noveltyPairs);
        int size = Math.min(Properties.DEFAULT_SELECTION_SIZE(), candidates.size());

        // assign each chromosome in the population its rank and perform roulette-wheel selection
        for (int i = 0; i < size; i++) {

            /*
             * We shuffle the list that chromosomes with an identical fitness value get a 'fair'
             * chance to be selected. Otherwise, the rank of those chromosomes is fixed by the
             * order they are handed-in to this method.
             */
            Randomness.shuffleList(candidates);

            // sort the chromosomes based on their novelty in order to assign a rank
            Collections.sort(candidates, (o1, o2) -> o1.second.compareTo(o2.second));

            // evaluate the maximal spectrum of the roulette wheel
            int sum = 0;
            int rank = 1;

            for (Pair<IChromosome<T>, Double> candidate : candidates) {
                sum += rank;
                rank++;
            }

            /*
             * The maximal spectrum of the roulette wheel is defined by the range [0,sum).
             * Thus, we pick a random number in that spectrum. The candidate that covers the
             * random number represents the selected chromosome.
             */
            int rnd = Randomness.getRnd().nextInt(sum);
            Pair<IChromosome<T>, Double> selected = null;

            int start = 0;
            rank = 1;

            for (Pair<IChromosome<T>, Double> candidate : candidates) {

                int end = start + rank;

                if (rnd < end) {
                    selected = candidate;
                    break;
                } else {
                    start = end;
                    rank++;
                }
            }

            selection.add(selected.first);

            // remove selected chromosome from roulette wheel
            candidates.remove(selected);
        }

        return selection;
    }
}
