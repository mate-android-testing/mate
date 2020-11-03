package org.mate.utils;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

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

        // store branch distance data
        if (BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            Registry.getEnvironmentManager().storeBranchDistanceData(chromosome.getValue().toString(), null);
            MATE.log("Branch Distance: " + Registry.getEnvironmentManager()
                    .getBranchDistance(chromosome.getValue().toString()));
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

        // store branch distance data
        if (BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            Registry.getEnvironmentManager().storeBranchDistanceData(chromosome.getValue().toString(), testCaseId);
            MATE.log("Branch Distance: " + Registry.getEnvironmentManager()
                    .getBranchDistance(chromosome.getValue().toString()));
        }

    }
}
