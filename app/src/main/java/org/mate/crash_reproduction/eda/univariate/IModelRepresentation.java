package org.mate.crash_reproduction.eda.univariate;

public interface IModelRepresentation {
    ModelRepresentationIterator getIterator();
    void resetProbabilities();
}
