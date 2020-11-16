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
     * Copies the fitness data for the given test cases from a source chromosome to a
     * target chromosome.
     *
     * @param sourceChromosome The source chromosome.
     * @param targetChromosome The target chromosome.
     * @param testCases The test cases for which coverage data should be copied over.
     */
    public static void copyFitnessData(IChromosome<TestSuite> sourceChromosome,
                                        IChromosome<TestSuite> targetChromosome, List<TestCase> testCases) {

        if (BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())
                || BranchDistanceFitnessFunctionMultiObjective.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())
                || LineCoveredPercentageFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            Registry.getEnvironmentManager().copyFitnessData(sourceChromosome, targetChromosome, testCases);
        }
    }

    /**
     * Stores for the given test case the fitness data, e.g. the traces are
     * fetched from the emulator when dealing with branch distance fitness.
     *
     * @param chromosome The given test case.
     */
    public static void storeTestCaseChromosomeFitness(IChromosome<TestCase> chromosome) {

        if (BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())
                || BranchDistanceFitnessFunctionMultiObjective.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())
                || LineCoveredPercentageFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            Registry.getEnvironmentManager().storeFitnessData(chromosome.getValue().toString(), null);
        }

        if (LineCoveredPercentageFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
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
            Registry.getEnvironmentManager().storeFitnessData(chromosome.getValue().toString(), testCaseId);
        }

    }

    /**
     * Removes all non active chromosomes, i.e. obsolete chromosomes, from an internal cache.
     *
     * @param activeChromosomes The list of active chromosomes.
     */
    public static <T> void cleanCache(List<IChromosome<T>> activeChromosomes) {

        if (LineCoveredPercentageFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            LineCoveredPercentageFitnessFunction.cleanCache(activeChromosomes);
        }
    }
}
