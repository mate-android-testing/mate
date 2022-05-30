package org.mate.exploration.genetic.fitness;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

/**
 * Provides a fitness function that considers the test length as objective, where shorter tests
 * are considered better.
 *
 * @param <T> The type of the chromosomes.
 */
public class TestLengthFitnessFunction<T> implements IFitnessFunction<T> {


    /**
     * The fitness of the given chromosome is simply the test length, i.e. the number of actions.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the fitness of the chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {
        if (chromosome.getValue() instanceof TestCase) {
            return ((TestCase) chromosome.getValue()).getActionSequence().size();
        } else if (chromosome.getValue() instanceof TestSuite) {
            int length = 0;
            for (TestCase testCase : ((TestSuite) chromosome.getValue()).getTestCases()) {
                length += testCase.getActionSequence().size();
            }
            return length;
        } else {
            throw new UnsupportedOperationException("Chromosome type "
                    + chromosome.getValue().getClass() + " not yet supported!");
        }
    }

    /**
     * Whether this fitness function is maximising or minimising.
     *
     * @return Returns {@code false} is the objective of this fitness function is to minimise
     *          the test length.
     */
    @Override
    public boolean isMaximizing() {
        return false;
    }

    /**
     * Normalizes the fitness function to return a fitness value in the range [0,1]. The test
     * length is simply divided by the maximal number of possible actions.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalized fitness value in range [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        if (chromosome.getValue() instanceof TestCase) {
            return getFitness(chromosome) / Properties.MAX_NUMBER_EVENTS();
        } else if (chromosome.getValue() instanceof TestSuite) {
            int maxLength = Properties.NUMBER_TESTCASES() * Properties.MAX_NUMBER_EVENTS();
            return getFitness(chromosome) / maxLength;
        } else {
            throw new UnsupportedOperationException("Chromosome type "
                    + chromosome.getValue().getClass() + "not yet supported!");
        }
    }
}
