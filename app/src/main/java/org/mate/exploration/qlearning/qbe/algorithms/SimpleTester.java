package org.mate.exploration.qlearning.qbe.algorithms;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.MATE;
import org.mate.exploration.Algorithm;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.Application;
import org.mate.exploration.qlearning.qbe.interfaces.State;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.utils.Pair;

import java.util.Objects;
import java.util.Optional;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class SimpleTester<S extends State<A>, A extends Action> implements Algorithm {

    private final Application<S, A> app;
    private final ExplorationStrategy<S, A> explorationStrategy;
    private final long timeoutInMilliseconds;
    private final int maximumNumberOfActionPerTestcase;

    public SimpleTester(final Application<S, A> app,
                        final ExplorationStrategy<S, A> explorationStrategy,
                        final long timeoutInMilliseconds,
                        final int maximumNumberOfActionPerTestcase) {
        this.app = Objects.requireNonNull(app);
        this.explorationStrategy = Objects.requireNonNull(explorationStrategy);
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        this.maximumNumberOfActionPerTestcase = maximumNumberOfActionPerTestcase;

        if (timeoutInMilliseconds <= 0) {
            throw new IllegalArgumentException("The timeout must be a positive value");
        }

        if (maximumNumberOfActionPerTestcase <= 0) {
            throw new IllegalArgumentException(
                    "The maximum number of actions per test case need to be at least 1.");
        }
    }

    public void run() {
        final long startTime = System.currentTimeMillis();
        while (noTimeout(startTime)) {
            app.reset();
            S currentState = app.getCurrentState();

            boolean noTerminalState;
            boolean noCrash = true; // Initialization not necessary, but the compiler cannot figure it out.
            int testcaseLength = 0;
            do {
                final Optional<A> chosenAction = explorationStrategy.chooseAction(currentState);
                MATE.log_debug("Choose action:" + chosenAction.toString());
                noTerminalState = chosenAction.isPresent();
                if (noTerminalState) {
                    final Pair<Optional<S>, UIAbstractionLayer.ActionResult> result = app.executeAction(chosenAction.get());
                    final Optional<S> nextState = result.first;
                    noCrash = nextState.isPresent();
                    ++testcaseLength;
                    if (noCrash) {
                        currentState = nextState.get();
                    }

                }
            } while (noTerminalState && noCrash && testcaseLength < maximumNumberOfActionPerTestcase && noTimeout(startTime));
        }
    }

    private boolean noTimeout(final long startTime) {
        final long currentTime = System.currentTimeMillis();
        return currentTime - startTime < timeoutInMilliseconds;
    }
}
