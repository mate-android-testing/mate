package org.mate.exploration.genetic;

import org.mate.ui.EnvironmentManager;

public class StatementCoverageFitnessFunction<T> implements IFitnessFunction<T> {
    public static final String FITNESS_FUNCTION_ID = "statement_coverage_fitness_function";

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        return EnvironmentManager.getCoverage();
    }
}
