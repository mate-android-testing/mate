package org.mate.utils;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.coverage.Coverage;

import java.util.EnumSet;
import java.util.List;

/**
 * A utility class for handling any kind of fitness request, i.e. storing, copying and retrieving
 * fitness data.
 */
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

        EnumSet<FitnessFunction> fitnessFunctions = EnumSet.of(FitnessFunction.BRANCH_COVERAGE,
                FitnessFunction.BRANCH_DISTANCE, FitnessFunction.LINE_COVERAGE,
                FitnessFunction.BRANCH_DISTANCE_MULTI_OBJECTIVE, FitnessFunction.LINE_PERCENTAGE_COVERAGE,
                FitnessFunction.METHOD_COVERAGE, FitnessFunction.BRANCH_MULTI_OBJECTIVE,
                FitnessFunction.BRANCH_DISTANCE_MULTI_OBJECTIVE, FitnessFunction.LINE_PERCENTAGE_COVERAGE,
                FitnessFunction.BASIC_BLOCK_BRANCH_COVERAGE, FitnessFunction.BASIC_BLOCK_LINE_COVERAGE,
                FitnessFunction.NOVELTY, FitnessFunction.BASIC_BLOCK_MULTI_OBJECTIVE);

        if (fitnessFunctions.contains(Properties.FITNESS_FUNCTION())) {
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

        EnumSet<FitnessFunction> fitnessFunctions = EnumSet.of(FitnessFunction.BRANCH_COVERAGE,
                FitnessFunction.BRANCH_DISTANCE, FitnessFunction.LINE_COVERAGE,
                FitnessFunction.BRANCH_DISTANCE_MULTI_OBJECTIVE,
                FitnessFunction.METHOD_COVERAGE,
                FitnessFunction.NOVELTY,
                FitnessFunction.BRANCH_DISTANCE_MULTI_OBJECTIVE, FitnessFunction.LINE_PERCENTAGE_COVERAGE,
                FitnessFunction.BASIC_BLOCK_BRANCH_COVERAGE, FitnessFunction.BASIC_BLOCK_LINE_COVERAGE,
                FitnessFunction.BASIC_BLOCK_MULTI_OBJECTIVE, FitnessFunction.BRANCH_MULTI_OBJECTIVE);

        if (fitnessFunctions.contains(Properties.FITNESS_FUNCTION())) {
            Registry.getEnvironmentManager().storeFitnessData(chromosome, null);
        }

        if (Properties.FITNESS_FUNCTION() == FitnessFunction.LINE_PERCENTAGE_COVERAGE) {
            LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
        }
    }

    /**
     * Stores for the given test suite the fitness data, e.g. the traces are
     * fetched from the emulator when dealing with branch distance fitness.
     *
     * @param chromosome The given test suite.
     * @param testCase The test case within the test suite.
     */
    public static void storeTestSuiteChromosomeFitness(IChromosome<TestSuite> chromosome, TestCase testCase) {

        EnumSet<FitnessFunction> fitnessFunctions = EnumSet.of(FitnessFunction.BRANCH_COVERAGE,
                FitnessFunction.BRANCH_DISTANCE, FitnessFunction.LINE_COVERAGE,
                FitnessFunction.METHOD_COVERAGE, FitnessFunction.BRANCH_MULTI_OBJECTIVE,
                FitnessFunction.BRANCH_DISTANCE_MULTI_OBJECTIVE, FitnessFunction.LINE_PERCENTAGE_COVERAGE,
                FitnessFunction.BASIC_BLOCK_BRANCH_COVERAGE, FitnessFunction.BASIC_BLOCK_LINE_COVERAGE,
                FitnessFunction.NOVELTY, FitnessFunction.BASIC_BLOCK_MULTI_OBJECTIVE);

        if (fitnessFunctions.contains(Properties.FITNESS_FUNCTION())) {
            Registry.getEnvironmentManager().storeFitnessData(chromosome, testCase.getId());
        }

        if (Properties.FITNESS_FUNCTION() == FitnessFunction.LINE_PERCENTAGE_COVERAGE) {
            LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
        }
    }

    /**
     * Removes all non active chromosomes, i.e. obsolete chromosomes, from an internal cache.
     *
     * @param activeChromosomes The list of active chromosomes.
     */
    public static <T> void cleanCache(List<IChromosome<T>> activeChromosomes) {

        if (Properties.FITNESS_FUNCTION() == FitnessFunction.LINE_PERCENTAGE_COVERAGE) {
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

        if (Properties.FITNESS_FUNCTION() == FitnessFunction.BRANCH_COVERAGE) {
            return Registry.getEnvironmentManager().getCoverage(Coverage.BRANCH_COVERAGE, chromosome);
        } else if (Properties.FITNESS_FUNCTION() == FitnessFunction.BRANCH_DISTANCE) {
            return Registry.getEnvironmentManager().getBranchDistance(chromosome);
        } else if (Properties.FITNESS_FUNCTION() == FitnessFunction.LINE_COVERAGE) {
            return Registry.getEnvironmentManager().getCoverage(Coverage.LINE_COVERAGE, chromosome);
        } else if (Properties.FITNESS_FUNCTION() == FitnessFunction.METHOD_COVERAGE) {
            return Registry.getEnvironmentManager().getCoverage(Coverage.METHOD_COVERAGE, chromosome);
        } else if (Properties.FITNESS_FUNCTION() == FitnessFunction.BASIC_BLOCK_LINE_COVERAGE) {
            return Registry.getEnvironmentManager().getCoverage(Coverage.BASIC_BLOCK_LINE_COVERAGE, chromosome);
        } else if (Properties.FITNESS_FUNCTION() == FitnessFunction.BASIC_BLOCK_BRANCH_COVERAGE) {
            return Registry.getEnvironmentManager().getCoverage(Coverage.BASIC_BLOCK_BRANCH_COVERAGE, chromosome);
        }

        throw new UnsupportedOperationException("Fitness function "
                + Properties.FITNESS_FUNCTION() + " not yet supported!");
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

        if (Properties.FITNESS_FUNCTION() == FitnessFunction.BRANCH_DISTANCE_MULTI_OBJECTIVE) {
            return Registry.getEnvironmentManager().getBranchDistanceVector(chromosome, objectives);
        } else if (Properties.FITNESS_FUNCTION() == FitnessFunction.LINE_PERCENTAGE_COVERAGE) {
            return Registry.getEnvironmentManager().getLineCoveredPercentage(chromosome, objectives);
        } else if (Properties.FITNESS_FUNCTION() == FitnessFunction.BASIC_BLOCK_MULTI_OBJECTIVE) {
            return Registry.getEnvironmentManager().getBasicBlockFitnessVector(chromosome, objectives);
        } else if (Properties.FITNESS_FUNCTION() == FitnessFunction.BRANCH_MULTI_OBJECTIVE) {
            return Registry.getEnvironmentManager().getBranchFitnessVector(chromosome, objectives);
        }

        throw new UnsupportedOperationException("Fitness function "
                + Properties.FITNESS_FUNCTION() + " not yet supported!");
    }

    /**
     * Retrieves the novelty vector for the chromosomes contained in the population and archive.
     *
     * @param population The chromosomes in the current population.
     * @param archive The chromosomes in the current archive.
     * @param nearestNeighbours The number of nearest neighbours k that should be considered.
     * @param objectives The objectives type, e.g. branches.
     * @param <T> Specifies the type of the chromosomes.
     * @return Returns the novelty vector.
     */
    public static <T> List<Double> getNoveltyVector(List<IChromosome<T>> population,
                                                List<IChromosome<T>> archive,
                                                    int nearestNeighbours, String objectives) {
        return Registry.getEnvironmentManager()
                .getNoveltyVector(population, archive, nearestNeighbours, objectives);
    }

    /**
     * Retrieves the novelty score for the given chromosome.
     *
     * @param chromosome The chromosome for which the novelty should be evaluated.
     * @param population The current population.
     * @param archive The current archive.
     * @param nearestNeighbours The number of nearest neighbours k.
     * @param objectives The kind of objectives, e.g. branches.
     * @param <T> The type of the chromosomes.
     * @return Returns the novelty for the given chromosome.
     */
    public static <T> double getNovelty(IChromosome<T> chromosome, List<IChromosome<T>> population,
                                        List<IChromosome<T>> archive, int nearestNeighbours,
                                        String objectives) {
        return Registry.getEnvironmentManager()
                .getNovelty(chromosome, population, archive, nearestNeighbours, objectives);
    }
}
