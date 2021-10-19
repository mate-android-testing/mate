package org.mate.exploration.genetic.selection;

import android.util.Pair;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.NoveltyFitnessFunction;
import org.mate.utils.MapUtils;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
     * @return Returns the population grounded on rank-based selection.
     */
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IChromosome<T>> archive,
                                       int nearestNeighbours, List<IFitnessFunction<T>> fitnessFunctions) {

        MATE.log_acc("Population before selection: " + population);
        MATE.log_acc("Population size before selection: " + population.size());

        List<IChromosome<T>> selection = new ArrayList<>();

        NoveltyFitnessFunction<T> noveltyFitnessFunction = (NoveltyFitnessFunction<T>) fitnessFunctions.get(0);
        List<Double> noveltyVector = noveltyFitnessFunction.getFitness(population, archive, nearestNeighbours);

        Pair<List<Double>, List<Double>> noveltyVectorPair
                = getNoveltyScoresOfPopulationAndArchive(noveltyVector, population.size());

        Map<IChromosome<T>, Double> populationNoveltyScores
                = MapUtils.sortByValue(convertToMap(population, noveltyVectorPair.first));
        MATE.log_acc("Sorted novelty values: " + populationNoveltyScores.values());

        // initially all chromosomes represent candidates
        List<IChromosome<T>> candidates = new LinkedList<>(populationNoveltyScores.keySet());

        // assign each chromosome in the population its rank and perform roulette-wheel selection
        for (int i = 0; i < populationNoveltyScores.size(); i++) {

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

        MATE.log_acc("Population after selection: " + selection);
        MATE.log_acc("Population size after selection: " + selection.size());
        return selection;
    }

    /**
     * Splits the novelty vector into the scores belonging to the population and the archive.
     *
     * @param noveltyVector The novelty vector.
     * @param populationSize The population size.
     * @return Returns a pair where the first entry refers to the novelty scores of the population
     *          and the second entry refers to the novelty scores of the archive.
     */
    private Pair<List<Double>, List<Double>> getNoveltyScoresOfPopulationAndArchive(
            List<Double> noveltyVector, int populationSize) {
        List<Double> populationNoveltyScores = noveltyVector.subList(0, populationSize);
        List<Double> archiveNoveltyScores = noveltyVector.subList(populationSize, noveltyVector.size());
        return new Pair<>(populationNoveltyScores, archiveNoveltyScores);
    }

    /**
     * Converts a key and value list into a corresponding map. This assumes that the number of
     * keys and values are identical.
     *
     * @param keys The list of keys.
     * @param values The list of values.
     * @return Returns a map with the respective key-value associations.
     */
    private Map<IChromosome<T>, Double> convertToMap(List<IChromosome<T>> keys, List<Double> values) {

        if (keys.size() != values.size()) {
            throw new IllegalStateException("Not the same number of keys and values!");
        }

        Map<IChromosome<T>, Double> map = new HashMap<>();
        Iterator<IChromosome<T>> i1 = keys.iterator();
        Iterator<Double> i2 = values.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            map.put(i1.next(), i2.next());
        }
        return map;
    }
}
