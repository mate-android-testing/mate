package org.mate.crash_reproduction.eda.representation;

import org.mate.model.TestCase;

import java.util.Iterator;

public interface IModelRepresentation {
    ModelRepresentationIterator getIterator();
    void resetProbabilities();
    default Iterator<NodeWithPickedAction> getTestcaseIterator(TestCase testCase) {
        return new TestCaseModelIterator(getIterator(), testCase);
    }
}
