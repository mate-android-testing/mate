package org.mate.crash_reproduction.eda.representation;

public interface IModelRepresentation {
    ModelRepresentationIterator getIterator();
    void resetProbabilities();
}
