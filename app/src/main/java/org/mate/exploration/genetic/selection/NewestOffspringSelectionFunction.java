package org.mate.exploration.genetic.selection;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewestOffspringSelectionFunction<T> implements ISelectionFunction<T> {
    public static final String SELECTION_FUNCTION_ID = "newest_offspring_selection_function";

    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> iFitnessFunctions) {
        return Arrays.asList(population.get(population.size() - 1));
    }
}
