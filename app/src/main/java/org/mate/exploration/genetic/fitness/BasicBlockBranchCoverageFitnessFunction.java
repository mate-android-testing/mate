package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

/**
 * Provides a fitness metric based on basic block branch coverage. This requires that the
 * AUT has been instrumented with the basic block coverage module.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class BasicBlockBranchCoverageFitnessFunction<T> implements IFitnessFunction<T> {

    @Override
    public double getFitness(IChromosome<T> chromosome) {

        double basicBlockBranchCoverage = FitnessUtils.getFitness(chromosome);

        if (basicBlockBranchCoverage == 100.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        // TODO: normalise fitness value in the range [0,1]
        return basicBlockBranchCoverage;
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
