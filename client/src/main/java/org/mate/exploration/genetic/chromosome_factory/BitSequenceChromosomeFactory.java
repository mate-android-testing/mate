package org.mate.exploration.genetic.chromosome_factory;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Provides a chromosome factory that generates chromosomes consisting of random booleans.
 * This factory is intended to be used in combination with a grammatical evolution strategy.
 */
public class BitSequenceChromosomeFactory implements IChromosomeFactory<List<Boolean>> {
    private final int sequenceLength;

    /**
     * Initialises a new chromosome factory.
     *
     * @param sequenceLength The length of the boolean sequence.
     */
    public BitSequenceChromosomeFactory(int sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

    /**
     * Creates a new chromosome that consists of a random sequence of booleans.
     *
     * @return Returns the generated chromosome.
     */
    @Override
    public IChromosome<List<Boolean>> createChromosome() {
        Random rnd = Randomness.getRnd();
        List<Boolean> sequence = new ArrayList<>(sequenceLength);
        for (int i = 0; i < sequenceLength; i++) {
            sequence.add(rnd.nextBoolean());
        }
        return new Chromosome<>(sequence);
    }
}
