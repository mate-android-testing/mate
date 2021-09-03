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
 * Select chromosomes proportionate to the first
 * {@link org.mate.exploration.genetic.fitness.IFitnessFunction} given with an additional random
 * factor
 * @param <T> Type wrapped by the chromosome implementation
 */
public class FitnessProportionateSelectionFunction<T> implements ISelectionFunction<T> {

    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {
        IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);
        List<Tuple<Integer, Double>> proportionateFitnessValues = new ArrayList<>();

        int count = 0;
        for (IChromosome<T> chromosome : population) {
            double proportionateFitness = fitnessFunction.getNormalizedFitness(chromosome) * Randomness
                    .getRnd().nextDouble();
            proportionateFitnessValues.add(new Tuple<>(count, proportionateFitness));
            count ++;
        }

        Collections.sort(proportionateFitnessValues, new Comparator<Tuple<Integer, Double>>() {
            @Override
            public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2) {
                return o2.getY().compareTo(o1.getY());
            }
        });

        List<IChromosome<T>> selection = new ArrayList<>();

        for (Tuple<Integer, Double> proportionateFitnessValue : proportionateFitnessValues) {
            selection.add(population.get(proportionateFitnessValue.getX()));
        }

        return selection;

    }
}
