package org.mate.exploration.genetic.util.eda.pipe.ppt;

import org.mate.interaction.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the action probabilities associated with each node in the {@link ApplicationStateTree}.
 */
public class ActionProbabilities {

    /**
     * The number of targets.
     */
    private final int targets;

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
        this.targets = targets;
        this.actions = new ArrayList<>(actionProbabilities.keySet());
        this.probabilities = new ArrayList<>(targets);
        for (int i = 0; i < targets; i++) { // each target requires a copy
            this.probabilities.add(new ArrayList<>(actionProbabilities.values()));
        }
    }

    /**
     * Retrieves the action probabilities for a specific target.
     *
     * @param target The target for which the action probabilities should be retrieved.
     * @return Returns the action probabilities for the given target.
     */
    public Map<Action, Float> getActionProbabilities(int target) {
        final List<Float> probabilities = this.probabilities.get(target);
        return IntStream.range(0, actions.size())
                .boxed()
                .collect(Collectors.toMap(actions::get, probabilities::get));
    }
}
