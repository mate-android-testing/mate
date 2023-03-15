package org.mate.crash_reproduction.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;

import java.util.Collections;
import java.util.Set;

public interface IMultipleFitnessFunctions<T> extends IFitnessFunction<T> {

    Set<IFitnessFunction<T>> getInnerFitnessFunction();

    static <T> IMultipleFitnessFunctions<T> wrapIfNecessary(IFitnessFunction<T> fitnessFunction) {

        if (fitnessFunction instanceof IMultipleFitnessFunctions) {
            return (IMultipleFitnessFunctions<T>) fitnessFunction;
        } else {
            return new IMultipleFitnessFunctions<T>() {
                @Override
                public Set<IFitnessFunction<T>> getInnerFitnessFunction() {
                    return Collections.singleton(fitnessFunction);
                }

                @Override
                public double getFitness(IChromosome<T> chromosome) {
                    return fitnessFunction.getFitness(chromosome);
                }

                @Override
                public boolean isMaximizing() {
                    return fitnessFunction.isMaximizing();
                }

                @Override
                public double getNormalizedFitness(IChromosome<T> chromosome) {
                    return fitnessFunction.getNormalizedFitness(chromosome);
                }
            };
        }
    }
}
