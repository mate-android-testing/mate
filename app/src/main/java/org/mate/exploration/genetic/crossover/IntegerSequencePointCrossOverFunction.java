package org.mate.exploration.genetic.crossover;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class IntegerSequencePointCrossOverFunction implements ICrossOverFunction<List<Integer>> {
    @Override
    public IChromosome<List<Integer>> cross(List<IChromosome<List<Integer>>> parents) {
        List<Integer> sequence1 = parents.get(0).getValue();
        List<Integer> sequence2 = parents.get(1).getValue();

        if (sequence1.size() != sequence2.size()) {
            throw new IllegalArgumentException("only allowed for same size sequence length");
        }

        int point = Randomness.getRnd().nextInt(sequence1.size() + 1);
        if (point == 0) {
            return new Chromosome<List<Integer>>(new ArrayList<>(sequence2));
        } else if (point == sequence1.size()) {
            return new Chromosome<List<Integer>>(new ArrayList<>(sequence1));
        } else {
            List<Integer> resultSequence = new ArrayList<>(sequence1.subList(0, point));
            resultSequence.addAll(new ArrayList<>(sequence2.subList(point, sequence2.size())));
            return new Chromosome<>(resultSequence);
        }
    }
}
