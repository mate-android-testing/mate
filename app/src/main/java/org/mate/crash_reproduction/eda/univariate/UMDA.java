package org.mate.crash_reproduction.eda.univariate;

import org.mate.crash_reproduction.eda.representation.IModelRepresentation;
import org.mate.crash_reproduction.eda.representation.TestCaseModelIterator;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.model.TestCase;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UMDA extends RepresentationBasedModel {
    private final List<IFitnessFunction<TestCase>> fitnessFunctions;
    private final ISelectionFunction<TestCase> selectionFunction;

    public UMDA(IModelRepresentation modelRepresentation, List<IFitnessFunction<TestCase>> fitnessFunctions, ISelectionFunction<TestCase> selectionFunction) {
        super(modelRepresentation);
        this.fitnessFunctions = fitnessFunctions;
        this.selectionFunction = selectionFunction;
    }

    @Override
    public void update(Collection<IChromosome<TestCase>> population) {
        modelRepresentation.resetProbabilities();
        List<IChromosome<TestCase>> selection = selectionFunction.select(new LinkedList<>(population), fitnessFunctions);

        for (IChromosome<TestCase> testCase : selection) {
            TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(modelRepresentation.getIterator(), testCase.getValue());

            while (testCaseModelIterator.hasNext()) {
                TestCaseModelIterator.NodeWithPickedAction node = testCaseModelIterator.next();

                node.putProbabilityOfAction(node.getProbabilityOfAction() + 1);
            }
        }
    }
}
