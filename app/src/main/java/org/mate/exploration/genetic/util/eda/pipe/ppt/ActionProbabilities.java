package org.mate.exploration.genetic.util.eda.pipe.ppt;

import org.mate.interaction.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the action probabilities associated with each node in the {@link ApplicationStateTree}.
 */
public class ActionProbabilities {

    /**
     * The initial action probabilities (shared among all targets).
     */
    private final List<Float> initialProbabilities;

    /**
     * The list of actions.
     */
    private final List<Action> actions;

    /**
     * The probabilities associated with each target and each action.
     */
    private final List<List<Float>> probabilities;

    /**
     * Initialises a new set of action probabilities for a node in the PPT.
     *
     * @param targets The number of targets.
     * @param actionProbabilities The initial action probabilities shared among all targets.
     */
    public ActionProbabilities(int targets, Map<Action, Float> actionProbabilities) {
        this.initialProbabilities = new ArrayList<>(actionProbabilities.values());
        this.actions = new ArrayList<>(actionProbabilities.keySet());
        this.probabilities = new ArrayList<>(targets);
        for (int i = 0; i < targets; i++) { // lazily initialise the action probabilities per target
            this.probabilities.add(null);
        }
    }

    /**
     * Retrieves the action probabilities for a specific target.
     *
     * @param target The target for which the action probabilities should be retrieved.
     * @return Returns the action probabilities for the given target.
     */
    public Map<Action, Float> getActionProbabilities(int target) {

        List<Float> probabilities = this.probabilities.get(target);

        if (probabilities == null) { // init with initial probabilities
            this.probabilities.set(target, new ArrayList<>(initialProbabilities)); // shallow copy
            probabilities = this.probabilities.get(target);
        }

        return IntStream.range(0, actions.size())
                .boxed()
                .collect(Collectors.toMap(actions::get, probabilities::get));
    }

    /**
     * Removes the stored action probabilities for the given targets.
     *
     * @param targets The targets for which the action probabilities should be removed.
     */
    public void removeActionProbabilities(Set<Integer> targets) {
        for (final Integer target : targets) {
            this.probabilities.set(target, null);
        }
    }
}
