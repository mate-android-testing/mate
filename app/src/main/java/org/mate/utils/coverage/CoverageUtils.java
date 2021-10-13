package org.mate.utils.coverage;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CoverageUtils {

    private CoverageUtils() {
    }

    // cache activities for activity coverage
    private static Set<String> activities = null;

    // tracks for each chromosome which activities have been visited
    private static Map<IChromosome, Set<String>> visitedActivities = new HashMap<>();

    /**
     * Copies the coverage data for the given test cases from a source chromosome to a
     * target chromosome.
     *
     * @param sourceChromosome The source chromosome.
     * @param targetChromosome The target chromosome.
     * @param testCases The test cases for which coverage data should be copied over.
     */
    public static void copyCoverageData(IChromosome<TestSuite> sourceChromosome,
                                        IChromosome<TestSuite> targetChromosome, List<TestCase> testCases) {

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:
                /*
                * Since we request the activity coverage from the coverage map, we need to
                * keep it up-to-date. Note that we assume that there is no coverage data
                * for the target chromosome present yet.
                 */
                Set<String> visitedActivitiesOfTestCases = new HashSet<>();
                for (TestCase testCase : testCases) {
                    visitedActivitiesOfTestCases.addAll(testCase.getVisitedActivities());
                }

                if (visitedActivities.containsKey(targetChromosome)) {
                    MATE.log_warn("Overwriting coverage data for chromosome " + targetChromosome + "!");
                }

                visitedActivities.put(targetChromosome, visitedActivitiesOfTestCases);
                break;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                Registry.getEnvironmentManager().copyCoverageData(sourceChromosome, targetChromosome, testCases);
                break;
            default:
                break;
        }
    }

    /**
     * Retrieves the activities of the AUT.
     *
     * @return Return a set of activities.
     */
    private static Set<String> getActivities() {
        if (activities == null) {
            activities = new HashSet<>(Registry.getEnvironmentManager().getActivityNames());
        }

        if (activities.size() == 0) {
            // TODO: app with 0 activities is unlikely
            throw new IllegalStateException("Couldn't derive the list of activities!");
        }

        return activities;
    }

    /**
     * Stores coverage data for the given test case chromosome.
     *
     * @param chromosome The test case chromosome.
     */
    public static void storeTestCaseChromosomeCoverage(IChromosome<TestCase> chromosome) {

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:
                visitedActivities.put(chromosome, chromosome.getValue().getVisitedActivities());
                break;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                Registry.getEnvironmentManager().storeCoverageData(
                        Properties.COVERAGE(), chromosome, null);
                break;
            default:
                break;
        }
    }

    /**
     * Stores the coverage data of a single test case within a test suite.
     *
     * @param chromosome The test suite.
     * @param testCase The test case within the test suite.
     */
    public static void storeTestSuiteChromosomeCoverage(
            IChromosome<TestSuite> chromosome, TestCase testCase) {

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:
                /*
                * We store here the activity coverage per test suite, not per test case.
                * This simplifies the implementation. In case someone needs access to the
                * coverage of the individual test cases, one has to iterate over the
                * test cases manually.
                 */
                Set<String> visitedActivitiesByTestCase = testCase.getVisitedActivities();

                // merge with already visited activities of other test cases in the test suite
                if (visitedActivities.containsKey(chromosome)) {
                    visitedActivitiesByTestCase.addAll(visitedActivities.get(chromosome));
                }

                visitedActivities.put(chromosome, visitedActivitiesByTestCase);
                break;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                Registry.getEnvironmentManager().storeCoverageData(
                        Properties.COVERAGE(), chromosome, testCase.getId());
                break;
            default:
                break;
        }
    }

    /**
     * Log the coverage value of the given chromosome
     *
     * @param chromosome log coverage for this chromosome
     * @param <T>        type of the chromosome
     */
    public static <T> void logChromosomeCoverage(IChromosome<T> chromosome) {

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:

                if (!visitedActivities.containsKey(chromosome)) {
                    throw new IllegalStateException("No visited activities for chromosome "
                            + chromosome + "!");
                }

                double activityCoverage = (double) visitedActivities.get(chromosome).size()
                        / getActivities().size() * 100;
                MATE.log("Coverage of chromosome "
                        + chromosome.getValue().toString() + ": " + activityCoverage);

                if (chromosome.getValue() instanceof TestSuite) {
                    for (TestCase testCase : ((TestSuite) chromosome.getValue()).getTestCases()) {
                        MATE.log("Coverage of individual chromosome " + testCase + ": "
                                + getCoverage(Properties.COVERAGE(), (IChromosome<TestSuite>) chromosome, testCase));
                    }
                }

                break;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                MATE.log("Coverage of chromosome " + chromosome.getValue().toString() + ": "
                        + Registry.getEnvironmentManager().getCoverage(
                        Properties.COVERAGE(), chromosome));

                if (chromosome.getValue() instanceof TestSuite) {
                    for (TestCase testCase : ((TestSuite) chromosome.getValue()).getTestCases()) {
                        MATE.log("Coverage of individual chromosome " + testCase + ": "
                                + getCoverage(Properties.COVERAGE(), (IChromosome<TestSuite>) chromosome, testCase));
                    }
                }
                break;
            default:
                break;
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

            MATE.log("Coverage of last test case: " +
                    Registry.getEnvironmentManager().getCoverage(Properties.COVERAGE()
                            , "lastIncompleteTestCase"));
        }

        // get combined coverage
        MATE.log_acc("Total coverage: " + getCombinedCoverage(Properties.COVERAGE()));

        if (Properties.COVERAGE() == Coverage.ACTIVITY_COVERAGE) {

            Set<String> visitedActivitiesTotal = new HashSet<>();

            for (Set<String> activities : visitedActivities.values()) {
                visitedActivitiesTotal.addAll(activities);
            }

            MATE.log_acc("Visited Activities: ");
            for (String activity : visitedActivitiesTotal) {
                    MATE.log_acc(activity);
            }
        }
    }

    /**
     *  Returns the total coverage for the given coverage type.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @return Returns the total coverage.
     */
    public static double getCombinedCoverage(Coverage coverage) {

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:
                Set<String> visitedActivitiesTotal = new HashSet<>();

                for (Set<String> activities : visitedActivities.values()) {
                    visitedActivitiesTotal.addAll(activities);
                }

                return (double) visitedActivitiesTotal.size() / getActivities().size() * 100;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return Registry.getEnvironmentManager().getCombinedCoverage(coverage, null);
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }

    /**
     * Returns the total coverage for the given coverage type and the specified list of
     * chromosomes.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosomes A list of chromosomes.
     * @param <T> The type parameter referring to test cases or test suites.
     * @return Returns the total coverage for the specified chromosomes.
     */
    public static <T> double getCombinedCoverage(Coverage coverage, List<IChromosome<T>> chromosomes) {

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:
                Set<String> visitedActivitiesTotal = new HashSet<>();

                for (IChromosome<T> chromosome : chromosomes) {

                    if (!visitedActivities.containsKey(chromosome)) {
                        throw new IllegalStateException("No visited activities for chromosome "
                                + chromosome + "!");
                    }

                    visitedActivitiesTotal.addAll(visitedActivities.get(chromosome));
                }

                return (double) visitedActivitiesTotal.size() / getActivities().size() * 100;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
                return Registry.getEnvironmentManager().getCombinedCoverage(coverage, chromosomes);
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }

    /**
     * A convenient function to retrieve the coverage of a single test case within a test suite.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @param testSuite The test suite.
     * @param testCase The single test case within the test suite.
     * @return Returns the coverage for the given test case.
     */
    public static double getCoverage(Coverage coverage, IChromosome<TestSuite> testSuite, TestCase testCase) {

        switch (Properties.COVERAGE()) {

            case ACTIVITY_COVERAGE:
                /*
                 * We store the activity coverage per test suite, hence we can't request that
                 * information from the visited activities coverage map.
                 */
                return (double) testCase.getVisitedActivities().size() / getActivities().size() * 100;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(coverage, testSuite.getValue().getId(), testCase.getId());
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }

    /**
     * A convenient function to retrieve the coverage of a single chromosome.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosome The given chromosome.
     * @param <T> The type parameter indicating a test case or a test suite.
     * @return Returns the coverage for the given chromosome.
     */
    public static <T> double getCoverage(Coverage coverage, IChromosome<T> chromosome) {

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:

                if (!visitedActivities.containsKey(chromosome)) {
                    throw new IllegalStateException("No visited activities for chromosome "
                            + chromosome + "!");
                }

                return (double) visitedActivities.get(chromosome).size() / getActivities().size() * 100;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return Registry.getEnvironmentManager().getCoverage(coverage, chromosome);
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }
}
