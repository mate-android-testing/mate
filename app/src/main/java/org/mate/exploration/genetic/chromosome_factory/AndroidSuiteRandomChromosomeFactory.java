package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.CoverageUtils;

/**
 * Provides a chromosome factory that generates {@link TestSuite}s consisting of {@link TestCase}s
 * that in turn consists of random {@link org.mate.interaction.action.ui.UIAction}s.
 */
public class AndroidSuiteRandomChromosomeFactory implements IChromosomeFactory<TestSuite> {

    /**
     * The number of test cases per test suite.
     */
    private final int numTestCases;

    /**
     * The chromosome factory used to generate the individual {@link TestCase}s.
     */
    private final AndroidRandomChromosomeFactory androidRandomChromosomeFactory;

    /**
     * Initialises a new chromosome that is capable of generating random {@link TestSuite}s.
     *
     * @param numTestCases The number of test cases per test suite.
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public AndroidSuiteRandomChromosomeFactory(int numTestCases, int maxNumEvents) {
        this.numTestCases = numTestCases;
        androidRandomChromosomeFactory = new AndroidRandomChromosomeFactory( true, maxNumEvents);
        androidRandomChromosomeFactory.setTestSuiteExecution(true);
    }

    /**
     * Creates a new chromosome that wraps a {@link TestSuite}. Note that the test suite is
     * inherently executed.
     *
     * @return Returns the generated test suite chromosome.
     */
    @Override
    public IChromosome<TestSuite> createChromosome() {
        TestSuite ts = new TestSuite();
        IChromosome<TestSuite> chromosome = new Chromosome<>(ts);
        MATE.log_acc("Android Suite Random Chromosome Factory: creating chromosome: " + chromosome);
        for (int i = 0; i < numTestCases; i++) {
            TestCase tc = androidRandomChromosomeFactory.createChromosome().getValue();
            MATE.log_acc("With test case: " + tc);
            ts.getTestCases().add(tc);
            FitnessUtils.storeTestSuiteChromosomeFitness(chromosome, tc);
            CoverageUtils.storeTestSuiteChromosomeCoverage(chromosome, tc);
        }
        CoverageUtils.logChromosomeCoverage(chromosome);
        return chromosome;
    }
}
