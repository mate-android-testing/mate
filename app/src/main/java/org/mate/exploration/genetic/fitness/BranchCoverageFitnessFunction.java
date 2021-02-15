package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

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
}
