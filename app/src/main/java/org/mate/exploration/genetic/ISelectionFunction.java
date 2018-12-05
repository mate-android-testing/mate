package org.mate.exploration.genetic;

import java.util.List;

public interface ISelectionFunction<T> {
    List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions);
}
