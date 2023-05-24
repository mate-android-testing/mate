package org.mate.model.fsm.sosm;

import org.mate.interaction.action.Action;
import org.mate.model.fsm.sosm.subjective_logic.BinomialOpinion;
import org.mate.model.fsm.sosm.subjective_logic.MultinomialOpinion;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Associates the opinions of a {@link MultinomialOpinion} with the corresponding actions.
 *
 * The problem is that a {@link MultinomialOpinion} only has a first, second, third, etc. opinion,
 * but we want an opinion on an action a, b, c etc. This class bridges the gap between actions and
 * the indexes of a multinomial opinion.
 */
public final class ActionsAndOpinion {

    /**
     * Associates with each action an action index.
     */
    private final Map<Action, Integer> actionsMap;

    /**
     * The multinomial opinion.
     */
    private final MultinomialOpinion opinion;

    /**
     * Constructs a new association between actions and a multinomial opinion.
     *
     * @param actions The actions of a screen state.
     * @param additionalActions The additional actions that have been discovered in the screen state.
     * @param opinion The multinomial opinion.
     */
    public ActionsAndOpinion(final List<? extends Action> actions,
                             final List<? extends Action> additionalActions,
                             final MultinomialOpinion opinion) {

        this.opinion = requireNonNull(opinion);

        final int size = actions.size() + additionalActions.size();
        actionsMap = new HashMap<>(size);

        for (int i = 0; i < actions.size(); ++i) {
            actionsMap.put(actions.get(i), i);
        }

        for (int i = 0; i < additionalActions.size(); ++i) {
            actionsMap.put(additionalActions.get(i), i + actions.size());
        }
    }

    /**
     * Retrieves the uncertainty of the multinomial opinion.
     *
     * @return Returns the uncertainty of the multinomial opinion.
     */
    public double getUncertainty() {
        return opinion.getUncertainty();
    }

    /**
     * Computes the binomial opinion of the given action.
     *
     * @param action The action for which the binomial opinion should be computed.
     * @return Returns the binomial opinion of the given action or {@code null} if no opinion is
     *         present.
     */
    public BinomialOpinion opinionOfAction(final Action action) {
        final Integer index = actionsMap.get(action);
        return index != null ? opinion.coarsenToOpinion(index) : null;
    }

    /**
     * Returns the actions associated with the multinomial opinion.
     *
     * @return Returns the actions associated with the multinomial opinion.
     */
    public Set<Action> getActions() {
        return Collections.unmodifiableSet(actionsMap.keySet());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final ActionsAndOpinion that = (ActionsAndOpinion) o;
            return opinion.equals(that.opinion)
                    && actionsMap.keySet().equals(that.actionsMap.keySet());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionsMap.keySet(), opinion);
    }

    @Override
    public String toString() {
        return String.format("ActionsAndOpinion{actionsMap=%s, option=%s}", actionsMap, opinion);
    }
}

