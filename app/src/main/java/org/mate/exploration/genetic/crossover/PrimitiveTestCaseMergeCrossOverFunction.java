package org.mate.exploration.genetic.crossover;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.utils.Coverage;
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

            // TODO: check whether we can use here testcase.finish()
            if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {

                MATE.log_acc("After primitive test case merge crossover:");

                Registry.getEnvironmentManager().storeCoverageData(Properties.COVERAGE(),
                        chromosome.getValue().getId(), null);

                MATE.log_acc("Coverage of: " + chromosome.getValue().getId() + ": "
                        +Registry.getEnvironmentManager().getCoverage(Properties.COVERAGE(),
                        chromosome.getValue().getId()));

                //TODO: remove hack, when better solution implemented
                if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
                    LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
                }

                if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {
                    BranchDistanceFitnessFunctionMultiObjective.retrieveFitnessValues(chromosome);
                }
            }

            MATE.log_acc("Found crash: " + chromosome.getValue().getCrashDetected());
            return chromosome;
        }

        return new Chromosome<>(offspring);
    }
}
