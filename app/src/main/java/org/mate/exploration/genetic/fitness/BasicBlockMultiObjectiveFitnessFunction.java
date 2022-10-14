package org.mate.exploration.genetic.fitness;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a fitness metric based on basic block coverage for multi-objective algorithms. This
 * requires that the AUT has been instrumented with the basic block coverage module. A fitness
 * value of '1' indicates that the basic block has been covered, '0' indicates non-covered.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class BasicBlockMultiObjectiveFitnessFunction<T> implements IFitnessFunction<T> {

    // a cache that stores for each basic block the set of test cases and its fitness value
    private static final Map<String, Map<IChromosome, Double>> cache = new HashMap<>();

    // all basic blocks (shared by instances)
    private static final List<String> blocks = new ArrayList<>();

    // the current basic block we want to evaluate this fitness function against
    private final String block;

    /**
     * Initialises the fitness function with the given basic block as target.
     *
     * @param block The target basic block.
     */
    public BasicBlockMultiObjectiveFitnessFunction(String block) {
        this.block = block;
        blocks.add(block);
        cache.put(block, new HashMap<>());
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

        double basicBlockFitnessValue;

        if (cache.get(block).containsKey(chromosome)) {
            basicBlockFitnessValue = cache.get(block).get(chromosome);
        } else {
            // retrieves the fitness value for every single basic block
            List<Double> basicBlockFitnessVector = FitnessUtils.getFitness(chromosome, blocks,
                    FitnessFunction.BASIC_BLOCK_MULTI_OBJECTIVE);

            // insert them into the cache
            for (int i = 0; i < basicBlockFitnessVector.size(); i++) {
                cache.get(blocks.get(i)).put(chromosome, basicBlockFitnessVector.get(i));
            }

            basicBlockFitnessValue = cache.get(block).get(chromosome);
        }

        return basicBlockFitnessValue;
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise coverage of
     *          basic blocks.
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
     * @param chromosomes The list of active chromosomes.
     */
    public static <T> void cleanCache(List<IChromosome<T>> chromosomes) {

        if (blocks.size() == 0 || cache.size() == 0) {
            return;
        }

        List<IChromosome<T>> activeChromosomes = new ArrayList<>(chromosomes);

        int count = 0;
        for (String block : blocks) {
            Map<IChromosome, Double> blockCache = cache.get(block);
            for (IChromosome chromosome: new ArrayList<>(blockCache.keySet())) {
                if (!activeChromosomes.contains(chromosome)) {
                    blockCache.remove(chromosome);
                    count++;
                }
            }
        }
        MATE.log_acc("Cleaning cache: " + count + " inactive chromosome removed.");
    }
}
