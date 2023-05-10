package org.mate.model.fsm.sosm;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.StartAction;
import org.mate.model.fsm.FSMModel;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;
import org.mate.model.fsm.sosm.subjective_logic.BinomialOpinion;
import org.mate.model.fsm.sosm.subjective_logic.RawBinomialOpinion;
import org.mate.state.IScreenState;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;


public final class SOSMModel extends FSMModel {

    public static final BinomialOpinion UNKNOWN_STATE_OPINION = new BinomialOpinion(
            new RawBinomialOpinion(0.0, 0.0, 1.0, 0.0));

    public final static BinomialOpinion UNKNOWN_ACTION_OPINION = new BinomialOpinion(
            new RawBinomialOpinion(0.0, 0.0, 1.0, 0.0));

    private final SOSMInference inference;

    private Map<State, ActionsAndOpinion> opinionPerState;

    private final List<Transition> recordedTransitions = new ArrayList<>();

    private static final NumberFormat formatter = NumberFormat.getInstance();

    static {
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
    }

    public SOSMModel(IScreenState rootState, String packageName) {
        super(rootState, packageName);
        recordedTransitions.add(new Transition(VIRTUAL_ROOT_STATE, new State(0, rootState),
                new StartAction()));
        this.inference = new SOSMInference(fsm, Properties.SOSM_UNCERTAINTY_THRESHOLD());
        opinionPerState = new HashMap<>(0);
    }

    public void updateSOSM(final List<Trace> traces) {
        opinionPerState = inference.inferSOSM(traces);
    }

    /**
     * Retrieves the current FSM state.
     *
     * @return Returns the current FSM state.
     */
    public State getCurrentState() {
        return fsm.getCurrentState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRootState(IScreenState rootState) {
        State root = fsm.getState(rootState);
        final Transition transition = new Transition(VIRTUAL_ROOT_STATE, root, new StartAction());
        fsm.addTransition(transition);
        recordedTransitions.add(transition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(IScreenState source, IScreenState target, Action action) {
        State sourceState = fsm.getState(source);
        State targetState = fsm.getState(target);
        Transition transition = new Transition(sourceState, targetState, action);
        fsm.addTransition(transition);
        recordedTransitions.add(transition);
    }

    public List<Transition> getRecordedTransitions() {
        return Collections.unmodifiableList(recordedTransitions);
    }

    public void resetRecordedTransitions() {
        recordedTransitions.clear();
    }

    /**
     * May contain null entries that correspond to states, for which no opinion has been calculated before.
     */
    public List<ActionsAndOpinion> getMultinomialOpinionsFor(final Trace trace) {
        return trace.stream()
                .map(t -> opinionPerState.get(t.getSource()))
                .collect(toList());
    }

    public List<BinomialOpinion> getCoarsenedBinomialOpinionsFor(final Trace trace) {
        /*
         * What opinion should be returned if asked for an opinion on a state that has not been
         * seen by the SOSM before? This can happen if a new state is discovered in a generation.
         */
        final List<BinomialOpinion> opinions = new ArrayList<>(trace.size());

        for (Transition transition : trace) {
            final State state = transition.getSource();
            final ActionsAndOpinion actionsAndOpinion = opinionPerState.get(state);

            if (actionsAndOpinion == null) {
                opinions.add(UNKNOWN_STATE_OPINION);
                continue;
            }

            final Action triggeredAction = transition.getAction();
            BinomialOpinion opinion = actionsAndOpinion.opinionOfAction(triggeredAction);

            if (opinion == null) {
                // Unknown actions can occur, because of a fuzzy state equivalence definition.
                inference.addUnknownAction(state, triggeredAction);
                opinions.add(UNKNOWN_ACTION_OPINION);
                continue;
            }

            opinions.add(opinion);
        }

        return opinions;
    }

    public ActionsAndOpinion getActionsAndOpinionOn(final State state) {
        return opinionPerState.get(state);
    }

    private String actionAndOpinionToStr(final Action action, final BinomialOpinion opinion) {

        final RawBinomialOpinion op = opinion.getRawOpinion();
        final String actionStr = action.toShortString();

        return actionStr +
                ":\\n" +
                "B = " +
                formatter.format(op.getBelief()) +
                ", D = " +
                formatter.format(op.getDisbelief()) +
                ", U = " +
                formatter.format(op.getUncertainty()) +
                ", A = " +
                formatter.format(op.getApriori());
    }

    public List<String> toDOT() {
        final List<String> lines = new ArrayList<>();
        lines.add("digraph {");

        final StringBuilder b = new StringBuilder();
        dotListStates(b, lines);
        final Map<String, Integer> actionIndex = dotListActions(b, lines);
        dotListTransitions(b, actionIndex, lines);
        b.append("}");
        lines.add(b.toString());
        b.setLength(0);
        return lines;
    }

    private void dotListTransitions(final StringBuilder b, final Map<String, Integer> actionIndex,
                                    List<String> lines) {
        requireNonNull(b);
        requireNonNull(actionIndex);
        requireNonNull(lines);

        final int transitions_per_line = 8;
        int newlineCount = 1;

        b.append('\t');
        for (final Transition t : fsm.getTransitions()) {
            /* The virtual root state has id '-1', but 'S-1' is not a valid identifier
             * in DOT, so we just print 'SS' instead.
             */
            final int sourceId = t.getSource().getId();
            if (sourceId == -1) {
                b.append("SS -> S")
                        .append(t.getTarget().getId())
                        .append(";");

                if (newlineCount % transitions_per_line == 0) {
                    lines.add(b.toString());
                    b.setCharAt(0, '\t');
                    b.setLength(1);
                } else {
                    b.append(" ");
                }

                ++newlineCount;
                continue;
            }

            final Action action = t.getAction();
            final Optional<BinomialOpinion> opinion = Optional.ofNullable(opinionPerState.get(t.getSource()))
                    .map(a -> a.opinionOfAction(t.getAction()))
                    .filter(op -> op.getUncertainty() != 1.0);

            if (opinion.isPresent()) {
                final String str = actionAndOpinionToStr(action, opinion.get());
                final Integer index = actionIndex.get(str);
                if (index != null) {
                    b.append("S")
                            .append(sourceId)
                            .append(" -> A")
                            .append(index)
                            .append(" -> S")
                            .append(t.getTarget().getId())
                            .append(";");

                    if (newlineCount % transitions_per_line == 0) {
                        lines.add(b.toString());
                        b.setCharAt(0, '\t');
                        b.setLength(1);
                    } else {
                        b.append(" ");
                    }

                    ++newlineCount;
                } else {
                    MATE.log_warn(String.format("Action '%s' not found.", str));
                }
            }
        }
    }

    private Map<String, Integer> dotListActions(final StringBuilder b, List<String> lines) {

        requireNonNull(b);
        requireNonNull(lines);

        final Map<String, Integer> actionsIndex = new HashMap<>();

        final String formatAsBox = "\",shape=\"box\",style=\"filled\",fillcolor=\"#E6E6E6\",color=\"#FFFFFF\"];";
        for (final Transition t : fsm.getTransitions()) {
            final Action action = t.getAction();
            Optional.ofNullable(opinionPerState.get(t.getSource()))
                    .map(a -> a.opinionOfAction(t.getAction()))
                    .filter(op -> op.getUncertainty() != 1.0)
                    .ifPresent(op -> {
                        final String str = actionAndOpinionToStr(action, op);
                        if (!actionsIndex.containsKey(str)) {
                            actionsIndex.put(str, actionsIndex.size());
                            final int index = actionsIndex.get(str);
                            b.append("\tA")
                                    .append(index)
                                    .append(" [label=\"")
                                    .append(str)
                                    .append(formatAsBox);
                            lines.add(b.toString());
                            b.setLength(0);
                        }
                    });
        }

        return actionsIndex;
    }

    private void dotListStates(final StringBuilder b, List<String> lines) {

        requireNonNull(b);
        requireNonNull(lines);

        for (final State state : fsm.getStates()) {
            /* The virtual root state has id '-1', but 'S-1' is not a valid identifier in DOT, so we
             * just print 'SS' instead.
             */
            final int id = state.getId();
            b.append("\tS").append(id != -1 ? Integer.toString(id) : "S");
            Optional.ofNullable(state.getScreenState())
                    .ifPresent(screenState -> b.append(" [label=\"")
                            .append(state.getScreenState()
                                    .getActivityName())
                            .append("\"]"));
            b.append(";");
            lines.add(b.toString());
            b.setLength(0);
        }
    }

    @Override
    public String toString() {
        return String.format("Sosm{inference=%s, s=%s}", inference, opinionPerState);
    }

}

