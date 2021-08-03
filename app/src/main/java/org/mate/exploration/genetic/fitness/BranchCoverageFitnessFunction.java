package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

/**
 * Provides a fitness metric based on branch coverage. This requires that the
 * AUT has been instrumented with the branch coverage module.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class BranchCoverageFitnessFunction<T> implements IFitnessFunction<T> {

    @Override
    public double getFitness(IChromosome<T> chromosome) {

        double branchCoverage = FitnessUtils.getFitness(chromosome);

        if (branchCoverage == 100.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        // TODO: normalise fitness value in the range [0,1]
        return branchCoverage;
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return 0;
    }
}
