package org.mate.exploration.qlearning.qbe.algorithms;

import org.mate.MATE;
import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.app.Application;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.interaction.action.ActionResult;
import org.mate.utils.Pair;

import java.util.Optional;

public final class SimpleTester<S extends State<A>, A extends Action> extends AbstractTester<S, A> {

    public SimpleTester(final Application<S, A> app,
                        final ExplorationStrategy<S, A> explorationStrategy,
                        final int maximumNumberOfActionPerTestCase) {
        super(app, explorationStrategy, maximumNumberOfActionPerTestCase);
    }

    @Override
    public void run() {
        while (true) {
            app.reset();
            S currentState = app.getCurrentState();

            boolean reachedTerminalState = false;
            boolean discoveredCrash = false;
            int testcaseLength = 0;

            while (!reachedTerminalState && !discoveredCrash
                    && testcaseLength < maximumNumberOfActionPerTestCase) {

                final Optional<A> chosenAction = explorationStrategy.chooseAction(currentState);
                MATE.log_debug("Choose action: " + chosenAction);
                reachedTerminalState = !chosenAction.isPresent();

                if (!reachedTerminalState) {
                    final Pair<Optional<S>, ActionResult> result = app.executeAction(chosenAction.get());
                    final Optional<S> nextState = result.first;
                    discoveredCrash = !nextState.isPresent();
                    ++testcaseLength;
                    if (!discoveredCrash) {
                        currentState = nextState.get();
                    }
                }
            }
        }
    }
}
