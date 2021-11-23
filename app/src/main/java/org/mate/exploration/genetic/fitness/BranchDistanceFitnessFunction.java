package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a fitness metric based on approach level + branch distance. This requires that the
 * AUT has been instrumented with the branch distance module. The fitness value is already
 * normalised in the range [0,1].
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class BranchDistanceFitnessFunction<T> implements IFitnessFunction<T> {

    /**
     * Caches for each chromosome the computed branch distance.
     */
    private Map<IChromosome<T>, Double> cache = new HashMap<>();

    /**
     * Computes the branch distance value for the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness value should be retrieved.
     * @return Returns the fitness value (branch distance) for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {

        double branchDistance;

        if (cache.containsKey(chromosome)) {
            branchDistance = cache.get(chromosome);
        } else {
            branchDistance = FitnessUtils.getFitness(chromosome);
        }

        /*
         * TODO: This is a side effect, which is triggered multiple times, e.g. by logFitness().
         *  Additionally, the decision when a 'target' is satisfied might depend on the
         *  algorithm in use. Thus, a better option is to move this functionality. However, be
         *  aware that the initial population can also satisfy this condition.
         */
        // we can end execution if we covered the target vertex
        if (branchDistance == 0.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        // cache fitness value for subsequent requests
        cache.put(chromosome, branchDistance);
        return branchDistance;
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code false} since this fitness functions aims to minimise the branch distance.
     */
    @Override
    public boolean isMaximizing() {
        return false;
    }

    /**
     * Returns the normalised fitness value, i.e. the branch distance between 0 and 1.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome);
    }
}
