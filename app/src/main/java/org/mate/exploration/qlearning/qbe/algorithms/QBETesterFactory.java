package org.mate.exploration.qlearning.qbe.algorithms;

import org.mate.Properties;
import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.app.Application;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.transition_system.TransitionSystem;

import java.util.function.Supplier;

/**
 * We might or might not want to record and serialize a {@link TransitionSystem} while exploring
 * with QBE. This factory class returns the more complex (thus slower) {@link ApplicationTester} if
 * the {@link TransitionSystem} should be recorded. Otherwise, it returns the simpler
 * {@link SimpleTester}.
 */
public final class QBETesterFactory<S extends State<A>, A extends Action>
        implements Supplier<AbstractTester<S, A>> {
    private final Application<S, A> app;
    private final ExplorationStrategy<S, A> explorationStrategy;
    private final int maximumNumberOfActionPerTestCase = Properties.MAX_NUMBER_EVENTS();

    public QBETesterFactory(final Application<S, A> app,
                            final ExplorationStrategy<S, A> explorationStrategy) {
        this.app = app;
        this.explorationStrategy = explorationStrategy;
    }

    @Override
    public AbstractTester<S, A> get() {
        return Properties.QBE_RECORD_TRANSITION_SYSTEM()
                ? new ApplicationTester<>(app, explorationStrategy, maximumNumberOfActionPerTestCase)
                : new SimpleTester<>(app, explorationStrategy, maximumNumberOfActionPerTestCase);
    }
}
