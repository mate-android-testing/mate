package org.mate.exploration.genetic.selection;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.mate.utils.MathUtils.isEpsEq;

/**
 * Select chromosomes strictly by the first
 * {@link org.mate.exploration.genetic.fitness.IFitnessFunction} given
 *
 * @param <T> Type wrapped by the chromosome implementation
 */
public class FitnessSelectionFunction<T> implements ISelectionFunction<T> {

    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, final List<IFitnessFunction<T>> fitnessFunctions) {
        List<IChromosome<T>> list = new ArrayList<>(population);
        Collections.sort(list, new Comparator<IChromosome<T>>() {
            @Override
            public int compare(IChromosome<T> o1, IChromosome<T> o2) {
                IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);
                double compared = fitnessFunction.getNormalizedFitness(o1)
                        - fitnessFunction.getNormalizedFitness(o2);
                return fitnessFunction.isMaximizing()
                        ? compareFunctions(compared) : compareFunctions(compared) * (-1);
            }
            private int compareFunctions(double compared) {
                if (compared > 0) {
                    return 1;
                } else if (compared < 0) {
                    return -1;
                }
                return 0;
            }
        });
        return list;
    }
}
