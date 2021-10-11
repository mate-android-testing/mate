package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

/**
 * Provides a fitness metric based on the novelty/diversity of a chromosome. This requires that the
 * AUT has been instrumented with the method coverage module.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class NoveltyFitnessFunction<T> implements IFitnessFunction<T> {

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        return FitnessUtils.getFitness(chromosome);
    }
}
