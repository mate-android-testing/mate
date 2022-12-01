package org.mate.exploration.genetic.crossover;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Collections;
import java.util.List;

/**
 * Provides a one-point crossover for two {@link TestCase}s. Note that the test cases are not
 * executed directly and this kind of crossover is only reasonable when we use
 * {@link org.mate.interaction.action.ui.PrimitiveAction}s.
 */
public class PrimitiveOnePointCrossOverFunction implements ICrossOverFunction<TestCase> {

    /**
     * Whether to directly execute the actions during crossover or not.
     */
    private boolean executeActions;

    /**
     * Initialises a new crossover function that is used for test cases produced by the primitive
     * chromosome factory.
     */
    public PrimitiveOnePointCrossOverFunction() {
        executeActions = true;
    }

    /**
     * Sets whether the actions should be directly executed or not.
     *
     * @param executeActions Whether the actions should be directly executed.
     */
    public void setExecuteActions(boolean executeActions) {
        this.executeActions = executeActions;
    }

    /**
     * Performs an one-point crossover applied to the given two test cases.
     *
     * @param parents The parents consisting of two test cases.
     * @return Returns the generated offspring.
     */
    @Override
    public List<IChromosome<TestCase>> cross(List<IChromosome<TestCase>> parents) {

        if (parents.size() == 1) {
            MATE.log_warn("OnePointCrossOverFunction not applicable on a single testcase!");
            return Collections.singletonList(parents.get(0));
        }

        MATE.log_acc("Undergo crossover...");

        TestCase t1 = parents.get(0).getValue();
        TestCase t2 = parents.get(1).getValue();

        TestCase offspring = TestCase.newDummy();

        int lengthT1 = t1.getActionSequence().size();
        int lengthT2 = t2.getActionSequence().size();
        int min = Math.min(lengthT1, lengthT2);
        int cutPoint = Randomness.getRnd().nextInt(min);

        offspring.getActionSequence().addAll(t1.getActionSequence().subList(0, cutPoint));
        offspring.getActionSequence().addAll(t2.getActionSequence().subList(cutPoint, lengthT2));

        if (executeActions) {
            TestCase executedTestCase = TestCase.fromDummy(offspring);
            Chromosome<TestCase> chromosome = new Chromosome<>(executedTestCase);

            if (Properties.SURROGATE_MODEL()) {
                // update sequences + write traces to external storage
                SurrogateModel surrogateModel
                        = (SurrogateModel) Registry.getUiAbstractionLayer().getGuiModel();
                surrogateModel.updateTestCase(executedTestCase);
            }

            FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
            CoverageUtils.logChromosomeCoverage(chromosome);

            executedTestCase.finish();

            return Collections.singletonList(chromosome);
        }

        return Collections.singletonList(new Chromosome<>(offspring));
    }
}
