package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

/**
 * Provides a fitness metric based on method coverage. This requires that the
 * AUT has been instrumented with the method coverage module.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class MethodCoverageFitnessFunction<T> implements IFitnessFunction<T> {

    /**
     * Computes the method coverage for the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the method coverage obtained by the method coverage module.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {

        double methodCoverage = FitnessUtils.getFitness(chromosome);

        if (methodCoverage == 100.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        return methodCoverage;
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise the method coverage.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value, i.e. the method coverage divided by 100.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome) / 100;
    }
}
