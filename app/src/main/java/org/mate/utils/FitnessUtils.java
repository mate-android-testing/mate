package org.mate.utils;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.exploration.genetic.fitness.StatementCoverageFitnessFunction;
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

    /**
     * Retrieves the fitness value for the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness value should be evaluated.
     * @param <T> Specifies whether the chromosome is a test suite or a test case.
     * @return Returns the fitness value for the given chromosome.
     */
    public static <T> double getFitness(IChromosome<T> chromosome) {

        if (BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            return Registry.getEnvironmentManager().getBranchDistance(chromosome.toString());
        } else if (BranchCoverageFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            return Registry.getEnvironmentManager().getCoverage(Coverage.BRANCH_COVERAGE, chromosome.toString());
        } else if (StatementCoverageFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            return Registry.getEnvironmentManager().getCoverage(Coverage.LINE_COVERAGE, chromosome.toString());
        }

        throw new UnsupportedOperationException("Fitness function "
                + Properties.FITNESS_FUNCTION() + "not yet supported!");
    }

    /**
     * Retrieves the fitness vector for the given chromosome, i.e. the chromosome is evaluated
     * against each single objective, e.g. branch.
     *
     * @param chromosome The chromosome for which the fitness vector should be evaluated.
     * @param objectives A list of objectives, e.g. lines or branches, or {@code null} if not required.
     * @param <T> Specifies whether the chromosome is a test suite or a test case.
     * @return Returns the fitness vector for the given chromosome.
     */
    public static <T> List<Double> getFitness(IChromosome<T> chromosome, List<String> objectives) {

        if (BranchDistanceFitnessFunctionMultiObjective.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            return Registry.getEnvironmentManager().getBranchDistanceVector(chromosome);
        } else if (LineCoveredPercentageFitnessFunction.FITNESS_FUNCTION_ID.equals(Properties.FITNESS_FUNCTION())) {
            return Registry.getEnvironmentManager().getLineCoveredPercentage(chromosome, objectives);
        }

        throw new UnsupportedOperationException("Fitness function "
                + Properties.FITNESS_FUNCTION() + "not yet supported!");
    }
}
