package org.mate.exploration.genetic.crossover;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.utils.Randomness;

import java.util.Collections;
import java.util.List;

/**
 * Provides a one-point crossover for two {@link TestCase}s. Note that the test cases are not
 * executed directly and this kind of crossover is only reasonable when we use
 * {@link org.mate.interaction.action.ui.PrimitiveAction}s.
 */
public class PrimitiveOnePointCrossOverFunction implements ICrossOverFunction<TestCase> {

    /**
     * Performs an one-point crossover applied to the given two test cases.
     *
     * @param parents The parents consisting of two test cases.
     * @return Returns the generated offsprings.
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
        TestCase copyT2 = TestCase.newDummy();

        int lengthT1 = t1.getActionSequence().size();
        int lengthT2 = t2.getActionSequence().size();
        int min = Math.min(lengthT1, lengthT2);
        int cutPoint = Randomness.getRnd().nextInt(min);

        copyT1.getActionSequence().addAll(t1.getActionSequence().subList(0, cutPoint));
        copyT1.getActionSequence().addAll(t2.getActionSequence().subList(cutPoint, lengthT2));

        copyT2.getActionSequence().addAll(t2.getActionSequence().subList(0, cutPoint));
        copyT2.getActionSequence().addAll(t1.getActionSequence().subList(cutPoint, lengthT1));

        return List.of(new Chromosome<>(copyT1), new Chromosome<>(copyT2));
    }
}
