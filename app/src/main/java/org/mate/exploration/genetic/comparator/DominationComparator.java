package org.mate.exploration.genetic.comparator;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;

import java.util.Comparator;
import java.util.List;

/**
 * Provides a comparator that compares two chromosomes based on the domination relation.
 */
public class DominationComparator<T> implements Comparator<IChromosome<T>> {

    /**
     * The list of fitness functions or targets.
     */
    private final List<IFitnessFunction<T>> fitnessFunctions;

    /**
     * Initialises a domination comparator with the given fitness functions (targets).
     *
     * @param fitnessFunctions The list of fitness functions (targets).
     */
    public DominationComparator(List<IFitnessFunction<T>> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }

    /**
     *  Performs a comparison of two chromosomes based on the domination relation.
     *
     * @param o1 The first chromosome.
     * @param o2 The second chromosome.
     * @return Returns a value of {@code -1} if o2 dominates o1 or a value of {@code 1}
     *          if o1 dominates o2. If no chromosome dominates each other, a value of
     *          {@code 0} is returned.
     */
    @Override
    public int compare(IChromosome<T> o1, IChromosome<T> o2) {

        /*
         * Whether o1 is in at least one target better/worse than o2.
         */
        boolean isBetterInOneTarget = false;
        boolean isWorseInOneTarget = false;

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {

            boolean isMaximising = fitnessFunction.isMaximizing();

            double fitness1 = fitnessFunction.getNormalizedFitness(o1);
            double fitness2 = fitnessFunction.getNormalizedFitness(o2);
            int cmp = Double.compare(fitness1, fitness2);

            if (cmp == 0) {
                // compare on next target
                continue;
            } else {

                if (!isMaximising) {
                    // flip comparison for minimising fitness function
                    cmp = cmp * -1;
                }

                if (cmp < 0) {
                    // o2 dominates o1
                    isWorseInOneTarget = true;
                } else {
                    // o1 dominates o2
                    isBetterInOneTarget = true;
                }

                if (isBetterInOneTarget && isWorseInOneTarget) {
                    // no domination, hence same pareto front
                    return 0;
                }
            }
        }

        if (!isWorseInOneTarget && !isBetterInOneTarget) {
            // equal in every target, hence no domination
            return 0;
        } else if (isBetterInOneTarget && !isWorseInOneTarget) {
            // at least better in one target, hence o1 dominates o2
            return 1;
        } else if (isWorseInOneTarget && !isBetterInOneTarget) {
            // at least worse in one target, hence o2 dominates o1
            return -1;
        } else {
            throw new IllegalStateException("Should never happen!");
        }
    }
}
