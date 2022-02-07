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
    protected final long timeoutInMilliseconds;
    protected final int maximumNumberOfActionPerTestCase;

    public AbstractTester(final Application<S, A> app,
                        final ExplorationStrategy<S, A> explorationStrategy,
                        final long timeoutInMilliseconds,
                        final int maximumNumberOfActionPerTestCase) {
        this.app = Objects.requireNonNull(app);
        this.explorationStrategy = Objects.requireNonNull(explorationStrategy);
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        this.maximumNumberOfActionPerTestCase = maximumNumberOfActionPerTestCase;

        if (timeoutInMilliseconds <= 0) {
            throw new IllegalArgumentException("The timeout must be a positive value");
        }

        if (maximumNumberOfActionPerTestCase <= 0) {
            throw new IllegalArgumentException(
                    "The maximum number of actions per test case need to be at least 1.");
        }
    }

    /**
     * Checks whether the specified timeout has been reached.
     *
     * @param startTime The starting time of the exploration.
     * @return Returns {@code true} if the timeout has been reached, otherwise {@code false}.
     */
    protected boolean reachedTimeout(final long startTime) {
        final long currentTime = System.currentTimeMillis();
        return currentTime - startTime > timeoutInMilliseconds;
    }
}
