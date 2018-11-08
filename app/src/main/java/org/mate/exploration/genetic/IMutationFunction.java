package org.mate.exploration.genetic;

import java.util.List;

public interface IMutationFunction<T> {
    List<IChromosome<T>> mutate(IChromosome<T> parent);
}
