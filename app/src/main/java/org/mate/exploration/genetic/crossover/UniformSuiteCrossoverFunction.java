package org.mate.exploration.genetic.crossover;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.coverage.CoverageUtils;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class UniformSuiteCrossoverFunction implements ICrossOverFunction<TestSuite> {

    @Override
    public IChromosome<TestSuite> cross(List<IChromosome<TestSuite>> parents) {

        if (parents.size() == 1) {
            MATE.log_warn("UniformSuiteCrossoverFunction not applicable on single chromosome!");
            return parents.get(0);
        }

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
        MATE.log_acc("Uniform Suite Cross Over: crossing over chromosome: " + parents.get(0) + " and chromosome: " + parents.get(1));
        MATE.log_acc("cross over result chromosome: " + offspring);

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
            MATE.log_acc("With " + copyTestCasesFromParent1.size() + " test cases from first parent");
            CoverageUtils.copyCoverageData(parents.get(0), offspring, copyTestCasesFromParent1);
            FitnessUtils.copyFitnessData(parents.get(0), offspring, copyTestCasesFromParent1);
        }

        if (!copyTestCasesFromParent2.isEmpty()) {
            MATE.log_acc("and " + copyTestCasesFromParent2.size() + " test cases from second parent");
            CoverageUtils.copyCoverageData(parents.get(1), offspring, copyTestCasesFromParent2);
            FitnessUtils.copyFitnessData(parents.get(1), offspring, copyTestCasesFromParent2);
        }

        return offspring;
    }
}
