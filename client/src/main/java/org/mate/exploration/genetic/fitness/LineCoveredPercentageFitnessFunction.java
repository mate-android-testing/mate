package org.mate.exploration.genetic.fitness;

import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a fitness function that aims to maximise a hand-crafted line metric. This fitness function
 * is intended to be used in the context of multi/many-objective search. The AUT needs to be manually
 * instrumented with Jacoco.
 */
public class LineCoveredPercentageFitnessFunction<T> implements IFitnessFunction<T> {

    /**
     * The cache is basically a two-dimensional array compacted to one dimension, i.e. the dimensions
     * are joined together in a linear fashion. We could say that we joined together multiple sub
     * lists, where each sub list represents the fitness values for a given chromosome. Each sub
     * list has {@link #numberOfLines} entries.
     *
     * A visual representation of the cache would look as follows:
     *
     * chromosome 1      chromosome 2     ...  chromosome n
     * [[line 1 to n], [line 1 to n], ..., [line 1 to n]]  (sub lists)
     *
     * By having the chromosome index, see {@link #chromosomeToCacheIndex}, and the number of lines,
     * see {@link #numberOfLines}, we can compute the starting position of the chromosome in the
     * cache. By adding the line index, we can retrieve the fitness value from the cache for the
     * given chromosome and objective (line).
     */
    private static final List<Float> cache = new ArrayList<>();

    /**
     * Maps a chromosome to its index in the cache.
     */
    private static final Map<IChromosome, Integer> chromosomeToCacheIndex = new HashMap<>();

    /**
     * We keep track of the used cache indices. Since we clean the cache from time to time, certain
     * indices get available again, which we re-use when assigning a new index, see
     * {@link #assignNewCacheIndex(IChromosome)}.
     */
    private static final BitSet usedCacheIndices = new BitSet();

    /**
     * Tracks the total number of lines / fitness functions.
     */
    private static int numberOfLines;

    /**
     * Represents the index of the n-th line / fitness function.
     */
    private final int index;

    /**
     * Initialises the fitness function with the given index of the branch.
     *
     * @param index The index of the n-th branch / fitness function.
     */
    public LineCoveredPercentageFitnessFunction(int index) {
        this.index = index;
        numberOfLines++;
    }

    /**
     * Retrieves the line metric value for the given chromosome.
     *
     * @param chromosome The chromosome for which we want to retrieve its fitness value.
     * @return Returns the line metric value for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {

        Integer cacheIndex = chromosomeToCacheIndex.get(chromosome);

        if (cacheIndex == null) {
            // the chromosome hasn't been cached yet
            cacheIndex = assignNewCacheIndex(chromosome);

            // retrieve the fitness value for every single line
            final List<Float> lineCoverageVector
                    = FitnessUtils.getLinePercentageVector(chromosome, numberOfLines);

            // describes the starting position of the chromosome in the cache
            final int baseIndex = cacheIndex * numberOfLines;
            assert baseIndex <= cache.size();

            if (baseIndex < cache.size()) {
                // we can re-use the slots of a chromosome that has been cleaned earlier
                for (int i = 0; i < numberOfLines; i++) {
                    cache.set(baseIndex + i, lineCoverageVector.get(i));
                }
            } else {
                // we need to enlarge the cache
                for (int i = 0; i < numberOfLines; i++) {
                    cache.add(baseIndex + i, lineCoverageVector.get(i));
                }
            }
        }

        return cache.get(cacheIndex * numberOfLines + index);
    }

    /**
     * Assigns to the given chromosome a new index in the cache.
     *
     * @param chromosome The given chromosome.
     * @return Returns the new cache index of the chromosome.
     */
    private int assignNewCacheIndex(final IChromosome<T> chromosome) {
        final int newIndex = usedCacheIndices.nextClearBit(0);
        usedCacheIndices.set(newIndex);
        chromosomeToCacheIndex.put(chromosome, newIndex);
        return newIndex;
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise the line metric.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value for the given chromosome.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome);
    }

    /**
     * Removes chromosomes from the cache that are no longer in use in order to avoid memory issues.
     *
     * @param activeChromosomes The list of active chromosomes.
     */
    public static <T> void cleanCache(List<IChromosome<T>> activeChromosomes) {

        Set<IChromosome<T>> cachedChromosomes = new HashSet(chromosomeToCacheIndex.keySet());

        for (IChromosome<T> activeChromosome : activeChromosomes) {
            cachedChromosomes.remove(activeChromosome);
        }

        for (IChromosome<T> inactiveChromosome : cachedChromosomes) {
            final int index = chromosomeToCacheIndex.remove(inactiveChromosome);
            usedCacheIndices.clear(index);
        }

        MATELog.log_acc("Cleaning cache: " + cachedChromosomes.size() + " inactive chromosome removed.");
    }
}
