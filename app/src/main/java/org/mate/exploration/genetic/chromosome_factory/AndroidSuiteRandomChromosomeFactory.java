package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
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
            if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
                // TODO: should store coverage for the test case tc belonging to the testsuite ts
                Registry.getEnvironmentManager().storeCoverageData(Properties.COVERAGE(), chromosome.toString(), tc.toString());
            }
        }

        // TODO: create method 'finish' in TestSuite class and move code there
        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
            // TODO: should retrieve coverage of entire test suite
            MATE.log_acc("Coverage of: " + chromosome.toString() + ": "
                    + Registry.getEnvironmentManager().getCoverage(Properties.COVERAGE(),
                    chromosome.toString()));
        }

        return chromosome;
    }
}
