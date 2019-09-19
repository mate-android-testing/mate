package org.mate.exploration.genetic.crossover;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Randomness;

import java.util.List;

public class PrimitiveTestCaseMergeCrossOverFunction implements ICrossOverFunction<TestCase> {
    public static final String CROSSOVER_FUNCTION_ID = "primitive_test_case_merge_crossover_function";
    private boolean storeCoverage;
    private boolean executeActions;

    public PrimitiveTestCaseMergeCrossOverFunction() {
        this(Properties.STORE_COVERAGE);
    }

    public PrimitiveTestCaseMergeCrossOverFunction(boolean storeCoverage) {
        this.storeCoverage = storeCoverage;
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

            if (storeCoverage) {
                EnvironmentManager.storeCoverageData(chromosome, null);

                MATE.log_acc("After primitive test case merge crossover:");
                MATE.log_acc("Coverage of: " + chromosome.toString() + ": " + EnvironmentManager
                        .getCoverage(chromosome));
                MATE.log_acc("Found crash: " + String.valueOf(chromosome.getValue().getCrashDetected()));

                //TODO: remove hack, when better solution implemented
                LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
            }

            return chromosome;
        }

        return new Chromosome<>(offspring);
    }
}
