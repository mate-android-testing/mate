package org.mate.utils;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BasicBlockMultiObjectiveFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceMultiObjectiveFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchMultiObjectiveFitnessFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.coverage.Coverage;

import java.util.Arrays;
import java.util.BitSet;
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

        for (FitnessFunction fitnessFunction : Properties.FITNESS_FUNCTIONS()) {
            if (fitnessFunctions.contains(fitnessFunction)) {
                Registry.getEnvironmentManager()
                        .copyFitnessData(sourceChromosome, targetChromosome, testCases, fitnessFunction);
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
        storeFitnessData(chromosome, null);
    }

    /**
     * Stores for the given test suite the fitness data, e.g. the traces are fetched from the emulator
     * when dealing with branch distance fitness.
     *
     * @param chromosome The given test suite.
     * @param testCase The test case within the test suite.
     */
    public static void storeTestSuiteChromosomeFitness(IChromosome<TestSuite> chromosome, TestCase testCase) {
        storeFitnessData(chromosome, testCase.getId());
    }

    /**
     * Stores for the given chromosome the fitness data, e.g. the traces.
     *
     * @param chromosome The chromosome for which the fitness data should be stored.
     * @param testCaseId Refers to a test case within a test suite or {@code null} if the chromosome
     *         describes a test case.
     * @param <T> Specifies whether the chromosome refers to a test case or a test suite.
     */
    private static <T> void storeFitnessData(IChromosome<T> chromosome, String testCaseId) {

        if (Properties.FITNESS_FUNCTIONS() == null) {
            /*
             * If the underlying algorithm doesn't use any fitness function but uses the default
             * chromosome factory or any derivative of it, storeFitnessData() is called. Since there
             * is no fitness function specified, the subsequent foreach loop would cause a NPE.
             */
            return;
        }

        EnumSet<FitnessFunction> fitnessFunctions = EnumSet.of(FitnessFunction.BRANCH_COVERAGE,
                FitnessFunction.BRANCH_DISTANCE, FitnessFunction.LINE_COVERAGE,
                FitnessFunction.METHOD_COVERAGE, FitnessFunction.BRANCH_MULTI_OBJECTIVE,
                FitnessFunction.BRANCH_DISTANCE_MULTI_OBJECTIVE, FitnessFunction.LINE_PERCENTAGE_COVERAGE,
                FitnessFunction.BASIC_BLOCK_BRANCH_COVERAGE, FitnessFunction.BASIC_BLOCK_LINE_COVERAGE,
                FitnessFunction.NOVELTY, FitnessFunction.BASIC_BLOCK_MULTI_OBJECTIVE);

        for (FitnessFunction fitnessFunction : Properties.FITNESS_FUNCTIONS()) {
            if (fitnessFunctions.contains(fitnessFunction)) {
                Registry.getEnvironmentManager().storeFitnessData(chromosome, testCaseId, fitnessFunction);
            }
        }
    }

    /**
     * Removes all non active chromosomes, i.e. obsolete chromosomes, from an internal cache. This
     * method should be only called in the context of multi-objective algorithms, where the same
     * fitness function is used for every objective, e.g. branch.
     *
     * @param activeChromosomes The list of active chromosomes.
     */
    public static <T> void cleanCache(List<IChromosome<T>> activeChromosomes) {

        assert Properties.ALGORITHM() == Algorithm.MIO || Properties.ALGORITHM() == Algorithm.MOSA;

        // TODO: perform a sanity check that the same fitness function is used for every objective
        FitnessFunction fitnessFunction = Properties.FITNESS_FUNCTIONS()[0];

        switch (fitnessFunction) {
            case LINE_PERCENTAGE_COVERAGE:
                LineCoveredPercentageFitnessFunction.cleanCache(activeChromosomes);
                break;
            case BASIC_BLOCK_MULTI_OBJECTIVE:
                BasicBlockMultiObjectiveFitnessFunction.cleanCache(activeChromosomes);
                break;
            case BRANCH_DISTANCE_MULTI_OBJECTIVE:
                BranchDistanceMultiObjectiveFitnessFunction.cleanCache(activeChromosomes);
                break;
            case BRANCH_MULTI_OBJECTIVE:
                BranchMultiObjectiveFitnessFunction.cleanCache(activeChromosomes);
                break;
        }
    }

    /**
     * Retrieves the fitness value for the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness value should be evaluated.
     * @param fitnessFunction The fitness function used.
     * @param <T> Specifies whether the chromosome is a test suite or a test case.
     * @return Returns the fitness value for the given chromosome.
     */
    public static <T> double getFitness(IChromosome<T> chromosome, FitnessFunction fitnessFunction) {

        switch (fitnessFunction) {
            case BRANCH_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.BRANCH_COVERAGE, chromosome).getBranchCoverage();
            case BRANCH_DISTANCE:
                return Registry.getEnvironmentManager().getBranchDistance(chromosome);
            case LINE_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.LINE_COVERAGE, chromosome).getLineCoverage();
            case METHOD_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.METHOD_COVERAGE, chromosome).getMethodCoverage();
            case BASIC_BLOCK_LINE_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.BASIC_BLOCK_LINE_COVERAGE, chromosome)
                        .getLineCoverage();
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(Coverage.BASIC_BLOCK_BRANCH_COVERAGE, chromosome)
                        .getBranchCoverage();
            case GENO_TO_PHENO_TYPE:
                // GE specifies the 'core' fitness function in the GE_FITNESS_FUNCTION() property
                if (Properties.GE_FITNESS_FUNCTION() == FitnessFunction.BASIC_BLOCK_BRANCH_COVERAGE) {
                    return Registry.getEnvironmentManager()
                            .getCoverage(Coverage.BASIC_BLOCK_BRANCH_COVERAGE, chromosome).getBranchCoverage();
                } else if (Properties.GE_FITNESS_FUNCTION() == FitnessFunction.BASIC_BLOCK_LINE_COVERAGE) {
                    return Registry.getEnvironmentManager()
                            .getCoverage(Coverage.BASIC_BLOCK_LINE_COVERAGE, chromosome).getLineCoverage();
                } else if (Properties.GE_FITNESS_FUNCTION() == FitnessFunction.BRANCH_COVERAGE) {
                    return Registry.getEnvironmentManager()
                            .getCoverage(Coverage.BRANCH_COVERAGE, chromosome).getBranchCoverage();
                } else if (Properties.GE_FITNESS_FUNCTION() == FitnessFunction.BRANCH_DISTANCE) {
                    return Registry.getEnvironmentManager().getBranchDistance(chromosome);
                } else if (Properties.GE_FITNESS_FUNCTION() == FitnessFunction.METHOD_COVERAGE) {
                    return Registry.getEnvironmentManager()
                            .getCoverage(Coverage.METHOD_COVERAGE, chromosome).getMethodCoverage();
                } else if (Properties.GE_FITNESS_FUNCTION() == FitnessFunction.LINE_COVERAGE) {
                    return Registry.getEnvironmentManager()
                            .getCoverage(Coverage.LINE_COVERAGE, chromosome).getLineCoverage();
                } else {
                    throw new UnsupportedOperationException("GE fitness function "
                            + Properties.GE_FITNESS_FUNCTION() + " not yet supported!");
                }
            default:
                throw new UnsupportedOperationException("Fitness function "
                        + fitnessFunction + " not yet supported!");
        }
    }

    /**
     * Retrieves the branch fitness vector for the given chromosome.
     *
     * @param chromosome The chromosome for which fitness should be evaluated.
     * @param numberOfBranches The number of branches.
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the branch fitness vector for the given chromosome.
     */
    public static <T> BitSet getBranchFitnessVector(IChromosome<T> chromosome, int numberOfBranches) {

        if (Arrays.stream(Properties.FITNESS_FUNCTIONS()).noneMatch(
                fitnessFunction -> fitnessFunction == FitnessFunction.BRANCH_MULTI_OBJECTIVE)) {
            throw new IllegalStateException("Unexpected fitness function!");
        }

        return Registry.getEnvironmentManager().getBranchFitnessVector(chromosome, numberOfBranches);
    }

    /**
     * Retrieves the basic block fitness vector for the given chromosome.
     *
     * @param chromosome The chromosome for which fitness should be evaluated.
     * @param numberOfBasicBlocks The number of basic blocks.
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the basic block fitness vector for the given chromosome.
     */
    public static <T> BitSet getBasicBlockFitnessVector(IChromosome<T> chromosome, int numberOfBasicBlocks) {

        if (Arrays.stream(Properties.FITNESS_FUNCTIONS()).noneMatch(
                fitnessFunction -> fitnessFunction == FitnessFunction.BASIC_BLOCK_MULTI_OBJECTIVE)) {
            throw new IllegalStateException("Unexpected fitness function!");
        }

        return Registry.getEnvironmentManager().getBasicBlockFitnessVector(chromosome, numberOfBasicBlocks);
    }

    /**
     * Retrieves the branch distance vector for the given chromosome.
     *
     * @param chromosome The chromosome for which fitness should be evaluated.
     * @param numberOfBranches The number of branches.
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the branch distance vector for the given chromosome.
     */
    public static <T> List<Float> getBranchDistanceVector(IChromosome<T> chromosome, int numberOfBranches) {

        if (Arrays.stream(Properties.FITNESS_FUNCTIONS()).noneMatch(
                fitnessFunction -> fitnessFunction == FitnessFunction.BRANCH_DISTANCE_MULTI_OBJECTIVE)) {
            throw new IllegalStateException("Unexpected fitness function!");
        }

        return Registry.getEnvironmentManager().getBranchDistanceVector(chromosome, numberOfBranches);
    }

    /**
     * Retrieves the line percentage vector for the given chromosome.
     *
     * @param chromosome The chromosome for which fitness should be evaluated.
     * @param numberOfLines The number of lines.
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the line percentage vector for the given chromosome.
     */
    public static <T> List<Float> getLinePercentageVector(IChromosome<T> chromosome, int numberOfLines) {

        if (Arrays.stream(Properties.FITNESS_FUNCTIONS()).noneMatch(
                fitnessFunction -> fitnessFunction == FitnessFunction.LINE_PERCENTAGE_COVERAGE)) {
            throw new IllegalStateException("Unexpected fitness function!");
        }

        return Registry.getEnvironmentManager().getLinePercentageVector(chromosome, numberOfLines);
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
