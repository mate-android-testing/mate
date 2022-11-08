package org.mate.exploration.genetic.crossover;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.Randomness;

import java.util.Collections;
import java.util.List;

/**
 * Provides the second cross over function called one point cross over from the Sapienz algorithm.
 */
public class OnePointCrossOverFunction<T> implements ICrossOverFunction<T>{

    /**
     * Performs an (in-place) one-point crossover with the given two test cases.
     *
     * @param parents Containing two test cases.
     * @return the offsprings.
     */
    @Override
    public List<IChromosome<T>> cross(List<IChromosome<T>> parents) {

        T object = parents.get(0).getValue();
        TestCase t1;
        TestCase t2;

        if (object instanceof TestCase) {
            if (parents.size() == 1) {
                MATE.log_warn("OnePointCrossOverFunction not applicable on a single testcase!");
                return Collections.singletonList(parents.get(0));
            }

            t1 = (TestCase) object;
            t2 = (TestCase) parents.get(1).getValue();
        } else if (object instanceof TestSuite) {
            TestSuite suite = (TestSuite) object;
            List<TestCase> cases = suite.getTestCases();

            if (cases.size() == 1) {
                MATE.log_warn("OnePointCrossOverFunction not applicable on a single testcase!");
                return Collections.singletonList(parents.get(0));
            }

            t1 = cases.get(0);
            t2 = cases.get(1);
        } else {
            throw new IllegalStateException("Error! One point cross over function hasn't "
                    + "implemented the class " + object.getClass() + " yet!");
        }

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
