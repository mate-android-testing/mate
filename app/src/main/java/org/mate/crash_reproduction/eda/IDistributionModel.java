package org.mate.crash_reproduction.eda;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

import java.util.Optional;
import java.util.Set;

public interface IDistributionModel<Node> {
    void update(Set<IChromosome<TestCase>> node);
    Optional<Node> drawNextNode(Node startNode);
    Optional<Node> getNextBestNode(Node startNode);
    Set<Node> getPossibleNodes(Node startNode);
}
