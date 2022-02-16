package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a mutation function that alters a single gene by replacement with a random gene.
 */
public class IntegerSequencePointMutationFunction implements IMutationFunction<List<Integer>> {

    /**
     * Mutates the chromosome by replacing a random gene with a new gene.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<List<Integer>> mutate(IChromosome<List<Integer>> chromosome) {
        List<Integer> resultSequence = new ArrayList<>(chromosome.getValue());
        int index = Randomness.randomIndex(resultSequence);
        resultSequence.set(index, Randomness.getRnd().nextInt());
        return new Chromosome<>(resultSequence);
    }
}
