package org.mate.exploration.genetic.chromosome_factory;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates integer sequences as chromosomes. These can be used for grammatical evolution.
 */
public class IntegerSequenceChromosomeFactory implements IChromosomeFactory<List<Integer>> {
    private final int sequenceLength;

    /**
     * Creates a factory that will generate integer sequences with the given sequence length
     * @param sequenceLength the length of the generated integer sequences
     */
    public IntegerSequenceChromosomeFactory(int sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

    @Override
    public IChromosome<List<Integer>> createChromosome() {
        Random rnd = Randomness.getRnd();
        List<Integer> sequence = new ArrayList<>(sequenceLength);
        for (int i = 0; i < sequenceLength; i++) {
            sequence.add(rnd.nextInt());
        }
        return new Chromosome<>(sequence);
    }
}
