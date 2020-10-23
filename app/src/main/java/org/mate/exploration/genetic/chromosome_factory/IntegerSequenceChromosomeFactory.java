package org.mate.exploration.genetic.chromosome_factory;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IntegerSequenceChromosomeFactory implements IChromosomeFactory<List<Integer>> {
    private final int sequenceLength;

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
