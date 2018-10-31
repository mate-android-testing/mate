package org.mate.exploration.genetic;

import org.mate.model.TestCase;

public class ActionChromosome extends Chromosome<TestCase> {
    public ActionChromosome(TestCase value) {
        super(value);
    }

    @Override
    public double getFitness() {
        return getValue().getVisitedStates().size();
    }

    @Override
    public String getId() {
        return getValue().getId();
    }
}
