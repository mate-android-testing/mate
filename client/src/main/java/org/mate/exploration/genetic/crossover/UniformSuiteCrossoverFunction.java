package org.mate.exploration.genetic.crossover;

import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;
import org.mate.commons.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a uniform crossover operation for {@link TestSuite}s, i.e. the offspring consists
 * of a random combination of the genes from the parents t1 and t2.
 */
public class UniformSuiteCrossoverFunction implements ICrossOverFunction<TestSuite> {

    /**
     * Performs a uniform crossover on the two given test suites. The offspring test suite
     * consists of a random combination of the test cases from the parent test suites t1 and t2.
     * Note that the offspring is not actually executed, but only the relevant coverage and fitness
     * data are copied from the parents to the offspring.
     *
     * @param parents The two test suites on which crossover should be performed.
     * @return Returns the offsprings.
     */
    @Override
    public List<IChromosome<TestSuite>> cross(List<IChromosome<TestSuite>> parents) {

        if (parents.size() == 1) {
            MATELog.log_warn("UniformSuiteCrossoverFunction not applicable on single chromosome!");
            return Collections.singletonList(parents.get(0));
        }

        TestSuite t1 = parents.get(0).getValue();
        TestSuite t2 = parents.get(1).getValue();

        // randomly select whether final length should be floored or ceiled
        int lengthBias = Randomness.getRnd().nextInt(2);
        int finalLength = (t1.getTestCases().size() + t2.getTestCases().size() + lengthBias) / 2;
        List<TestCase> testCasePool = new ArrayList<>();
        testCasePool.addAll(t1.getTestCases());
        testCasePool.addAll(t2.getTestCases());

        TestSuite offspringSuite = new TestSuite();
        IChromosome<TestSuite> offspring = new Chromosome<>(offspringSuite);
        MATELog.log_acc("Uniform Suite Cross Over: crossing over chromosome: "
                + parents.get(0) + " and chromosome: " + parents.get(1));
        MATELog.log_acc("Cross over result chromosome: " + offspring);

        List<TestCase> copyTestCasesFromParent1 = new ArrayList<>();
        List<TestCase> copyTestCasesFromParent2 = new ArrayList<>();

        // randomly select a test case from test suite t1 or t2
        for (int i = 0; i < finalLength; i++) {
            int choice = Randomness.randomIndex(testCasePool);
            TestCase tcChoice = testCasePool.get(choice);
            if (parents.get(0).getValue().getTestCases().contains(tcChoice)) {
                copyTestCasesFromParent1.add(tcChoice);
            } else {
                copyTestCasesFromParent2.add(tcChoice);
            }
            offspringSuite.getTestCases().add(tcChoice);
            testCasePool.remove(choice);
        }

        // copy coverage and fitness data from first parent to offspring
        if (!copyTestCasesFromParent1.isEmpty()) {
            MATELog.log_acc("With " + copyTestCasesFromParent1.size() + " test cases from first parent");
            CoverageUtils.copyCoverageData(parents.get(0), offspring, copyTestCasesFromParent1);
            FitnessUtils.copyFitnessData(parents.get(0), offspring, copyTestCasesFromParent1);
        }

        // copy coverage and fitness data from second parent to offspring
        if (!copyTestCasesFromParent2.isEmpty()) {
            MATELog.log_acc("and " + copyTestCasesFromParent2.size() + " test cases from second parent");
            CoverageUtils.copyCoverageData(parents.get(1), offspring, copyTestCasesFromParent2);
            FitnessUtils.copyFitnessData(parents.get(1), offspring, copyTestCasesFromParent2);
        }

        return Collections.singletonList(offspring);
    }
}
