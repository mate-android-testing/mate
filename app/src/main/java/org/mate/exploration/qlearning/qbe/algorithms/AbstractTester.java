package org.mate.exploration.qlearning.qbe.algorithms;

import org.mate.exploration.Algorithm;
import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.app.Application;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;

import java.util.Objects;

public abstract class AbstractTester<S extends State<A>, A extends Action> implements Algorithm {

    protected final Application<S, A> app;
    protected final ExplorationStrategy<S, A> explorationStrategy;
    protected final int maximumNumberOfActionPerTestCase;

    public AbstractTester(final Application<S, A> app,
                        final ExplorationStrategy<S, A> explorationStrategy,
                        final int maximumNumberOfActionPerTestCase) {
        this.app = Objects.requireNonNull(app);
        this.explorationStrategy = Objects.requireNonNull(explorationStrategy);
        this.maximumNumberOfActionPerTestCase = maximumNumberOfActionPerTestCase;

        if (maximumNumberOfActionPerTestCase <= 0) {
            throw new IllegalArgumentException(
                    "The maximum number of actions per test case need to be at least 1.");
        }
    }
}
