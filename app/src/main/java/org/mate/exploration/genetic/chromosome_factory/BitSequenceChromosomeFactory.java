package org.mate.exploration.genetic.chromosome_factory;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BitSequenceChromosomeFactory implements IChromosomeFactory<List<Boolean>> {
    private final int sequenceLength;

    public BitSequenceChromosomeFactory(int sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

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
