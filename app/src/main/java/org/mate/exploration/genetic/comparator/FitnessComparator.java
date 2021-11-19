package org.mate.exploration.genetic.comparator;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;

import java.util.Comparator;

/**
 * Provides a comparator that compares two {@link IChromosome}s based on its fitness value.
 *
 * @param <T> The type of the chromosomes.
 */
public class FitnessComparator<T> implements Comparator<IChromosome<T>> {

    /**
     * The fitness function used for the comparison.
     */
    private final IFitnessFunction<T> fitnessFunction;

    /**
     * Initialises the fitness comparator with the given fitness function.
     *
     * @param fitnessFunction The fitness function used for the comparison.
     */
    public FitnessComparator(IFitnessFunction<T> fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    /**
     * Compares two {@link IChromosome}s based on its fitness. Sorts the chromosomes based
     * on the ascending order of magnitude, i.e. the worst chromosome comes first.
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

        if (fitnessFunction.isMaximizing()) {
            return Double.compare(fitnessChromosome1, fitnessChromosome2);
        } else {
            return Double.compare(fitnessChromosome2, fitnessChromosome1);
        }
    }
}
