package org.mate.exploration.genetic.crossover;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.utils.coverage.CoverageUtils;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;

import java.util.List;

public class PrimitiveTestCaseMergeCrossOverFunction implements ICrossOverFunction<TestCase> {
    public static final String CROSSOVER_FUNCTION_ID = "primitive_test_case_merge_crossover_function";
    private boolean executeActions;

    public PrimitiveTestCaseMergeCrossOverFunction() {
        executeActions = false;
    }

    public void setExecuteActions(boolean executeActions) {
        this.executeActions = executeActions;
    }


    @Override
    public IChromosome<TestCase> cross(List<IChromosome<TestCase>> parents) {
        TestCase parent0 = parents.get(0).getValue();
        TestCase parent1 = parents.get(1).getValue();
        TestCase offspring = TestCase.newDummy();
        int l0 = parent0.getEventSequence().size() / 2;
        int l1 = parent1.getEventSequence().size() / 2;
        int start0 = Randomness.getRnd().nextInt(l0 + 1);
        int start1 = Randomness.getRnd().nextInt(l1 + 1);
        offspring.getEventSequence().addAll(parent0.getEventSequence().subList(start0, start0 + l0));
        offspring.getEventSequence().addAll(parent1.getEventSequence().subList(start1, start1 + l1));

        if (executeActions) {
            TestCase executedTestCase = TestCase.fromDummy(offspring);
            Chromosome<TestCase> chromosome = new Chromosome<>(executedTestCase);

            FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
            CoverageUtils.logChromosomeCoverage(chromosome);

            return chromosome;
        }

        return new Chromosome<>(offspring);
    }
}
