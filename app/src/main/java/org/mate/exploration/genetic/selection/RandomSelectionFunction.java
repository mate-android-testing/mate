package org.mate.exploration.genetic.selection;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

/**
 * Randomly select chromosomes from the population
 * @param <T> Type wrapped by the chromosome implementation
 */
public class RandomSelectionFunction<T> implements ISelectionFunction<T> {

    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {
        List<IChromosome<T>> selection = new ArrayList<>(population);
        Randomness.shuffleList(selection);
        return selection;
    }
}
