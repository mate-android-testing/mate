package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutation function that add and/or removes entries from an integer sequences
 */
public class IntegerSequenceLengthMutationFunction implements IMutationFunction<List<Integer>> {
    private final int GE_MUTATION_COUNT;

    /**
     * Creates a mutation function that will add and/or removes the given amount of entries from
     * the integer sequences.
     * @param geMutationCount how the amount of entries that should be added and/or removed
     */
    public IntegerSequenceLengthMutationFunction(int geMutationCount) {
        GE_MUTATION_COUNT = geMutationCount;
    }

    @Override
    public List<IChromosome<List<Integer>>> mutate(IChromosome<List<Integer>> chromosome) {
        List<Integer> resultSequence = new ArrayList<>(chromosome.getValue());

        for (int i = 0; i < GE_MUTATION_COUNT; i++) {
            boolean doDrop = Randomness.getRnd().nextBoolean();

            if (resultSequence.size() == 0) {
                doDrop = false;
            }

            if (doDrop) {
                int index = Randomness.randomIndex(resultSequence);
                //index must not be of Integer Object type to avoid calling wrong overloaded function
                resultSequence.remove(index);
            } else {
                int index = Randomness.getRnd().nextInt(resultSequence.size() + 1);
                resultSequence.add(index, Randomness.getRnd().nextInt());
            }
        }

        List<IChromosome<List<Integer>>> chromosomeList = new ArrayList<>();
        chromosomeList.add(new Chromosome<>(resultSequence));
        return chromosomeList;
    }
}
