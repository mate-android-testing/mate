package org.mate.utils;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BasicBlockMultiObjectiveFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceMultiObjectiveFitnessFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.coverage.Coverage;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Provides utility functions to retrieving fitness related information.
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

        for (FitnessFunction fitness : Properties.FITNESS_FUNCTIONS()) {
            if (fitnessFunctions.contains(fitness)) {
                Registry.getEnvironmentManager()
                        .copyFitnessData(sourceChromosome, targetChromosome, testCases, fitness);
            }
        }
    }

    /**
     * Stores for the given test case the fitness data, e.g. the traces are fetched from the emulator
     * when dealing with branch distance fitness.
     *
     * @param chromosome The given test case.
     */
    public static void storeTestCaseChromosomeFitness(IChromosome<TestCase> chromosome) {
        storeTestSuite(chromosome, null);
    }

    /**
     * Stores for the given test suite the fitness data, e.g. the traces are fetched from the emulator
     * when dealing with branch distance fitness.
     *
     * @param chromosome The given test suite.
     * @param testCase The test case within the test suite.
     */
    public static void storeTestSuiteChromosomeFitness(IChromosome<TestSuite> chromosome, TestCase testCase) {
        storeTestSuite(chromosome, testCase.getId());
    }

    private static <T> void storeTestSuite(IChromosome<T> chromosome, String testCaseId) {
        EnumSet<FitnessFunction> fitnessFunctions = EnumSet.of(FitnessFunction.BRANCH_COVERAGE,
                FitnessFunction.BRANCH_DISTANCE, FitnessFunction.LINE_COVERAGE,
                FitnessFunction.METHOD_COVERAGE, FitnessFunction.BRANCH_MULTI_OBJECTIVE,
                FitnessFunction.BRANCH_DISTANCE_MULTI_OBJECTIVE, FitnessFunction.LINE_PERCENTAGE_COVERAGE,
                FitnessFunction.BASIC_BLOCK_BRANCH_COVERAGE, FitnessFunction.BASIC_BLOCK_LINE_COVERAGE,
                FitnessFunction.NOVELTY, FitnessFunction.BASIC_BLOCK_MULTI_OBJECTIVE);

        for (FitnessFunction function : Properties.FITNESS_FUNCTIONS()) {
            if (fitnessFunctions.contains(function)) {
                Registry.getEnvironmentManager().storeFitnessData(chromosome, testCaseId, function);
            }
        }

        if (Arrays.stream(Properties.FITNESS_FUNCTIONS()).anyMatch(
                function -> function == FitnessFunction.LINE_PERCENTAGE_COVERAGE)) {
            LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
        }
    }

    /**
     * Removes all non active chromosomes, i.e. obsolete chromosomes, from an internal cache.
     *
     * @param activeChromosomes The list of active chromosomes.
     */
    public static <T> void cleanCache(List<IChromosome<T>> activeChromosomes) {

        for (FitnessFunction function : Properties.FITNESS_FUNCTIONS()) {
            switch (function) {
                case LINE_PERCENTAGE_COVERAGE:
                    LineCoveredPercentageFitnessFunction.cleanCache(activeChromosomes);
                    break;
                case BASIC_BLOCK_MULTI_OBJECTIVE:
                    BasicBlockMultiObjectiveFitnessFunction.cleanCache(activeChromosomes);
                    break;
                case BRANCH_DISTANCE_MULTI_OBJECTIVE:
                    BranchDistanceMultiObjectiveFitnessFunction.cleanCache(activeChromosomes);
                    break;
                default:
            }
        }
    }

    /**
     * Retrieves the fitness value for the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness value should be evaluated.
     * @param function The fitness function used.
     * @param <T> Specifies whether the chromosome is a test suite or a test case.
     * @return Returns the fitness value for the given chromosome.
     */
    public static <T> double getFitness(IChromosome<T> chromosome, FitnessFunction function) {
        switch (function) {
            case BRANCH_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.BRANCH_COVERAGE, chromosome)
                        .getBranchCoverage();
            case BRANCH_DISTANCE:
                return Registry.getEnvironmentManager().getBranchDistance(chromosome);
            case LINE_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.LINE_COVERAGE, chromosome)
                        .getLineCoverage();
            case METHOD_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.METHOD_COVERAGE, chromosome)
                        .getMethodCoverage();
            case BASIC_BLOCK_LINE_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.BASIC_BLOCK_LINE_COVERAGE, chromosome)
                        .getLineCoverage();
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.BASIC_BLOCK_BRANCH_COVERAGE, chromosome)
                        .getBranchCoverage();
            default:
                throw new UnsupportedOperationException("Fitness function "
                        + function + " not yet supported!");
        }
    }

    /**
     * Retrieves the fitness vector for the given chromosome, i.e. the chromosome is evaluated
     * against each single objective, e.g. branch.
     *
     * @param chromosome The chromosome for which the fitness vector should be evaluated.
     * @param objectives A list of objectives, e.g. lines or branches, or {@code null} if not required.
     * @param function The fitness function used.
     * @param <T> Specifies whether the chromosome is a test suite or a test case.
     * @return Returns the fitness vector for the given chromosome.
     */
    public static <T> List<Double> getFitness(IChromosome<T> chromosome,
                                              List<String> objectives,
                                              FitnessFunction function) {

        switch (function) {
            case BRANCH_DISTANCE_MULTI_OBJECTIVE:
                return Registry.getEnvironmentManager()
                        .getBranchDistanceVector(chromosome, objectives);
            case LINE_PERCENTAGE_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getLineCoveredPercentage(chromosome, objectives);
            case BASIC_BLOCK_MULTI_OBJECTIVE:
                return Registry.getEnvironmentManager()
                        .getBasicBlockFitnessVector(chromosome, objectives);
            case BRANCH_MULTI_OBJECTIVE:
                return Registry.getEnvironmentManager()
                        .getBranchFitnessVector(chromosome, objectives);
            default:
                throw new UnsupportedOperationException("Fitness function "
                        + function + " not yet supported!");
        }
    }

    /**
     * Retrieves the novelty vector for the given chromosomes.
     *
     * @param chromosomes The list of chromosomes for which the novelty should be computed.
     * @param nearestNeighbours The number of nearest neighbours k that should be considered.
     * @param objectives The objectives type, e.g. branches.
     * @param <T> Specifies the type of the chromosomes.
     * @return Returns the novelty vector.
     */
    public static <T> List<Double> getNoveltyVector(List<IChromosome<T>> chromosomes,
                                                    int nearestNeighbours, String objectives) {
        return Registry.getEnvironmentManager()
                .getNoveltyVector(chromosomes, nearestNeighbours, objectives);
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
