package org.mate.crash_reproduction.eda;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IDistributionModel extends IChromosomeFactory<TestCase> {
    void update(Collection<IChromosome<TestCase>> population);
}
