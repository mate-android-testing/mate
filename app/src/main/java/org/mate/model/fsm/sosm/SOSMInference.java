package org.mate.model.fsm.sosm;

import org.mate.interaction.action.Action;
import org.mate.model.fsm.FSM;
import org.mate.model.fsm.State;
import org.mate.model.fsm.sosm.subjective_logic.MultinomialOpinion;
import org.mate.model.fsm.sosm.subjective_logic.RawMultinomialOpinion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * This class incrementally infers the Subjective Opinions on each state of a FSMModel of the AUT.
 *  It essentially implements, Algorithm 1 of Neil and Rob paper's. The differences are that
 *  1) We avoid re-counting the how often actions are executed every time, but cache the counts and
 *     just update the counts every time a new list of Traces is added.
 *  2) In the original algorithm, Neil and Bob use the same certainty threshold for every state.
 *     This is fine when each state has approx. the same number of actions, but works less well if
 *      the number of actions can be largely different. Instead, the threshold to be absolutely
 *      certain that the first-order probabilities are correct is
 *          <number-of-action-in-that-state> * <certainty threshold>
 */
public final class SOSMInference {

    private final FSM fsm;
    private final double alpha;
    private final Map<State, List<Action>> additionalActions = new HashMap<>();
    private final Map<State, Map<Action, Integer>> frequenciesPerState = new HashMap<>();

    private final static List<Action> empty = new ArrayList<>(0);

    /**
     * Creates a new {@code SOSMInference} which initially has no traces that can be used to compute
     * the subjetive opinon of states.
     *
     * @param fsm The FSM describing the AUT.
     * @param alpha The Certainty Threashold.
     */

    public SOSMInference(final FSM fsm, double alpha) {
        this.fsm = requireNonNull(fsm);
        this.alpha = alpha;
    }

    /**
     * Records that an action can be executed in a particular state, even if the state does not know
     * that it is possible to execute that action in that state.
     *
     * To compute the subjectiv opinions, we need to know all actions that can be executed from a
     * state.
     * Because of a imprecise state equivalency definition, it is possible that an action can be
     * executed in a state, even if the state does not know that executing this action is possible.
     * In that case we need to externally record, that executing that action is in fact possible in
     * that state.
     *
     * How is it possible that a state does not know about an action that can be executed in that
     * state? Consider a State s0 in which actions a0, and a1 can be executed. Later in the search
     * we discover another state s1 (!= s0) in which actions a2 and a3 can be executed (where all
     * actions a0, a1, a2, and a3 are mutually different). What happens if action a2 is executed in
     * state s1, but s1 is equivalent to s0? Then we record that action a2 is executed in state s0.
     *
     * @param state The state in which the action is executed.
     * @param action The action that is executed.
     */
    public void addUnknownAction(final State state, final Action action) {
        final List<Action> actions = additionalActions.computeIfAbsent(state, ignored -> new ArrayList<>());
        if (!actions.contains(action)) {
            actions.add(action);
        }
    }

    private ActionsAndOpinion optionForState(final State state) {

        final Map<Action, Integer> frequencies = frequenciesPerState.get(state);

        final List<? extends Action> screenStateActions = state.getScreenState().getActions();
        final int numberOfActions = screenStateActions.size();
        final List<? extends Action> additional = additionalActions.getOrDefault(state, empty);
        final int numberOfAdditionalActions = additional.size();

        final int size = numberOfActions + numberOfAdditionalActions;
        final double[] actionFrequencies = new double[size];

        // Calculate actions frequency, total
        int total = 0;
        if (frequencies != null) {
            for (int i = 0; i < numberOfActions; ++i) {
                final Action action = screenStateActions.get(i);
                final int occurrences = frequencies.getOrDefault(action, 0);
                actionFrequencies[i] = occurrences;
                total += occurrences;
            }
            for (int i = 0; i < numberOfAdditionalActions; ++i) {
                final Action action = additional.get(i);
                final int occurrences = frequencies.getOrDefault(action, 0);
                actionFrequencies[i + numberOfActions] = occurrences;
                total += occurrences;
            }
        }

        final double uncertainty;
        if (total > 0) {
            final double div = 1.0 / Math.max(total, alpha * size);
            // Arrays.setAll(actionFrequencies, f -> f * div);
            for (int i = 0; i < size; ++i) {
                actionFrequencies[i] *= div;
            }

            uncertainty = 1.0 - ((double) total) * div;
        } else {
            //Arrays.fill(actionFrequencies, 0.0);
            uncertainty = 1.0;
        }

        final MultinomialOpinion opinion
                = new MultinomialOpinion(new RawMultinomialOpinion(actionFrequencies, uncertainty));
        return new ActionsAndOpinion(screenStateActions, additional, opinion);
    }

    /**
     * Implement Neil and Rob paper's Algorithm 1 for inferring SOSM given a FSM and a set of traces.
     * The interrence is incremental, so the given traces are just used in addition to all
     * previously given traces.
     *
     * @param traces The traces that should be additionally used to colcuate the subjetive opinions
     * @return A map that for each state, has a multinomial opinon on every action that can be
     * executed in that state.
     */
    public Map<State, ActionsAndOpinion> inferSOSM(final List<Trace> traces) {

        Trace.countFrequencies(frequenciesPerState, traces);

        return fsm.getStates().stream()
                .filter(s -> s.getId() != -1)
                .collect(Collectors.toMap(s -> s, this::optionForState));
    }

    @Override
    public String toString() {
        return String.format("SosmInference{fsm=%s, alpha=%s}", fsm, alpha);
    }
}

