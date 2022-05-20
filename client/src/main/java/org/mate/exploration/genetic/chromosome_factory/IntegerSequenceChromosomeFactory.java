package org.mate.exploration.genetic.chromosome_factory;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates integer sequences as chromosomes. This factory is intended to be used in combination
 * with a grammatical evolution strategy.
 */
public class IntegerSequenceChromosomeFactory implements IChromosomeFactory<List<Integer>> {

    /**
     * The sequence length.
     */
    private final int sequenceLength;

    /**
     * Creates a factory that will generate random integer sequences.
     *
     * @param sequenceLength The length of the generated integer sequences.
     */
    public IntegerSequenceChromosomeFactory(int sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

    /**
     * Creates a chromosome consisting of a random integer sequence.
     *
     * @return Returns the generated chromosome.
     */
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
