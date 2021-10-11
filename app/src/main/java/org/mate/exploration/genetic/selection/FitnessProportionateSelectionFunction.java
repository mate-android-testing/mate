package org.mate.exploration.genetic.selection;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.utils.Randomness;
import org.mate.utils.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Performs a roulette wheel selection based on the fitness values. Only applicable where a single
 * fitness function is used.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class FitnessProportionateSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Performs a roulette wheel selection proportionate to the fitness values. This is an iterative
     * process. Every round, a new roulette wheel is constructed based on a list of chromosomes
     * (initially the entire population). Then, a random number determines the selection of the next
     * chromosome. After that, the chromosome is removed from the list and the next iteration starts.
     * The iteration ends when the list of chromosomes is empty.
     *
     * @param population A pool of candidates for the selection.
     * @param fitnessFunctions The fitness functions. Note, we assume that this method is called
     *                         only with a single fitness function.
     * @return Returns a list of chromosomes based on the order of the roulette wheel selection.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {

        IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);
        List<Tuple<Integer, Double>> proportionateFitnessValues = new ArrayList<>();

        int count = 0;
        final boolean maximizing = fitnessFunction.isMaximizing();

        for (IChromosome<T> chromosome : population) {
            double normalizedFitnessValue = fitnessFunction.getNormalizedFitness(chromosome);
            if (!maximizing) {
                normalizedFitnessValue = invertFitnessValue(normalizedFitnessValue);
            }
            double proportionateFitness = normalizedFitnessValue * Randomness.getRnd().nextDouble();
            proportionateFitnessValues.add(new Tuple<>(count, proportionateFitness));
            count++;
        }

        Collections.sort(proportionateFitnessValues, new Comparator<Tuple<Integer, Double>>() {
            @Override
            public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2) {
                int comparedValue = o2.getY().compareTo(o1.getY());
                return maximizing ? comparedValue : comparedValue * (-1);
            }
        });

        List<IChromosome<T>> selection = new ArrayList<>();

        for (Tuple<Integer, Double> proportionateFitnessValue : proportionateFitnessValues) {
            selection.add(population.get(proportionateFitnessValue.getX()));
        }

        return selection;

    }

    /**
     * This method inverts a fitness value - used in case of a minimizing fitness function.
     *
     * @param fitnessValue The fitness value to be inverted.
     * @return The inverted fitness value.
     */
    private double invertFitnessValue(final double fitnessValue) {
        return 1.0 / (fitnessValue + 1.0);
    }
}
