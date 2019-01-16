package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CutPointMutationFunction implements IMutationFunction<TestCase> {
    public static final String MUTATION_FUNCTION_ID = "cut_point_mutation_function";
    private final boolean storeCoverage;

    private UIAbstractionLayer uiAbstractionLayer;
    private int maxNumEvents;

    public CutPointMutationFunction(int maxNumEvents) {
        this(true, maxNumEvents);
    }

    public CutPointMutationFunction(boolean storeCoverage, int maxNumEvents) {
        this.storeCoverage = storeCoverage;
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
            Action newAction;
            if (i < cutPoint) {
                newAction = chromosome.getValue().getEventSequence().get(i);
            } else {
                newAction = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
            }
            if (!uiAbstractionLayer.getExecutableActions().contains(newAction) || !mutant.updateTestCase(newAction, String.valueOf(i))) {
                break;
            }
        }

        if (storeCoverage) {
            EnvironmentManager.storeCoverageData(mutatedChromosome, null);

            MATE.log_acc("Coverage of: " + mutatedChromosome + ": " + EnvironmentManager
                    .getCoverage(mutatedChromosome));
            MATE.log_acc("Found crash: " + String.valueOf(mutatedChromosome.getValue().getCrashDetected()));

            //TODO: remove hack, when better solution implemented
            LineCoveredPercentageFitnessFunction.retrieveFitnessValues(mutatedChromosome);
        }
        return mutations;
    }

    private int chooseCutPoint(TestCase testCase) {
        return Randomness.getRnd().nextInt(testCase.getEventSequence().size());
    }
}
