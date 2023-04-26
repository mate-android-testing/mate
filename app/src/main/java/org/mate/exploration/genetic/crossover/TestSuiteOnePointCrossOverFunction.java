package org.mate.exploration.genetic.crossover;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Collections;
import java.util.List;

/**
 * Provides a one point crossover on {@link TestSuite}s.
 */
public class TestSuiteOnePointCrossOverFunction implements ICrossOverFunction<TestSuite> {

    /**
     * Performs a one point crossover on two test suites.
     *
     * @param parents The parents that undergo crossover.
     * @return Returns the generated offspring.
     */
    @Override
    public List<IChromosome<TestSuite>> cross(List<IChromosome<TestSuite>> parents) {

        if (parents.size() == 1) {
            MATE.log_warn("TestSuiteOnePointCrossOverFunction not applicable on single chromosome!");
            return Collections.singletonList(parents.get(0));
        }

        TestSuite t1 = parents.get(0).getValue();
        TestSuite t2 = parents.get(1).getValue();

        // select a random cut point
        int min = Math.min(t1.getTestCases().size(), t2.getTestCases().size());
        int cutPoint = Randomness.getRnd().nextInt(min + 1);

        TestSuite offspringSuite = new TestSuite();
        IChromosome<TestSuite> offspring = new Chromosome<>(offspringSuite);

        // select test cases from t1 up to the cut point, then from t2
        List<TestCase> testCasesFromT1 = t1.getTestCases().subList(0, cutPoint);
        List<TestCase> testCasesFromT2 = t2.getTestCases().subList(cutPoint, t2.getTestCases().size());
        offspringSuite.getTestCases().addAll(testCasesFromT1);
        offspringSuite.getTestCases().addAll(testCasesFromT2);

        // copy coverage and fitness data from t1 to offspring
        if (!testCasesFromT1.isEmpty()) {
            MATE.log_acc("With " + testCasesFromT1.size() + " test cases from first parent");
            CoverageUtils.copyCoverageData(parents.get(0), offspring, testCasesFromT1);
            FitnessUtils.copyFitnessData(parents.get(0), offspring, testCasesFromT1);
        }

        // copy coverage and fitness data from t2 to offspring
        if (!testCasesFromT2.isEmpty()) {
            MATE.log_acc("With " + testCasesFromT2.size() + " test cases from second parent");
            CoverageUtils.copyCoverageData(parents.get(1), offspring, testCasesFromT2);
            FitnessUtils.copyFitnessData(parents.get(1), offspring, testCasesFromT2);
        }

        return Collections.singletonList(offspring);
    }
}
