package org.mate.exploration.genetic.crossover;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.utils.Randomness;

import java.util.Collections;
import java.util.List;

/**
 * Provides the second cross over function called one point cross over from the Sapienz algorithm.
 */
public class OnePointCrossOverFunction implements ICrossOverFunction<TestCase>{

    /**
     * Performs an (in-place) one-point crossover with the given two test cases.
     *
     * @param parents Containing two test cases.
     * @return the offsprings.
     */
    @Override
    public List<IChromosome<TestCase>> cross(List<IChromosome<TestCase>> parents) {
        if (parents.size() == 1) {
            MATE.log_warn("OnePointCrossOverFunction not applicable on a single testcase!");
            return Collections.singletonList(parents.get(0));
        }

        TestCase t1 = parents.get(0).getValue();
        TestCase t2 = parents.get(1).getValue();

        TestCase copyT1 = TestCase.newDummy();
        copyT1.getEventSequence().addAll(t1.getEventSequence());
        TestCase copyT2 = TestCase.newDummy();
        copyT2.getEventSequence().addAll(t2.getEventSequence());

        int lengthT1 = t1.getEventSequence().size();
        int lengthT2 = t2.getEventSequence().size();
        int min = Math.min(lengthT1, lengthT2);
        int cutPoint = Randomness.getRnd().nextInt(min);

        t1.getEventSequence().clear();
        t2.getEventSequence().clear();

        t1.getEventSequence().addAll(copyT1.getEventSequence().subList(0, cutPoint));
        t1.getEventSequence().addAll(copyT2.getEventSequence().subList(cutPoint, lengthT2));

        t2.getEventSequence().addAll(copyT2.getEventSequence().subList(0, cutPoint));
        t2.getEventSequence().addAll(copyT1.getEventSequence().subList(cutPoint, lengthT1));

        return parents;
    }
}
