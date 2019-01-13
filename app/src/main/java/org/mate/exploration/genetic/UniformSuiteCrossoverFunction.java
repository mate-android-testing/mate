package org.mate.exploration.genetic;

import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class UniformSuiteCrossoverFunction implements ICrossOverFunction<TestSuite> {
    public static final String CROSSOVER_FUNCTION_ID = "uniform_suite_crossover_function";
    @Override
    public IChromosome<TestSuite> cross(List<IChromosome<TestSuite>> parents) {
        TestSuite t1 = parents.get(0).getValue();
        TestSuite t2 = parents.get(1).getValue();

        //Randomly select whether final length should be floored or ceiled
        int lengthBias = Randomness.getRnd().nextInt(2);
        int finalLength = (t1.getTestCases().size() + t2.getTestCases().size() + lengthBias) / 2;
        List<TestCase> testCasePool = new ArrayList<>();
        testCasePool.addAll(t1.getTestCases());
        testCasePool.addAll(t2.getTestCases());

        TestSuite offspringSuite = new TestSuite();
        IChromosome<TestSuite> offspring = new Chromosome<>(offspringSuite);

        List<TestCase> copyTestCasesFromParent1 = new ArrayList<>();
        List<TestCase> copyTestCasesFromParent2 = new ArrayList<>();

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

        if (!copyTestCasesFromParent1.isEmpty()) {
            EnvironmentManager.copyCoverageData(parents.get(0), offspring, copyTestCasesFromParent1);
        }

        if (!copyTestCasesFromParent2.isEmpty()) {
            EnvironmentManager.copyCoverageData(parents.get(1), offspring, copyTestCasesFromParent2);
        }

        return offspring;
    }
}
