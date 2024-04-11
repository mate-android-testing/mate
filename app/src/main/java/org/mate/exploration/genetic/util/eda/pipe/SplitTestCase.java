package org.mate.exploration.genetic.util.eda.pipe;

import java.util.List;

/**
 * Splits a test case into good and bad actions.
 */
class SplitTestCase {

    /**
     * The list of good actions in a test case (those that improve the fitness).
     */
    final List<NodeWithPickedAction> goodActions;

    /**
     * The list of bad actions in a test case (those that don't improve the fitness).
     */
    final List<NodeWithPickedAction> badActions;

    /**
     * Creates a split test case consisting of the given good and bad actions.
     *
     * @param goodActions The list of good actions.
     * @param badActions The list of bad actions.
     */
    SplitTestCase(List<NodeWithPickedAction> goodActions, List<NodeWithPickedAction> badActions) {
        this.goodActions = goodActions;
        this.badActions = badActions;
    }
}
