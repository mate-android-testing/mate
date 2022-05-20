package org.mate.exploration.genetic.crossover;

import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A crossover function that crosses two integer sequences at a random point. This crossover function
 * should be used in combination with a grammatical evolution strategy.
 */
public class IntegerSequencePointCrossOverFunction implements ICrossOverFunction<List<Integer>> {

    /**
     * Performs a one point crossover on the given parents.
     *
     * @param parents The parents that undergo crossover.
     * @return Returns the generated offsprings.
     */
    @Override
    public List<IChromosome<List<Integer>>> cross(List<IChromosome<List<Integer>>> parents) {

        if (parents.size() == 1) {
            MATELog.log_warn("IntegerSequencePointCrossOverFunction not applicable on single chromosome!");
            return Collections.singletonList(parents.get(0));
        }

        List<Integer> sequence1 = parents.get(0).getValue();
        List<Integer> sequence2 = parents.get(1).getValue();

        double point = Randomness.getRnd().nextDouble();
        int point1 = (int) (point * (sequence1.size() + 1));
        int point2 = (int) (point * (sequence2.size() + 1));
        List<Integer> resultSequence = new ArrayList<>(sequence1.subList(0, point1));
        resultSequence.addAll(sequence2.subList(point2, sequence2.size()));
        return Collections.singletonList(new Chromosome<>(resultSequence));
    }
}
