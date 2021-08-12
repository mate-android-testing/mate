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

    @Override
    public double getFitness(IChromosome<T> chromosome) {

        double methodCoverage = FitnessUtils.getFitness(chromosome);

        if (methodCoverage == 100.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        // TODO: normalise fitness value in the range [0,1]
        return methodCoverage;
    }

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome) / 100;
    }
}
