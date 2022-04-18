package org.mate.exploration.eda;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IDistributionModel<Node> {
    void update(Set<List<Node>> node);
    Optional<Node> drawNextNode(Node startNode);
}
