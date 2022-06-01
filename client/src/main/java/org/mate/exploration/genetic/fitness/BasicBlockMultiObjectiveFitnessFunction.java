package org.mate.exploration.genetic.fitness;

import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a fitness metric based on basic block coverage for multi-objective algorithms. This
 * requires that the AUT has been instrumented with the basic block coverage module. A fitness
 * value of '1' indicates that the basic block has been covered, '0' indicates non-covered.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class BasicBlockMultiObjectiveFitnessFunction<T> implements IFitnessFunction<T> {

    /**
     * The cache is essentially a flat two-dimensional array where the first dimension describes
     * the position of a chromosome and the second dimension the position of the n-th basic block
     * or fitness function. Hence, by having a chromosome index, see {@link #chromosomeToCacheIndex},
     * and the fitness function index, see {@link #index}, we can look up the fitness for each
     * chromosome and basic block.
     *
     * A visual representation of the cache would look as follows:
     *
     *      chromosome 1 || chromosome 2 || ... || chromosome n
     *      [1,0,1,0,..]    [0,1,0,1,..]           [1,1,0,1,..]
     *      block 1 to n    block 1 to n           block 1 to n
     *
     * We could also say that we joined together multiple bit sets, where each bit set represents
     * the fitness values of a chromosome. To get the fitness value for a given chromosome and
     * basic block (objective), we can use the following formula:
     *
     *      fitness = cache[chromosomeIndex * numberOfBasicBlocks + indexOfBasicBlock]
     */
    private static final BitSet cache = new BitSet();

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
     * Tracks the total number of basic blocks / fitness functions.
     */
    private static int numberOfBasicBlocks;

    /**
     * Represents the index of the n-th basic block / fitness function.
     */
    private final int index;

    /**
     * Initialises the fitness function with the given index of the basic block.
     *
     * @param index The index of the n-th basic block / fitness function.
     */
    public BasicBlockMultiObjectiveFitnessFunction(int index) {
        this.index = index;
        numberOfBasicBlocks++;
    }

    /**
     * Computes the basic block fitness value for the given chromosome. A cache is employed to make
     * subsequent requests faster. A fitness value of '1' indicates that the basic block has been
     * covered, '0' indicates non-covered.
     *
     * @param chromosome The chromosome for which we want to retrieve its fitness value.
     * @return Returns the fitness value for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {

        Integer cacheIndex = chromosomeToCacheIndex.get(chromosome);

        if (cacheIndex == null) {
            // the chromosome isn't cached yet
            cacheIndex = assignNewCacheIndex(chromosome);

            // retrieve the fitness value for every single basic block
            final BitSet basicBlockFitnessVector
                    = FitnessUtils.getBasicBlockFitnessVector(chromosome, numberOfBasicBlocks);

            // update cache
            for (int i = 0; i < numberOfBasicBlocks; i++) {
                cache.set(cacheIndex * numberOfBasicBlocks + i, basicBlockFitnessVector.get(i));
            }
        }

        return cache.get(cacheIndex * numberOfBasicBlocks + index) ? 1.0 : 0.0;
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
     * @return Returns {@code true} since this fitness functions aims to maximise coverage of
     *         basic blocks.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value, i.e. the value 0 if the basic block was not covered
     * by the chromosome, otherwise the value 1 is returned.
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
