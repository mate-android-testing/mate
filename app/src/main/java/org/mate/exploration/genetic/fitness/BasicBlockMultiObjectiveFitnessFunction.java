package org.mate.exploration.genetic.fitness;

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
    private static List<String> blocks = new ArrayList<>();

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
        cache.put(block, new HashMap<IChromosome, Double>());
    }

    /**
     * Retrieves the basic block fitness value for the given chromosome.
     * A cache is employed to make subsequent requests faster. A fitness value of '1' indicates
     * that the basic block has been covered, '0' indicates non-covered.
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
            List<Double> basicBlockFitnessVector = FitnessUtils.getFitness(chromosome, blocks);

            // insert them into the cache
            for (int i = 0; i < basicBlockFitnessVector.size(); i++) {
                cache.get(blocks.get(i)).put(chromosome, basicBlockFitnessVector.get(i));
            }

            basicBlockFitnessValue = cache.get(block).get(chromosome);
        }

        return basicBlockFitnessValue;
    }

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome);
    }
}
