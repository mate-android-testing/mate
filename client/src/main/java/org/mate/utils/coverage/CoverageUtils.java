package org.mate.utils.coverage;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides utility functions to retrieve coverage-related information.
 */
public final class CoverageUtils {

    private CoverageUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    /**
     * Caches the activities of the AUT.
     */
    private static Set<String> activities = null;

    /**
     * Tracks for each chromosome which activities have been visited.
     */
    private static final Map<IChromosome, Set<String>> visitedActivities = new HashMap<>();

    /**
     * Copies the coverage data for the given test cases from a source chromosome to a
     * target chromosome.
     *
     * @param sourceChromosome The source chromosome.
     * @param targetChromosome The target chromosome.
     * @param testCases        The test cases for which coverage data should be copied over.
     */
    public static void copyCoverageData(IChromosome<TestSuite> sourceChromosome,
                                        IChromosome<TestSuite> targetChromosome, List<TestCase> testCases) {

        /*
         * We store (here copy) data about activity coverage in any case.
         * Since we request the activity coverage from the coverage map, we need to
         * keep it up-to-date. Note that we assume that there is no coverage data
         * for the target chromosome present yet.
         */
        Set<String> visitedActivitiesOfTestCases = new HashSet<>();
        for (TestCase testCase : testCases) {
            visitedActivitiesOfTestCases.addAll(testCase.getVisitedActivitiesOfApp());
        }

        if (visitedActivities.containsKey(targetChromosome)) {
            MATELog.log_warn("Overwriting coverage data for chromosome " + targetChromosome + "!");
        }

        visitedActivities.put(targetChromosome, visitedActivitiesOfTestCases);

        switch (Properties.COVERAGE()) {
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
            case ALL_COVERAGE:
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
            activities = new HashSet<>(Registry.getUiAbstractionLayer().getActivities());
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

        // store data about activity coverage in any case
        visitedActivities.put(chromosome, chromosome.getValue().getVisitedActivitiesOfApp());

        switch (Properties.COVERAGE()) {
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
            case ALL_COVERAGE:
                Registry.getEnvironmentManager().storeCoverageData(
                        Properties.COVERAGE(), chromosome, null);
                break;
            default:
                break;
        }
    }

    /**
     * Updates the activity coverage for the given test case chromosome. This is only necessary for
     * activities that have been discovered through an action not belonging to the test case.
     *
     * @param chromosome The test case chromosome.
     * @param activity The activity that has been visited.
     */
    public static <T> void updateTestCaseChromosomeActivityCoverage(IChromosome<T> chromosome,
                                                                String activity) {

        Set<String> visited = visitedActivities.getOrDefault(chromosome, new HashSet<>());
        visited.add(activity);
        visitedActivities.put(chromosome, visited);
    }

    /**
     * Stores the coverage data of a single test case within a test suite.
     *
     * @param chromosome The test suite.
     * @param testCase   The test case within the test suite.
     */
    public static void storeTestSuiteChromosomeCoverage(
            IChromosome<TestSuite> chromosome, TestCase testCase) {

        /*
         * We store data about activity coverage in any case. In particular,
         * we store here the activity coverage per test suite, not per test case.
         * This simplifies the implementation. In case someone needs access to the
         * coverage of the individual test cases, one has to iterate over the
         * test cases manually.
         */
        Set<String> visitedActivitiesByTestCase = testCase.getVisitedActivitiesOfApp();

        // merge with already visited activities of other test cases in the test suite
        if (visitedActivities.containsKey(chromosome)) {
            visitedActivitiesByTestCase.addAll(visitedActivities.get(chromosome));
        }

        visitedActivities.put(chromosome, visitedActivitiesByTestCase);

        switch (Properties.COVERAGE()) {
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
            case ALL_COVERAGE:
                Registry.getEnvironmentManager().storeCoverageData(
                        Properties.COVERAGE(), chromosome, testCase.getId());
                break;
            default:
                break;
        }
    }

    /**
     * Returns the activity coverage for the given chromosome.
     *
     * @param chromosome The chromosome for which the activity coverage should be evaluated.
     * @param <T>        Refers either to a test case or a test suite.
     * @return Returns the activity coverage of the given chromosome.
     */
    private static <T> double getActivityCoverage(IChromosome<T> chromosome) {

        if (!visitedActivities.containsKey(chromosome)) {
            throw new IllegalStateException("No visited activities for chromosome "
                    + chromosome + "!");
        }

        return (double) visitedActivities.get(chromosome).size() / getActivities().size() * 100;
    }

    /**
     * Log the coverage value of the given chromosome
     *
     * @param chromosome log coverage for this chromosome
     * @param <T>        type of the chromosome
     */
    public static <T> void logChromosomeCoverage(IChromosome<T> chromosome) {

        if (!visitedActivities.containsKey(chromosome)) {
            throw new IllegalStateException("No visited activities for chromosome "
                    + chromosome + "!");
        }

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:

                MATELog.log("Coverage of chromosome "
                        + chromosome.getValue().toString() + ": " + getActivityCoverage(chromosome));

                if (chromosome.getValue() instanceof TestSuite) {
                    for (TestCase testCase : ((TestSuite) chromosome.getValue()).getTestCases()) {
                        MATELog.log("Coverage of individual chromosome " + testCase + ": "
                                + getCoverage(Properties.COVERAGE(), (IChromosome<TestSuite>) chromosome, testCase));
                    }
                }
                break;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:

                // log activity coverage in any case
                MATELog.log("Activity coverage of chromosome "
                        + chromosome.getValue().toString() + ": " + getActivityCoverage(chromosome));

                MATELog.log("Coverage of chromosome " + chromosome.getValue().toString() + ": "
                        + Registry.getEnvironmentManager().getCoverage(
                        Properties.COVERAGE(), chromosome));

                if (chromosome.getValue() instanceof TestSuite) {
                    for (TestCase testCase : ((TestSuite) chromosome.getValue()).getTestCases()) {
                        MATELog.log("Coverage of individual chromosome " + testCase + ": "
                                + getCoverage(Properties.COVERAGE(), (IChromosome<TestSuite>) chromosome, testCase));
                    }
                }
                break;
            case ALL_COVERAGE:

                CoverageDTO coverageDTO = Registry.getEnvironmentManager().getCoverage(
                        Properties.COVERAGE(), chromosome);
                double activityCoverage = getActivityCoverage(chromosome);
                coverageDTO.setActivityCoverage(activityCoverage);

                MATELog.log("Coverage of chromosome "
                        + chromosome.getValue().toString() + ": " + coverageDTO);

                if (chromosome.getValue() instanceof TestSuite) {
                    for (TestCase testCase : ((TestSuite) chromosome.getValue()).getTestCases()) {
                        MATELog.log("Coverage of individual chromosome " + testCase + ": "
                                + getCoverage(Properties.COVERAGE(), (IChromosome<TestSuite>) chromosome, testCase));
                    }
                }
                break;
            default:
                break;
        }

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
            MATELog.log_acc("Intermediate coverage: " + getCombinedCoverage(Properties.COVERAGE()));
        }
    }

    /**
     * Logs the total coverage at the end of a run. As a side effect, the coverage of the last
     * test case is stored.
     */
    public static void logFinalCoverage() {

        // TODO: check if it is somehow possible to evaluate activity coverage of last test case
        if (Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {
            // store coverage of test case interrupted by timeout
            Registry.getEnvironmentManager().storeCoverageData(Properties.COVERAGE(),
                    "lastIncompleteTestCase", null);

            MATELog.log("Coverage of last test case: " +
                    Registry.getEnvironmentManager().getCoverage(Properties.COVERAGE()
                            , "lastIncompleteTestCase"));
        }

        // get combined coverage
        MATELog.log_acc("Total coverage: " + getCombinedCoverage(Properties.COVERAGE()));

        if (Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {
            MATELog.log_acc("Total activity coverage: "
                    + getCombinedCoverage(Coverage.ACTIVITY_COVERAGE).getActivityCoverage());
        }

        if (Properties.COVERAGE() == Coverage.ACTIVITY_COVERAGE) {

            Set<String> visitedActivitiesTotal = new HashSet<>();

            for (Set<String> activities : visitedActivities.values()) {
                visitedActivitiesTotal.addAll(activities);
            }

            MATELog.log_acc("Total visited activities: ");
            for (String activity : visitedActivitiesTotal) {
                if (Registry.getUiAbstractionLayer().getActivities().contains(activity)) {
                    MATELog.log_acc(activity);
                }
            }
        }
    }

    /**
     * Returns the total coverage for the given coverage type.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @return Returns the total coverage.
     */
    public static CoverageDTO getCombinedCoverage(Coverage coverage) {

        switch (coverage) {
            case ACTIVITY_COVERAGE:
                CoverageDTO activityCoverageDTO = new CoverageDTO();
                activityCoverageDTO.setActivityCoverage(getActivityCoverage());
                return activityCoverageDTO;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return Registry.getEnvironmentManager().getCombinedCoverage(coverage, null);
            case ALL_COVERAGE:
                CoverageDTO coverageDTO = Registry.getEnvironmentManager()
                        .getCombinedCoverage(coverage, null);
                coverageDTO.setActivityCoverage(getActivityCoverage());
                return coverageDTO;
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }

    /**
     * Computes the total activity coverage.
     *
     * @return Returns the total activity coverage.
     */
    private static double getActivityCoverage() {

        Set<String> visitedActivitiesTotal = new HashSet<>();

        for (Set<String> activities : visitedActivities.values()) {
            visitedActivitiesTotal.addAll(activities);
        }

        return (double) visitedActivitiesTotal.size() / getActivities().size() * 100;
    }

    /**
     * Returns the total coverage for the given coverage type and the specified list of
     * chromosomes.
     *
     * @param coverage    The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosomes A list of chromosomes.
     * @param <T>         The type parameter referring to test cases or test suites.
     * @return Returns the total coverage for the specified chromosomes.
     */
    public static <T> CoverageDTO getCombinedCoverage(Coverage coverage, List<IChromosome<T>> chromosomes) {

        switch (coverage) {
            case ACTIVITY_COVERAGE:
                CoverageDTO activityCoverageDTO = new CoverageDTO();
                activityCoverageDTO.setActivityCoverage(getActivityCoverage(chromosomes));
                return activityCoverageDTO;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
                return Registry.getEnvironmentManager().getCombinedCoverage(coverage, chromosomes);
            case ALL_COVERAGE:
                CoverageDTO coverageDTO = Registry.getEnvironmentManager()
                        .getCombinedCoverage(coverage, chromosomes);
                coverageDTO.setActivityCoverage(getActivityCoverage(chromosomes));
                return coverageDTO;
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }

    /**
     * Computes the activity coverage for the given chromosomes.
     *
     * @param chromosomes The chromosomes for which activity coverage should be evaluated.
     * @param <T>         The type wrapped by the chromosomes.
     * @return Returns the activity coverage for the given chromosomes.
     */
    private static <T> double getActivityCoverage(List<IChromosome<T>> chromosomes) {

        Set<String> visitedActivitiesTotal = new HashSet<>();

        for (IChromosome<T> chromosome : chromosomes) {

            if (!visitedActivities.containsKey(chromosome)) {
                throw new IllegalStateException("No visited activities for chromosome "
                        + chromosome + "!");
            }
            visitedActivitiesTotal.addAll(visitedActivities.get(chromosome));
        }

        return (double) visitedActivitiesTotal.size() / getActivities().size() * 100;
    }

    /**
     * A convenient function to retrieve the coverage of a single test case within a test suite.
     *
     * @param coverage  The coverage type, e.g. BRANCH_COVERAGE.
     * @param testSuite The test suite.
     * @param testCase  The single test case within the test suite.
     * @return Returns the coverage for the given test case.
     */
    public static CoverageDTO getCoverage(Coverage coverage, IChromosome<TestSuite> testSuite, TestCase testCase) {

        switch (coverage) {

            case ACTIVITY_COVERAGE:
                /*
                 * We store the activity coverage per test suite, hence we can't request that
                 * information from the visited activities coverage map.
                 */
                CoverageDTO activityCoverageDTO = new CoverageDTO();
                activityCoverageDTO.setActivityCoverage(getActivityCoverage(testCase));
                return activityCoverageDTO;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return Registry.getEnvironmentManager()
                        .getCoverage(coverage, testSuite.getValue().getId(), testCase.getId());
            case ALL_COVERAGE:
                CoverageDTO coverageDTO = Registry.getEnvironmentManager()
                        .getCoverage(coverage, testSuite.getValue().getId(), testCase.getId());
                coverageDTO.setActivityCoverage(getActivityCoverage(testCase));
                return coverageDTO;
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }

    /**
     * Computes the activity coverage for the given test case.
     *
     * @param testCase The test case for which the activity coverage should be evaluated.
     * @return Returns the activity coverage of the given test case.
     */
    private static double getActivityCoverage(TestCase testCase) {
        return (double) testCase.getVisitedActivitiesOfApp().size() / getActivities().size() * 100;
    }

    /**
     * A convenient function to retrieve the coverage of a single chromosome.
     *
     * @param coverage   The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosome The given chromosome.
     * @param <T>        The type parameter indicating a test case or a test suite.
     * @return Returns the coverage for the given chromosome.
     */
    public static <T> CoverageDTO getCoverage(Coverage coverage, IChromosome<T> chromosome) {

        switch (coverage) {
            case ACTIVITY_COVERAGE:
                CoverageDTO activityCoverageDTO = new CoverageDTO();
                activityCoverageDTO.setActivityCoverage(getActivityCoverage(chromosome));
                return activityCoverageDTO;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
            case METHOD_COVERAGE:
            case BASIC_BLOCK_LINE_COVERAGE:
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return Registry.getEnvironmentManager().getCoverage(coverage, chromosome);
            case ALL_COVERAGE:
                CoverageDTO coverageDTO = Registry.getEnvironmentManager()
                        .getCoverage(coverage, chromosome);
                coverageDTO.setActivityCoverage(getActivityCoverage(chromosome));
                return coverageDTO;
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }
}
