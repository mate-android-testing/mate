package org.mate.utils;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
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
     * Store coverage data for the given TestCase based chromosome
     *
     * @param chromosome store coverage for this chromosome
     */
    public static void storeTestCaseChromosomeCoverage(IChromosome<TestCase> chromosome) {

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:
                visitedActivities.put(chromosome, chromosome.getValue().getVisitedActivities());
                break;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
                Registry.getEnvironmentManager().storeCoverageData(
                        Properties.COVERAGE(),
                        chromosome.getValue().toString(),
                        null);
                break;
            default:
                break;
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

        switch (Properties.COVERAGE()) {
            case ACTIVITY_COVERAGE:
                // only add the visited activities by the specified test case
                Set<String> visitedActivitiesByTestCase = new HashSet<>();
                for (TestCase testCase : chromosome.getValue().getTestCases()) {
                    if (testCase.toString().equals(testCaseId)) {
                        visitedActivitiesByTestCase.addAll(testCase.getVisitedActivities());
                    }
                }

                // merge with already visited activities of other test cases in the test suite
                if (visitedActivities.containsKey(chromosome)) {
                    visitedActivitiesByTestCase.addAll(visitedActivities.get(chromosome));
                }

                visitedActivities.put(chromosome, visitedActivitiesByTestCase);
                break;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
                Registry.getEnvironmentManager().storeCoverageData(
                        Properties.COVERAGE(),
                        chromosome.getValue().toString(),
                        testCaseId);
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
                MATE.log_acc("Coverage of chromosome "
                        + chromosome.getValue().toString() + ": " + activityCoverage);
                break;
            case BRANCH_COVERAGE:
            case LINE_COVERAGE:
                MATE.log_acc("Coverage of chromosome " + chromosome.getValue().toString() + ": "
                        + Registry.getEnvironmentManager().getCoverage(
                        Properties.COVERAGE(),
                        chromosome.getValue().toString()));
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

            MATE.log_acc("Coverage of last test case: " +
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
                return Registry.getEnvironmentManager().getCombinedCoverage(coverage, null);
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }

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
                return Registry.getEnvironmentManager().getCombinedCoverage(coverage, chromosomes);
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }

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
                return Registry.getEnvironmentManager().getCoverage(coverage, chromosome.toString());
            default:
                throw new UnsupportedOperationException("Coverage type not yet supported!");
        }
    }
}
