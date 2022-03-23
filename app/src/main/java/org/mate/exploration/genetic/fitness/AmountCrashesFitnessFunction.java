package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

/**
 * Provides a fitness function that considers the number of crashes as objective.
 */
public class AmountCrashesFitnessFunction<T> implements IFitnessFunction<T> {

    /**
     * The fitness of the given chromosome is simply the number of discovered crashes.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the fitness of the chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {

        if (chromosome.getValue() instanceof TestSuite) {
            TestSuite testSuite = (TestSuite) chromosome.getValue();
            int amountCrashes = 0;
            for (TestCase testCase : testSuite.getTestCases()) {
                amountCrashes += testCase.hasCrashDetected() ? 1 : 0;
            }
            return amountCrashes;
        } else if (chromosome.getValue() instanceof TestCase) {
            TestCase testCase = (TestCase) chromosome.getValue();
            return testCase.hasCrashDetected() ? 1 : 0;
        } else {
            throw new UnsupportedOperationException("Chromosome type "
                    + chromosome.getValue().getClass() + " not yet supported!");
        }
    }

    /**
     * Whether this fitness function is maximising or minimising.
     *
     * @return Returns {@code true} since the objective is to maximise the number of crashes.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns a normalised fitness value bounded in [0,1].
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value.
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        if (chromosome.getValue() instanceof TestSuite) {
            // since a test case can only produce a single crash, we divide by the number of test cases
            return getFitness(chromosome) / ((TestSuite) chromosome.getValue()).getTestCases().size();
        } else if (chromosome.getValue() instanceof TestCase) {
            // a test case can only produce a single crash, thus the fitness value is already in [0,1]
            return getFitness(chromosome);
        } else {
            throw new UnsupportedOperationException("Chromosome type "
                    + chromosome.getValue().getClass() + "not yet supported!");
        }
    }
}
