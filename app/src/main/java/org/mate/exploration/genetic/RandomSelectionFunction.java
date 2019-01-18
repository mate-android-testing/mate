package org.mate.exploration.genetic;

import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class RandomSelectionFunction<T> implements ISelectionFunction<T> {
    public static final String SELECTION_FUNCTION_ID = "random_selection_function";

    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {
        List<IChromosome<T>> selection = new ArrayList<>(population);
        Randomness.shuffleList(selection);
        return selection;
    }
}
