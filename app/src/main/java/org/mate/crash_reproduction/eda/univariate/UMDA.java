package org.mate.crash_reproduction.eda.univariate;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UMDA extends VectorBasedDistributionModel<Double> {
    private final List<IFitnessFunction<TestCase>> fitnessFunctions;
    private final ISelectionFunction<TestCase> selectionFunction;

    public UMDA(List<IFitnessFunction<TestCase>> fitnessFunctions, ISelectionFunction<TestCase> selectionFunction) {
        super((state, action) -> Registry.getUiAbstractionLayer().getPromisingActions(state).contains(action) ? 2D : 1D, 0.1);
        this.fitnessFunctions = fitnessFunctions;
        this.selectionFunction = selectionFunction;
    }

    @Override
    public void update(Collection<IChromosome<TestCase>> population) {
        stateActionTree.clearWeights();
        List<IChromosome<TestCase>> selection = selectionFunction.select(new LinkedList<>(population), fitnessFunctions);

        for (int i = 0; i < Properties.MAX_NUMBER_EVENTS(); i++) {
            for (IChromosome<TestCase> testCase : selection) {
                Action actionAtIndex = testCase.getValue().getEventSequence().get(i);
                stateActionTree.updateWeightOfAction(i, actionAtIndex, weight -> weight + 1);
            }
        }
    }
}
