package org.mate.utils;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

public class CoverageUtils {
    private CoverageUtils() {}

    /**
     * Store coverage data for the given TestCase based chromosome
     * @param chromosome store coverage for this chromosome
     */
    public static void storeTestCaseChromosomeCoverage(IChromosome<TestCase> chromosome) {
        if (Properties.COVERAGE() != Coverage.NO_COVERAGE
                && Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {
            Registry.getEnvironmentManager().storeCoverageData(
                    Properties.COVERAGE(),
                    chromosome.getValue().toString(),
                    null);
        }

        if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {
            BranchDistanceFitnessFunctionMultiObjective.retrieveFitnessValues(chromosome);
        } else if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
            LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
        }
    }

    /**
     * Store coverage data of a single TestCase of the given TestSuite based chromosome
     * @param chromosome store coverage for this chromosome
     * @param testCaseId store coverage for this TestCase
     */
    public static void storeTestSuiteChromosomeCoverage(
            IChromosome<TestSuite> chromosome,
            String testCaseId) {
        if (Properties.COVERAGE() != Coverage.NO_COVERAGE
                && Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {
            Registry.getEnvironmentManager().storeCoverageData(
                    Properties.COVERAGE(),
                    chromosome.getValue().toString(),
                    testCaseId);
        }

        if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {
            //Currently Unsupported
        } else if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
            //Currently Unsupported
        }
    }

    /**
     * Log the coverage value of the given chromosome
     * @param chromosome log coverage for this chromosome
     * @param <T> type of the chromosome
     */
    public static <T> void logChromosomeCoverage(IChromosome<T> chromosome) {
        if (Properties.COVERAGE() != Coverage.NO_COVERAGE
                && Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {
            MATE.log_acc("Coverage of chromosome " + chromosome.getValue().toString() + ": "
                    + Registry.getEnvironmentManager().getCoverage(
                    Properties.COVERAGE(),
                    chromosome.getValue().toString()));
        }
    }
}
