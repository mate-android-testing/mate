package org.mate.exploration.genetic.mutation;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;
import org.mate.ui.WidgetAction;
import org.mate.utils.CoverageUtils;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class CutPointMutationFunction implements IMutationFunction<TestCase> {
    public static final String MUTATION_FUNCTION_ID = "cut_point_mutation_function";

    private UIAbstractionLayer uiAbstractionLayer;
    private int maxNumEvents;

    private boolean isTestSuiteExecution = false;

    public CutPointMutationFunction(int maxNumEvents) {
        this.uiAbstractionLayer = MATE.uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;
    }

    // TODO: might be replaceable with chromosome factory property in the future
    public void setTestSuiteExecution(boolean testSuiteExecution) {
        this.isTestSuiteExecution = testSuiteExecution;
    }

    @Override
    public List<IChromosome<TestCase>> mutate(IChromosome<TestCase> chromosome) {
        uiAbstractionLayer.resetApp();

        List<IChromosome<TestCase>> mutations = new ArrayList<>();

        int cutPoint = chooseCutPoint(chromosome.getValue());

        TestCase mutant = TestCase.newInitializedTestCase();
        IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutant);

        mutations.add(mutatedChromosome);

        try {
            for (int i = 0; i < maxNumEvents; i++) {
                WidgetAction newAction;
                if (i < cutPoint) {
                    //Todo: highlight that this class can only be used for widget based execution
                    newAction = (WidgetAction) chromosome.getValue().getEventSequence().get(i);
                } else {
                    newAction = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
                }
                if (!uiAbstractionLayer.getExecutableActions().contains(newAction) || !mutant.updateTestCase(newAction, i)) {
                    break;
                }
            }
        } finally {
            mutant.finish();
        }

        if (!isTestSuiteExecution) {
            /*
             * If we deal with a test suite execution, the storing of coverage
             * and fitness data is handled by the AndroidSuiteRandomChromosomeFactory itself.
             */
            FitnessUtils.storeTestCaseChromosomeFitness(mutatedChromosome);
            CoverageUtils.storeTestCaseChromosomeCoverage(mutatedChromosome);
            CoverageUtils.logChromosomeCoverage(mutatedChromosome);
        }

        return mutations;
    }

    private int chooseCutPoint(TestCase testCase) {
        return Randomness.getRnd().nextInt(testCase.getEventSequence().size());
    }
}
