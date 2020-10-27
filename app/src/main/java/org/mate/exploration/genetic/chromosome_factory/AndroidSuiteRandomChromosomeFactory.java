package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.Coverage;

public class AndroidSuiteRandomChromosomeFactory implements IChromosomeFactory<TestSuite> {
    public static final String CHROMOSOME_FACTORY_ID = "android_suite_random_chromosome_factory";

    private final int numTestCases;
    private final AndroidRandomChromosomeFactory androidRandomChromosomeFactory;

    public AndroidSuiteRandomChromosomeFactory(int numTestCases, int
            maxNumEvents) {
        this.numTestCases = numTestCases;
        androidRandomChromosomeFactory = new AndroidRandomChromosomeFactory( true, maxNumEvents);
    }

    @Override
    public IChromosome<TestSuite> createChromosome() {
        TestSuite ts = new TestSuite();
        IChromosome<TestSuite> chromosome = new Chromosome<>(ts);
        MATE.log_acc("Android Suite Random Chromosome Factory: creating chromosome: " + chromosome);
        for (int i = 0; i < numTestCases; i++) {
            TestCase tc = androidRandomChromosomeFactory.createChromosome().getValue();
            MATE.log_acc("With test case: " + tc);
            ts.getTestCases().add(tc);

            if (Properties.FITNESS_FUNCTION().equals(BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID)) {
                // store branch distance data for test case tc belonging to the testsuite ts
                Registry.getEnvironmentManager().storeBranchDistanceData(chromosome.toString(), tc.toString());
            }

            if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
                // store coverage data for the test case tc belonging to the testsuite ts
                Registry.getEnvironmentManager().storeCoverageData(Properties.COVERAGE(),
                        chromosome.toString(), tc.toString());
            }
        }

        // TODO: remove after debugging
        if (Properties.FITNESS_FUNCTION().equals(BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID)) {
            MATE.log("Branch Distance of: " + chromosome.toString() + ": "
                + Registry.getEnvironmentManager().getBranchDistance(chromosome.toString()));

        }

        // TODO: create method 'finish' in TestSuite class and move code there
        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
            // retrieve coverage of entire test suite
            MATE.log_acc("Coverage of: " + chromosome.toString() + ": "
                    + Registry.getEnvironmentManager().getCoverage(Properties.COVERAGE(),
                    chromosome.toString()));
        }

        return chromosome;
    }
}
