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

    /**
     * Computes the basic block branch coverage for the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the branch coverage obtained by the basic block coverage module.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {

        double basicBlockBranchCoverage
                = FitnessUtils.getFitness(chromosome, FitnessFunction.BASIC_BLOCK_BRANCH_COVERAGE);

        if (basicBlockBranchCoverage == 100.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        return basicBlockBranchCoverage;
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise the branch coverage.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value, i.e. the branch coverage divided by 100.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome) / 100;
    }
}
