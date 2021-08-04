package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An integer sequence mutation function that changes a randomly selected entry in the sequences
 * to a new random integer
 */
public class IntegerSequencePointMutationFunction implements IMutationFunction<List<Integer>> {
    @Override
    public List<IChromosome<List<Integer>>> mutate(IChromosome<List<Integer>> chromosome) {
        List<Integer> resultSequence = new ArrayList<>(chromosome.getValue());
        int index = Randomness.randomIndex(resultSequence);
        resultSequence.set(index, Randomness.getRnd().nextInt());

        List<IChromosome<List<Integer>>> chromosomeList = new ArrayList<>();
        chromosomeList.add(new Chromosome<>(resultSequence));
        return chromosomeList;
    }
}
