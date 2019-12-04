package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.ui.EnvironmentManager;

public class AndroidSuiteRandomChromosomeFactory implements IChromosomeFactory<TestSuite> {
    public static final String CHROMOSOME_FACTORY_ID = "android_suite_random_chromosome_factory";

    private final int numTestCases;
    private final AndroidRandomChromosomeFactory androidRandomChromosomeFactory;
    private final boolean storeCoverage;

    public AndroidSuiteRandomChromosomeFactory(boolean storeCoverage, int numTestCases, int
            maxNumEvents) {
        this.storeCoverage = storeCoverage;
        this.numTestCases = numTestCases;
        androidRandomChromosomeFactory = new AndroidRandomChromosomeFactory(false, true, maxNumEvents);
    }

    public AndroidSuiteRandomChromosomeFactory(int numTestCases, int maxNumEvents) {
        this(Properties.STORE_COVERAGE(), numTestCases, maxNumEvents);
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
            if (storeCoverage) {
                Registry.getEnvironmentManager().storeCoverageData(chromosome, tc);
            }
        }

        if (storeCoverage) {
            MATE.log_acc("Coverage of: " + chromosome.toString() + ": " + Registry.getEnvironmentManager()
                    .getCoverage(chromosome));
        }

        return chromosome;
    }
}
