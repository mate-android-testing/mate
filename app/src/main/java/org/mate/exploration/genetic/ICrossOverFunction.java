package org.mate.exploration.genetic;

import java.util.List;

public interface ICrossOverFunction<T> {
    IChromosome<T> cross(List<IChromosome<T>> parents);
}
