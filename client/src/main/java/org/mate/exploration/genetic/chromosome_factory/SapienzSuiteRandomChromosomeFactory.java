package org.mate.exploration.genetic.chromosome_factory;

import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.CoverageUtils;

/**
 * Provides a chromosome factory as described in the Sapienz paper, i.e. it generates test suites
 * which in turn consists of test cases that in turn consists of atomic (primitive) and motif genes.
 * Requires that the property {@link Properties#WIDGET_BASED_ACTIONS()} is set to {@code false}.
 */
public class SapienzSuiteRandomChromosomeFactory implements IChromosomeFactory<TestSuite> {

    /**
     * The number of test cases per test suite.
     */
    private final int numTestCases;

    /**
     * A special chromosome factory that produces test cases consisting of atomic and motif genes
     * as described in the Sapienz paper.
     */
    private final SapienzRandomChromosomeFactory sapienzRandomChromosomeFactory;

    /**
     * Initialises the chromosome factory with the number of test cases per test suite and the
     * maximal number of events/actions per test case.
     *
     * @param numTestCases The number of test cases per test suite.
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public SapienzSuiteRandomChromosomeFactory(int numTestCases, int maxNumEvents) {
        this.numTestCases = numTestCases;
        sapienzRandomChromosomeFactory = new SapienzRandomChromosomeFactory( true, maxNumEvents);
        sapienzRandomChromosomeFactory.setTestSuiteExecution(true);
    }

    /**
     * Generates a new chromosome that is a wrapper around a {@link TestSuite}. The chromosome factory
     * uses the underlying {@link SapienzRandomChromosomeFactory} to produce the individual test
     * cases of the test suite.
     *
     * @return Returns the generated chromosome.
     */
    @Override
    public IChromosome<TestSuite> createChromosome() {
        TestSuite ts = new TestSuite();
        IChromosome<TestSuite> chromosome = new Chromosome<>(ts);
        MATELog.log_acc("Sapienz Suite Random Chromosome Factory: creating chromosome: " + chromosome);
        for (int i = 0; i < numTestCases; i++) {
            TestCase tc = sapienzRandomChromosomeFactory.createChromosome().getValue();
            MATELog.log_acc("With test case: " + tc);
            ts.getTestCases().add(tc);
            FitnessUtils.storeTestSuiteChromosomeFitness(chromosome, tc);
            CoverageUtils.storeTestSuiteChromosomeCoverage(chromosome, tc);
        }
        CoverageUtils.logChromosomeCoverage(chromosome);
        return chromosome;
    }
}
