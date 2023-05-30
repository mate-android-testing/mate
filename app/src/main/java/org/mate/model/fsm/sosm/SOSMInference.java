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
 * Incrementally infers the subjective opinions for each state from a FSM of the AUT. The
 * implementation is based on the paper 'Modelling Second-Order Uncertainty in State Machines', see
 * https://ieeexplore.ieee.org/document/10057480. There are two minor modifications:
 *
 *  1) We avoid re-counting how often actions are executed every time, but cache the counts and
 *     just update the counts every time a new list of traces is added.
 *  2) In the original algorithm, the same certainty threshold is used for every state. This is
 *     suitable when each state has approx. the same number of actions, but works less well if the
 *     number of actions can be largely vary between states. Instead, we use a threshold that
 *     considers the number of actions and a fixed certainty threshold.
 */
public final class SOSMInference {

    /**
     * The underlying FSM.
     */
    private final FSM fsm;

    /**
     * The used certainty threshold.
     */
    private final double alpha;

    /**
     * Stores the additional actions per state. This can happen due to the impreciseness of the used
     * state equivalence function.
     */
    private final Map<State, List<Action>> additionalActions = new HashMap<>();

    /**
     * Stores for each state how often an action was executed.
     */
    private final Map<State, Map<Action, Integer>> frequenciesPerState = new HashMap<>();

    /**
     * An empty list.
     */
    private final static List<Action> empty = new ArrayList<>(0);

    /**
     * Creates a new SOSMInference with initially no trace information.
     *
     * @param fsm The FSM describing the AUT.
     * @param alpha The certainty threshold alpha.
     */
    public SOSMInference(final FSM fsm, double alpha) {
        this.fsm = requireNonNull(fsm);
        this.alpha = alpha;
    }

    /**
     * Records that an action can be executed in a particular state, even if the state does not know
     * that it is possible to execute that action in that state. This situation can occur due to the
     * impreciseness in the underlying state equivalence function.
     * However, to compute the subjective opinions we need to know all actions that can be executed
     * in a particular state. Thus, we need to track those 'unknown' actions.
     *
     * @param state The state in which the 'unknown' action is executed.
     * @param action The 'unknown' action executed in the specified state.
     */
    public void addUnknownAction(final State state, final Action action) {
        final List<Action> actions = additionalActions.computeIfAbsent(state,
                ignored -> new ArrayList<>());
        if (!actions.contains(action)) {
            actions.add(action);
        }
    }

    /**
     * Retrieves the actions and the associated multinomial opinion for the given state.
     *
     * @param state The given state.
     * @return Returns the actions and the associated multinomial opinion for the given state.
     */
    private ActionsAndOpinion actionsAndOpinionForState(final State state) {

        final Map<Action, Integer> frequencies = frequenciesPerState.get(state);

        final List<? extends Action> screenStateActions = state.getScreenState().getActions();
        final int numberOfActions = screenStateActions.size();
        final List<? extends Action> additionalActions
                = this.additionalActions.getOrDefault(state, empty);
        final int numberOfAdditionalActions = additionalActions.size();

        final int size = numberOfActions + numberOfAdditionalActions;
        final double[] actionFrequencies = new double[size];

        // Calculate action frequencies and total occurrences.
        int total = 0;
        if (frequencies != null) {

            for (int i = 0; i < numberOfActions; ++i) {
                final Action action = screenStateActions.get(i);
                final int occurrences = frequencies.getOrDefault(action, 0);
                actionFrequencies[i] = occurrences;
                total += occurrences;
            }

            for (int i = 0; i < numberOfAdditionalActions; ++i) {
                final Action action = additionalActions.get(i);
                final int occurrences = frequencies.getOrDefault(action, 0);
                actionFrequencies[i + numberOfActions] = occurrences;
                total += occurrences;
            }
        }

        // Calculate the uncertainty.
        final double uncertainty;

        if (total > 0) {

            // Unlike in the original algorithm, we choose a certainty threshold that is state
            // dependent, i.e. we consider the number of actions in the given state along with the
            // constant certainty threshold alpha.
            final double div = 1.0 / Math.max(total, alpha * size);

            for (int i = 0; i < size; ++i) {
                actionFrequencies[i] *= div;
            }

            uncertainty = 1.0 - ((double) total) * div; // line 11
        } else {
            uncertainty = 1.0;
        }

        final MultinomialOpinion opinion
                = new MultinomialOpinion(new RawMultinomialOpinion(actionFrequencies, uncertainty));
        return new ActionsAndOpinion(screenStateActions, additionalActions, opinion);
    }

    /**
     * Implements Algorithm 1 of 'Modelling Second-Order Uncertainty in State Machines' for inferring
     * a SOSM given a FSM and a set of traces. The inference is incremental, so the given traces
     * are just used in addition to all previously given traces.
     *
     * @param traces The traces that should be additionally used to calculate the subjective opinions.
     * @return Returns a mapping that describes for each state the executable actions and the
     *         associated subjective opinions.
     */
    public Map<State, ActionsAndOpinion> inferSOSM(final List<Trace> traces) {

        Trace.countFrequencies(frequenciesPerState, traces);

        return fsm.getStates().stream()
                .filter(s -> s.getId() != -1) // exclude the virtual root state
                .collect(Collectors.toMap(s -> s, this::actionsAndOpinionForState));
    }

    @Override
    public String toString() {
        return String.format("SosmInference{fsm=%s, alpha=%s}", fsm, alpha);
    }
}

