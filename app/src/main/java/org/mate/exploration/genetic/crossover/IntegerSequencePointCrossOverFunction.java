package org.mate.exploration.genetic.crossover;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

/**
 * A cross over function that crosses two integer sequences at a random point
 */
public class IntegerSequencePointCrossOverFunction implements ICrossOverFunction<List<Integer>> {
    @Override
    public IChromosome<List<Integer>> cross(List<IChromosome<List<Integer>>> parents) {
        List<Integer> sequence1 = parents.get(0).getValue();
        List<Integer> sequence2 = parents.get(1).getValue();

        double point = Randomness.getRnd().nextDouble();
        int point1 = (int) (point * (sequence1.size() + 1));
        int point2 = (int) (point * (sequence2.size() + 1));
        List<Integer> resultSequence = new ArrayList<>(sequence1.subList(0, point1));
        resultSequence.addAll(sequence2.subList(point2, sequence2.size()));
        return new Chromosome<>(resultSequence);
    }
}
