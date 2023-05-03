package org.mate.exploration.genetic.selection;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.util.novelty.ChromosomeNoveltyTrace;
import org.mate.model.TestCase;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A selection function for {@link org.mate.exploration.genetic.algorithm.NoveltySearchUsingSOSM}
 * that is based on rank selection.
 */
public class SOSMNoveltyRankSelection implements ISelectionFunction<TestCase> {

    /**
     * Do not call this method, consider {@link #select(List)} instead.
     *
     * @param population The given population.
     * @return Returns an {@link UnsupportedOperationException}.
     */
    @Override
    public List<IChromosome<TestCase>> select(List<IChromosome<TestCase>> population,
                                              List<IFitnessFunction<TestCase>> fitnessFunctions) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    // TODO: Add documentation.
    public List<ChromosomeNoveltyTrace> select(final List<ChromosomeNoveltyTrace> chromosomeNoveltyTraces) {

        // initially all chromosomes represent candidates
        List<ChromosomeNoveltyTrace> candidates = new ArrayList<>(chromosomeNoveltyTraces);
        final int size = Math.min(Properties.DEFAULT_SELECTION_SIZE(), candidates.size());

        /*
         * We shuffle the list that chromosomes with an identical fitness value get a 'fair'
         * chance to be selected. Otherwise, the rank of those chromosomes is fixed by the
         * order they are handed-in to this method.
         */
        Collections.shuffle(candidates, Randomness.getRnd());
        Collections.sort(candidates, Comparator.comparingDouble(ChromosomeNoveltyTrace::novelty));

        int sum = (candidates.size() + 1) * candidates.size() / 2; // = 1 + ... + candidates.size()

        // assign each chromosome in the population its rank and perform roulette-wheel selection
        final List<ChromosomeNoveltyTrace> selection = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            final int rnd = Randomness.getRnd().nextInt(sum);

            ChromosomeNoveltyTrace selected = null;
            int start = 0;
            int rank = 1;
            for (final ChromosomeNoveltyTrace candidate : candidates) {
                final int end = start + rank;

                if (rnd < end) {
                    selected = candidate;
                    break;
                } else {
                    start = end;
                    ++rank;
                }
            }

            assert selected != null;
            selection.add(selected);
            sum -= candidates.size();
            candidates.remove(selected);
        }

        return selection;
    }
}

