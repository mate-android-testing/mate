package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.coverage.CoverageUtils;
import org.mate.utils.FitnessUtils;

public class AndroidSuiteRandomChromosomeFactory implements IChromosomeFactory<TestSuite> {
    public static final String CHROMOSOME_FACTORY_ID = "android_suite_random_chromosome_factory";

    private final int numTestCases;
    private final AndroidRandomChromosomeFactory androidRandomChromosomeFactory;

    public AndroidSuiteRandomChromosomeFactory(int numTestCases, int
            maxNumEvents) {
        this.numTestCases = numTestCases;
        androidRandomChromosomeFactory = new AndroidRandomChromosomeFactory( true, maxNumEvents);
        androidRandomChromosomeFactory.setTestSuiteExecution(true);
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
            FitnessUtils.storeTestSuiteChromosomeFitness(chromosome, tc.toString());
            CoverageUtils.storeTestSuiteChromosomeCoverage(chromosome, tc.toString());
        }
        CoverageUtils.logChromosomeCoverage(chromosome);
        return chromosome;
    }
}
