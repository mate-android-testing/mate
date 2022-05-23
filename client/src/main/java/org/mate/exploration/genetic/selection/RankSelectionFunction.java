package org.mate.exploration.genetic.selection;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.core.GAUtils;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a rank-based selection function that returns {@link Properties#DEFAULT_SELECTION_SIZE()}
 * chromosomes.
 *
 * @param <T> The type of the chromosomes.
 */
public class RankSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Performs a rank-based selection, i.e. the chromosomes in the population are first ranked
     * based on their fitness and afterwards a selection as in roulette-wheel selection is performed.
     *
     * @param population The current population.
     * @param fitnessFunctions The list of fitness functions. Only the first one is used.
     * @return Returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {

        final IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);

        List<IChromosome<T>> selection = new ArrayList<>();
        List<IChromosome<T>> candidates = new LinkedList<>(population);
        int size = Math.min(Properties.DEFAULT_SELECTION_SIZE(), candidates.size());

        for (int i = 0; i < size; i++) {

            /*
            * We shuffle the list that chromosomes with an identical fitness value get a 'fair'
            * chance to be selected. Otherwise, the rank of those chromosomes is fixed by the
            * order they are handed-in to this method.
             */
            Randomness.shuffleList(candidates);

            // sort the chromosomes based on their fitness in order to assign a rank
            List<IChromosome<T>> sorted = GAUtils.sortByFitness(candidates, fitnessFunction);

            /*
             * Constructs the roulette wheel. Each chromosome is assigned a range proportionate
             * to its rank. The first chromosome has the worst rank 1, followed by the second
             * chromosome with rank 2 and so on.
             */
            int sum = 0;
            int rank = 1;

            for (IChromosome<T> chromosome : sorted) {
                sum += rank;
                rank++;
            }

            /*
             * The maximal spectrum of the roulette wheel is defined by the range [0,sum).
             * Thus, we pick a random number in that spectrum. The candidate that covers the
             * random number represents the selected chromosome.
             */
            int rnd = Randomness.getRnd().nextInt(sum);
            IChromosome<T> selected = null;

            int start = 0;
            rank = 1;

            for (IChromosome<T> chromosome : sorted) {

                int end = start + rank;

                if (rnd < end) {
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
