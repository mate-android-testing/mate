package org.mate.utils;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.List;

public class FitnessUtils {

    private FitnessUtils() {
        throw new UnsupportedOperationException("Utility class!");
    }

    /**
     * Stores for the given test case the fitness data, e.g. the traces are
     * fetched from the emulator when dealing with branch distance fitness.
     *
     * @param chromosome The given test case.
     */
    public static void storeTestCaseChromosomeFitness(IChromosome<TestCase> chromosome) {

        // TODO: use enum for fitness function property
        // store branch distance data
        if (BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())
                || BranchDistanceFitnessFunctionMultiObjective.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            Registry.getEnvironmentManager().storeBranchDistanceData(chromosome.getValue().toString(), null);
        }

        if (LineCoveredPercentageFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            Registry.getEnvironmentManager().storeCoverageData(
                    Coverage.LINE_COVERAGE,
                    chromosome.getValue().toString(),
                    null);

            LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
        }
    }

    /**
     * Stores for the given test suite the fitness data, e.g. the traces are
     * fetched from the emulator when dealing with branch distance fitness.
     *
     * @param chromosome The given test suite.
     * @param testCaseId The test case id.
     */
    public static void storeTestSuiteChromosomeFitness(IChromosome<TestSuite> chromosome, String testCaseId) {

        // TODO: use enum for fitness function property
        // store branch distance data
        if (BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())
                || BranchDistanceFitnessFunctionMultiObjective.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            Registry.getEnvironmentManager().storeBranchDistanceData(chromosome.getValue().toString(), testCaseId);
        }

    }

    /**
     * Removes all non active chromosomes, i.e. obsolete chromosomes, from an internal cache.
     *
     * @param activeChromosomes The list of active chromosomes.
     */
    public static void cleanCache(List<IChromosome<TestCase>> activeChromosomes) {

        if (LineCoveredPercentageFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            // LineCoveredPercentageFitnessFunction.cleanCache(activeChromosomes);
        }
    }
}
