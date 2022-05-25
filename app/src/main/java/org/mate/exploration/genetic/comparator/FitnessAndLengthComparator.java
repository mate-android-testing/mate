package org.mate.exploration.genetic.comparator;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.Comparator;

/**
 * Provides a comparator that compares two {@link IChromosome}s primarily based on its fitness values
 * and in case of a tie, the two {@link IChromosome}s are compared based on its lengths.
 *
 * @param <T> The type of the chromosomes.
 */
public class FitnessAndLengthComparator<T> implements Comparator<IChromosome<T>> {

    /**
     * The fitness function used for the comparison.
     */
    private final IFitnessFunction<T> fitnessFunction;

    /**
     * Initialises the fitness length comparator with the given fitness function.
     *
     * @param fitnessFunction The fitness function used for the comparison.
     */
    public FitnessAndLengthComparator(IFitnessFunction<T> fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    /**
     * Compares two {@link IChromosome}s based on its fitness and its length. Sorts the chromosomes
     * in ascending order of the fitness magnitude and in descending order of the length.
     *
     * @param o1 The first chromosome.
     * @param o2 The second chromosome.
     * @return Returns a value of {@code -1} if o1 is worse than o2 or a value of {@code 1}
     *          if o1 is better than o2. If both chromosomes share the same fitness, a value of
     *          {@code 0} is returned.
     */
    @Override
    public int compare(IChromosome<T> o1, IChromosome<T> o2) {

        double fitnessChromosome1 = fitnessFunction.getNormalizedFitness(o1);
        double fitnessChromosome2 = fitnessFunction.getNormalizedFitness(o2);

        if (fitnessChromosome1 == fitnessChromosome2) {
            // compare on length, sorts in descending order
            return getChromosomeLength(o2) - getChromosomeLength(o1);
        } else {
            // compare on fitness, sorts in ascending order
            if (fitnessFunction.isMaximizing()) {
                return Double.compare(fitnessChromosome1, fitnessChromosome2);
            } else {
                return Double.compare(fitnessChromosome2, fitnessChromosome1);
            }
        }
    }

    /**
     * Determines the length of the given chromosome.
     *
     * @param chromosome The chromosome for which its length should be determined.
     * @return Returns the length of the given chromosome.
     */
    private int getChromosomeLength(IChromosome<T> chromosome) {

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
}
