package org.mate.crash_reproduction.eda.univariate;

import org.mate.crash_reproduction.eda.representation.IModelRepresentation;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

import java.util.Collection;

public class NoUpdate extends RepresentationBasedModel {
    public NoUpdate(IModelRepresentation modelRepresentation) {
        super(modelRepresentation);
    }

    @Override
    public void update(Collection<IChromosome<TestCase>> population) {

    }
}
