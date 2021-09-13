package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

/**
 * Provides a fitness metric based on basic block line coverage. This requires that the
 * AUT has been instrumented with the basic block coverage module.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class BasicBlockLineCoverageFitnessFunction<T> implements IFitnessFunction<T> {

    @Override
    public double getFitness(IChromosome<T> chromosome) {

        double basicBlockLineCoverage = FitnessUtils.getFitness(chromosome);

        if (basicBlockLineCoverage == 100.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        return basicBlockLineCoverage;
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
