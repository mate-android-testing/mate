package org.mate.exploration.genetic;

import org.mate.MATE;
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
        androidRandomChromosomeFactory = new AndroidRandomChromosomeFactory(maxNumEvents);
    }

    public AndroidSuiteRandomChromosomeFactory(int numTestCases, int maxNumEvents) {
        this(true, numTestCases, maxNumEvents);
    }

    @Override
    public IChromosome<TestSuite> createChromosome() {
        TestSuite ts = new TestSuite();
        IChromosome<TestSuite> chromosome = new Chromosome<>(ts);
        for (int i = 0; i < numTestCases; i++) {
            TestCase tc = androidRandomChromosomeFactory.createChromosome().getValue();
            ts.getTestCases().add(tc);
            EnvironmentManager.storeCoverageData(chromosome, tc);
        }
        return chromosome;
    }
}
