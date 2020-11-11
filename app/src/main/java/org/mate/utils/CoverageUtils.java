package org.mate.utils;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.List;

public class CoverageUtils {

    private CoverageUtils() {
    }



    /**
     * Store coverage data for the given TestCase based chromosome
     *
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

        // TODO: try to get rid of this
        if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
            LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
        }
    }

    /**
     * Store coverage data of a single TestCase of the given TestSuite based chromosome
     *
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

        if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
            //Currently Unsupported
        }
    }

    /**
     * Log the coverage value of the given chromosome
     *
     * @param chromosome log coverage for this chromosome
     * @param <T>        type of the chromosome
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

    /**
     * Logs the total coverage at the end of a run.
     * As a side effect, the coverage of the last test case is stored.
     */
    public static void logFinalCoverage() {

        // TODO: check if it is somehow possible to evaluate activity coverage of last test case
        if (Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {
            // store coverage of test case interrupted by timeout
            Registry.getEnvironmentManager().storeCoverageData(Properties.COVERAGE(),
                    "lastIncompleteTestCase", null);

            MATE.log_acc("Coverage of last test case: " +
                    Registry.getEnvironmentManager().getCoverage(Properties.COVERAGE()
                            ,"lastIncompleteTestCase"));
        }

        // get combined coverage
        MATE.log_acc("Total coverage: " + getCombinedCoverage(Properties.COVERAGE()));
    }

    public static double getCombinedCoverage(Coverage coverage) {
        if (coverage == Coverage.ACTIVITY_COVERAGE) {
            throw new UnsupportedOperationException("Activity coverage not yet supported!");
        } else {
            return Registry.getEnvironmentManager().getCombinedCoverage(coverage, null);
        }
    }

    public static <T> double getCombinedCoverage(Coverage coverage, List<IChromosome<T>> chromosomes) {
        if (coverage == Coverage.ACTIVITY_COVERAGE) {
            throw new UnsupportedOperationException("Activity coverage not yet supported!");
        } else {
            return Registry.getEnvironmentManager().getCombinedCoverage(coverage, chromosomes);
        }
    }

    public static double getCoverage(Coverage coverage, String chromosomeId) {
        if (coverage == Coverage.ACTIVITY_COVERAGE) {
            throw new UnsupportedOperationException("Activity coverage not yet supported!");
        } else {
            return Registry.getEnvironmentManager().getCoverage(coverage, chromosomeId);
        }
    }
}
