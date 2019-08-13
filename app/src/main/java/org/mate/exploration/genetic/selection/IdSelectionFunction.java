package org.mate.exploration.genetic.selection;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Don't perform any selection, just return the population
 * @param <T> Type wrapped by the chromosome implementation
 */
public class IdSelectionFunction<T> implements ISelectionFunction<T> {
    public static final String SELECTION_FUNCTION_ID = "id_selection_function";

    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> iFitnessFunctions) {
        return new ArrayList<>(population);
    }
}
