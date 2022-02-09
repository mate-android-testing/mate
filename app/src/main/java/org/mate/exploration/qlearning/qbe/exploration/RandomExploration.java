package org.mate.exploration.qlearning.qbe.exploration;

import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;
import org.mate.utils.Randomness;

import java.util.Optional;
import java.util.Set;

/**
 * Provides a random exploration strategy.
 *
 * @param <S> The generic state type.
 * @param <A> The generic action type.
 */
public final class RandomExploration<S extends State<A>, A extends Action> implements ExplorationStrategy<S, A> {

    /**
     * Chooses a random action from the current state.
     *
     * @param currentState The current state.
     * @return Returns a random action applicable on the current state if possible.
     */
    @Override
    public Optional<A> chooseAction(final S currentState) {
        final Set<A> possibleActions = currentState.getActions();
        return possibleActions.isEmpty()
                ? Optional.empty() : Optional.of(Randomness.randomElement(possibleActions));
    }
}
