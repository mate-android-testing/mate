package org.mate.exploration.genetic;

import org.mate.model.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FitnessSelectionFunction<T> implements ISelectionFunction<T> {
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, final List<IFitnessFunction<T>> fitnessFunctions) {
        List<IChromosome<T>> list = new ArrayList<>(population);
        Collections.sort(list, new Comparator<IChromosome<T>>() {
            @Override
            public int compare(IChromosome<T> o1, IChromosome<T> o2) {
                double c = fitnessFunctions.get(0).getFitness(o1)
                           - fitnessFunctions.get(0).getFitness(o2);
                if (c > 0) {
                    return 1;
                } else if (c < 0) {
                    return -1;
                }
                return 0;
            }
        });
        return list;
    }
}
