package org.mate.exploration.genetic.mutation;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.WidgetAction;
import org.mate.utils.Coverage;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class CutPointMutationFunction implements IMutationFunction<TestCase> {
    public static final String MUTATION_FUNCTION_ID = "cut_point_mutation_function";

    private UIAbstractionLayer uiAbstractionLayer;
    private int maxNumEvents;

    public CutPointMutationFunction(int maxNumEvents) {
        this.uiAbstractionLayer = MATE.uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;
    }
    @Override
    public List<IChromosome<TestCase>> mutate(IChromosome<TestCase> chromosome) {
        uiAbstractionLayer.resetApp();

        List<IChromosome<TestCase>> mutations = new ArrayList<>();

        int cutPoint = chooseCutPoint(chromosome.getValue());

        TestCase mutant = TestCase.newInitializedTestCase();
        IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutant);

        mutations.add(mutatedChromosome);

        for (int i = 0; i < maxNumEvents; i++) {
            WidgetAction newAction;
            if (i < cutPoint) {
                //Todo: highlight that this class can only be used for widget based execution
                newAction = (WidgetAction) chromosome.getValue().getEventSequence().get(i);
            } else {
                newAction = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
            }
            if (!uiAbstractionLayer.getExecutableActions().contains(newAction) || !mutant.updateTestCase(newAction, String.valueOf(i))) {
                break;
            }
        }

        // TODO: check whether we can use here testcase.finish()
        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {

            MATE.log_acc("Coverage of: " + mutatedChromosome + ": " +
                    Registry.getEnvironmentManager().storeCoverage(Properties.COVERAGE(),
                            mutatedChromosome.getValue().getId(), null));

            MATE.log_acc("Found crash: " + mutatedChromosome.getValue().getCrashDetected());

            if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
                LineCoveredPercentageFitnessFunction.retrieveFitnessValues(mutatedChromosome);
            }

            if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {
                BranchDistanceFitnessFunctionMultiObjective.retrieveFitnessValues(chromosome);
            }
        }
        return mutations;
    }

    private int chooseCutPoint(TestCase testCase) {
        return Randomness.getRnd().nextInt(testCase.getEventSequence().size());
    }
}
