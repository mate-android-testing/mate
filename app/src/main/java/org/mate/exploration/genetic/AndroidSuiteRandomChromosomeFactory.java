package org.mate.exploration.genetic;

import org.mate.model.TestSuite;

public class AndroidSuiteRandomChromosomeFactory implements IChromosomeFactory<TestSuite> {
    public static final String CHROMOSOME_FACTORY_ID = "android_suite_random_chromosome_factory";

    private final int numTestCases;
    private final AndroidRandomChromosomeFactory androidRandomChromosomeFactory;

    public AndroidSuiteRandomChromosomeFactory(int numTestCases, int maxNumEvents) {
        this.numTestCases = numTestCases;
        androidRandomChromosomeFactory = new AndroidRandomChromosomeFactory(maxNumEvents);
    }

    @Override
    public IChromosome<TestSuite> createChromosome() {
        TestSuite ts = new TestSuite();
        for (int i = 0; i < numTestCases; i++) {
            ts.getTestCases().add(androidRandomChromosomeFactory.createChromosome().getValue());
        }
        return new Chromosome<>(ts);
    }
}
