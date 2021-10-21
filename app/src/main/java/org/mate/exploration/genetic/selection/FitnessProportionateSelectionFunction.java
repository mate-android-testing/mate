package org.mate.exploration.genetic.selection;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a roulette wheel selection based on the fitness values for single objective search.
 * The selection returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
 *
 * @param <T> Refers to the type of the chromosomes.
 */
public class FitnessProportionateSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Performs a roulette wheel selection proportionate to the fitness values. This process is
     * repeated until a selection of {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes is
     * formed.
     *
     * @param population The current population.
     * @param fitnessFunctions The list of fitness functions. Only the first one is used here.
     * @return Returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {

        final IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);
        final boolean maximizing = fitnessFunction.isMaximizing();

        List<IChromosome<T>> selection = new ArrayList<>();
        List<IChromosome<T>> candidates = new LinkedList<>(population);

        for (int i = 0; i < Properties.DEFAULT_SELECTION_SIZE(); i++) {

            /*
            * Constructs the roulette wheel. Each chromosome is assigned a range proportionate
            * to its fitness value. The first chromosome c1 covers the range [0.0,fitness(c1)],
            * the second chromosome c2 the range (fitness(c1),fitness(c2)], and so on.
             */
            double sum = 0.0;

            for (IChromosome<T> chromosome : candidates) {
                double fitness = fitnessFunction.getNormalizedFitness(chromosome);
                sum += maximizing ? fitness : invertFitnessValue(fitness);
            }

            /*
            * The maximal spectrum of the roulette wheel is defined by the range [0.0,sum].
            * Thus, we pick a random number in that spectrum. The candidate that covers the
            * random number represents the selected chromosome.
             */
            double rnd = Randomness.getRandom(0.0, sum);
            IChromosome<T> selected = null;

            double start = 0.0;
            for (IChromosome<T> chromosome : candidates) {
                double fitness = fitnessFunction.getNormalizedFitness(chromosome);
                double end = start + (maximizing ? fitness : invertFitnessValue(fitness));
                if (rnd <= end) {
                    selected = chromosome;
                    break;
                } else {
                    start = end;
                }
            }

            selection.add(selected);

            // remove selected chromosome from roulette wheel
            candidates.remove(selected);
        }

        return selection;
    }

    /**
     * This method inverts a fitness value - used in case of a minimizing fitness function.
     *
     * @param fitnessValue The fitness value to be inverted.
     * @return Returns the inverted fitness value.
     */
    private double invertFitnessValue(final double fitnessValue) {
        return 1.0 - fitnessValue;
    }
}
