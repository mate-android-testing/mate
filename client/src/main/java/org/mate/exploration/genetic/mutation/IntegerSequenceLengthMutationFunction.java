package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a mutation function that add and/or removes genes from the chromosome.
 */
public class IntegerSequenceLengthMutationFunction implements IMutationFunction<List<Integer>> {

    /**
     * The number of genes that should be added and/or removed.
     */
    private final int GE_MUTATION_COUNT;

    /**
     * Initialises the mutation function.
     *
     * @param geMutationCount The number of genes that should be added or removed.
     */
    public IntegerSequenceLengthMutationFunction(int geMutationCount) {
        GE_MUTATION_COUNT = geMutationCount;
    }

    /**
     * Performs a mutation that adds and/or removes genes from the given chromosome.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<List<Integer>> mutate(IChromosome<List<Integer>> chromosome) {

        List<Integer> resultSequence = new ArrayList<>(chromosome.getValue());

        for (int i = 0; i < GE_MUTATION_COUNT; i++) {

            // whether to add or remove genes
            boolean doDrop = Randomness.getRnd().nextBoolean();

            // can't remove genes if none present
            if (resultSequence.size() == 0) {
                doDrop = false;
            }

            if (doDrop) {
                int index = Randomness.randomIndex(resultSequence);
                // index must not be of Integer Object type to avoid calling wrong overloaded function
                resultSequence.remove(index);
            } else {
                int index = Randomness.getRnd().nextInt(resultSequence.size() + 1);
                resultSequence.add(index, Randomness.getRnd().nextInt());
            }
        }

        return new Chromosome<>(resultSequence);
    }
}
