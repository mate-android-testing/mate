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
 * This class associates the opinions of a {@link MultinomialOpinion} with the corresponding
 * actions.
 *
 * The problem is that a {@link MultinomialOpinion} only has a first, second, third, etc. opinion,
 * but we want an opinion on an action a, b, c etc. This class bridges the gap between actions and
 * the indexes of a multinomial opinion.
 */
public final class ActionsAndOpinion {

    private final Map<Action, Integer> actionsMap;
    private final MultinomialOpinion opinion;

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

    public double getUncertainty() {
        return opinion.getUncertainty();
    }

    public BinomialOpinion opinionOfAction(final Action action) {
        final Integer index = actionsMap.get(action);
        return index != null ? opinion.coarsenToOpinion(index) : null;
    }

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
            return opinion.equals(that.opinion) && actionsMap.keySet().equals(that.actionsMap.keySet());
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

