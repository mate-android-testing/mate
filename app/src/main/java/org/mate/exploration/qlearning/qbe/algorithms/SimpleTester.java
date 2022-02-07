package org.mate.exploration.qlearning.qbe.algorithms;

import org.mate.MATE;
import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.app.Application;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.utils.Pair;

import java.util.Optional;

public final class SimpleTester<S extends State<A>, A extends Action> extends AbstractTester<S, A> {

    public SimpleTester(final Application<S, A> app,
                        final ExplorationStrategy<S, A> explorationStrategy,
                        final long timeoutInMilliseconds,
                        final int maximumNumberOfActionPerTestCase) {
        super(app, explorationStrategy, timeoutInMilliseconds, maximumNumberOfActionPerTestCase);
    }

    @Override
    public void run() {

        final long startTime = System.currentTimeMillis();

        while (!reachedTimeout(startTime)) {

            app.reset();
            S currentState = app.getCurrentState();

            boolean noTerminalState;
            boolean noCrash = true;
            int testcaseLength = 0;

            do {
                final Optional<A> chosenAction = explorationStrategy.chooseAction(currentState);
                MATE.log_debug("Choose action:" + chosenAction);
                noTerminalState = chosenAction.isPresent();

                if (noTerminalState) {
                    final Pair<Optional<S>, UIAbstractionLayer.ActionResult> result
                            = app.executeAction(chosenAction.get());
                    final Optional<S> nextState = result.first;
                    noCrash = nextState.isPresent();
                    ++testcaseLength;
                    if (noCrash) {
                        currentState = nextState.get();
                    }
                }
            } while (noTerminalState && noCrash
                    && testcaseLength < maximumNumberOfActionPerTestCase && !reachedTimeout(startTime));
        }
    }

}
