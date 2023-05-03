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

public final class SOSMInference {

    private final FSM fsm;
    private final double alpha;
    private final Map<State, List<Action>> additionalActions = new HashMap<>();
    private final Map<State, Map<Action, Integer>> frequenciesPerState = new HashMap<>();

    private final static List<Action> empty = new ArrayList<>(0);

    public SOSMInference(final FSM fsm, double alpha) {
        this.fsm = requireNonNull(fsm);
        this.alpha = alpha;
    }

    public void addUnknownAction(final State state, final Action action) {
        final List<Action> actions = additionalActions.computeIfAbsent(state, ignored -> new ArrayList<>());
        if (!actions.contains(action)) {
            actions.add(action);
        }
    }

    public ActionsAndOpinion optionForState(final State state) {

        final Map<Action, Integer> frequencies = frequenciesPerState.get(state);

        final List<? extends Action> screenStateActions = state.getScreenState().getActions();
        final int ssa = screenStateActions.size();
        final List<? extends Action> additional = additionalActions.getOrDefault(state, empty);
        final int aa = additional.size();

        final int size = ssa + aa;
        final double[] actionFrequencies = new double[size];

        // Calculate actions frequency, total
        int total = 0;
        if (frequencies != null) {
            for (int i = 0; i < ssa; ++i) {
                final Action action = screenStateActions.get(i);
                final int occurrences = frequencies.getOrDefault(action, 0);
                actionFrequencies[i] = occurrences;
                total += occurrences;
            }
            for (int i = 0; i < aa; ++i) {
                final Action action = additional.get(i);
                final int occurrences = frequencies.getOrDefault(action, 0);
                actionFrequencies[i + ssa] = occurrences;
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
     */
    public Map<State, ActionsAndOpinion> inferSOSM(final List<Trace> traces) {

        Trace.countFrequencies(frequenciesPerState, traces);

        // TODO: parallelize?
        return fsm.getStates().stream()
                .filter(s -> s.getId() != -1)
                .collect(Collectors.toMap(s -> s, this::optionForState));
    }

    @Override
    public String toString() {
        return String.format("SosmInference{fsm=%s, alpha=%s}", fsm, alpha);
    }
}

