package org.mate.exploration.genetic.fitness;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.Coverage;
import org.mate.utils.CoverageUtils;

public class BranchCoverageFitnessFunction<T> implements IFitnessFunction<T> {

    public static final String FITNESS_FUNCTION_ID = "branch_coverage_fitness_function";

    @Override
    public double getFitness(IChromosome<T> chromosome) {

        if (Properties.COVERAGE() != Coverage.BRANCH_COVERAGE) {
            throw new IllegalStateException("Wrong coverage property specified!");
        }

        double branchCoverage = CoverageUtils.getCoverage(Properties.COVERAGE(), chromosome);

        if (branchCoverage == 100.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        // TODO: normalise fitness value in the range [0,1]
        return branchCoverage;
    }
}
