package org.mate.model.fsm.sosm;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.StartAction;
import org.mate.model.fsm.FSMModel;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;
import org.mate.model.fsm.sosm.subjective_logic.BinomialOpinion;
import org.mate.model.fsm.sosm.subjective_logic.MultinomialOpinion;
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


/**
 * A FSM model of the AUT, that enhances the {@link FSMModel} by adding an
 * {@link MultinomialOpinion} to each state. The {@link MultinomialOpinion} describes the likelihood
 * of any specific action being executed in that state.
 */
public final class SOSMModel extends FSMModel {

    /**
     * The initial subjective opinion for states that we haven't seen.
     */
    public static final BinomialOpinion UNKNOWN_STATE_OPINION = new BinomialOpinion(
            new RawBinomialOpinion(0.0, 0.0, 1.0, 0.0));

    /**
     * We may encounter the situation that an action is not applicable in a given state due to
     * impreciseness of the underlying state equivalence function. In this case we assign an opinion
     * with an uncertainty of {@code 1.0}.
     */
    public final static BinomialOpinion UNKNOWN_ACTION_OPINION = new BinomialOpinion(
            new RawBinomialOpinion(0.0, 0.0, 1.0, 0.0));

    /**
     * Enables to incrementally infer the SOSM from a set of traces.
     */
    private final SOSMInference inference;

    /**
     * Stores the subjective opinion for each state.
     */
    private Map<State, ActionsAndOpinion> opinionPerState;

    /**
     * The transitions taken by a test case. Turned into a {@link Trace} after the test case is
     * finished.
     */
    private final List<Transition> recordedTransitions = new ArrayList<>();

    /**
     * Used to format the DOT graph description of the SOSM.
     */
    private static final NumberFormat formatter = NumberFormat.getInstance();

    static {
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
    }

    /**
     * Creates a new {@code SOSMModel} which is a {@link FSMModel} of the AUT, enhanced with a
     * subjective opinion for each state.
     *
     * @param rootState The root state of the AUT.
     * @param packageName The package name of the AUT.
     */
    public SOSMModel(IScreenState rootState, String packageName) {
        super(rootState, packageName);
        recordedTransitions.add(new Transition(VIRTUAL_ROOT_STATE, new State(0, rootState),
                new StartAction()));
        this.inference = new SOSMInference(fsm, Properties.SOSM_CERTAINTY_THRESHOLD());
        opinionPerState = new HashMap<>(0);
    }

    /**
     * Update the subjective opinions of the SOSM by including the given traces in addition to all
     * previous given traces in the inference.
     *
     * @param traces The list of traces that that should additionally be included in the inference.
     */
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

    /**
     * Returns all transitions that have been recorded since the last time the recorded transitions
     * have been reset. Transitions are recoded whenever a test case executes an action.
     *
     * @return Returns all recorded transitions since the last reset.
     */
    public List<Transition> getRecordedTransitions() {
        return Collections.unmodifiableList(recordedTransitions);
    }

    /**
     * Clears the list of recorded transitions.
     */
    public void resetRecordedTransitions() {
        recordedTransitions.clear();
    }

    /**
     * Retrieves a multinomial opinion for each state described by the trace. May contain
     * {@code null} entries for states that haven't assigned any subjective opinion so far.
     *
     * @param trace Describes the transitions of a test case.
     * @return Returns a list of action opinion pairs for the actions inferred from the trace.
     */
    public List<ActionsAndOpinion> getMultinomialOpinionsFor(final Trace trace) {
        return trace.stream()
                .map(t -> opinionPerState.get(t.getSource()))
                .collect(toList());
    }

    /**
     * Retrieves a coarsened binomial opinion for the executed actions according to the given trace.
     *
     * @param trace Describes the transitions of a test case.
     * @return Returns a list of coarsened binomial opinions for the actions described by the trace.
     */
    public List<BinomialOpinion> getCoarsenedBinomialOpinionsFor(final Trace trace) {

        final List<BinomialOpinion> opinions = new ArrayList<>(trace.size());

        for (Transition transition : trace) {

            final State state = transition.getSource();
            final ActionsAndOpinion actionsAndOpinion = opinionPerState.get(state);

            if (actionsAndOpinion == null) {
                // No subjective opinion has been computed for the state so far.
                opinions.add(UNKNOWN_STATE_OPINION);
                continue;
            }

            final Action triggeredAction = transition.getAction();
            final BinomialOpinion opinion = actionsAndOpinion.opinionOfAction(triggeredAction);

            if (opinion == null) {
                // Unknown actions can occur because of a imprecise state equivalence function.
                inference.addUnknownAction(state, triggeredAction);
                opinions.add(UNKNOWN_ACTION_OPINION);
                continue;
            }

            opinions.add(opinion);
        }

        return opinions;
    }

    /**
     * Retrieves the subjective opinion for the give state.
     *
     * @param state The state for which the subjective opinion should be retrieved.
     * @return Returns the subjective opinion for the given state.
     */
    public ActionsAndOpinion getActionsAndOpinionOn(final State state) {
        return opinionPerState.get(state);
    }

    /**
     * A helper function that constructs a DOT conform string representation for an action and the
     * associated subjective opinion.
     *
     * @param action The given action.
     * @param opinion The given opinion.
     * @return Returns a DOT representation for the given action and opinion.
     */
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

    /**
     * Converts the SOSM to DOT format.
     *
     * @return Returns the SOSM in DOT format line-by-line.
     */
    public List<String> convertToDOT() {

        final List<String> lines = new ArrayList<>();
        lines.add("digraph {");

        final StringBuilder builder = new StringBuilder();
        convertStatesToDOT(builder, lines);
        final Map<String, Integer> actionIndex = convertActionsToDOT(builder, lines);
        convertTransitionsToDOT(builder, actionIndex, lines);
        builder.append("}");
        lines.add(builder.toString());
        builder.setLength(0);
        return lines;
    }

    /**
     * Converts transitions to DOT format.
     *
     * @param builder The current string builder.
     * @param actionIndex A mapping of actions to its index.
     * @param lines The current line-by-line DOT representation.
     */
    private void convertTransitionsToDOT(final StringBuilder builder,
                                         final Map<String, Integer> actionIndex,
                                         final List<String> lines) {
        requireNonNull(builder);
        requireNonNull(actionIndex);
        requireNonNull(lines);

        final int transitionsPerLine = 8;
        int newlineCount = 1;

        builder.append('\t');
        for (final Transition transition : fsm.getTransitions()) {

            /* The virtual root state has id '-1', but 'S-1' is not a valid identifier
             * in DOT, so we just print 'SS' instead.
             */
            final int sourceId = transition.getSource().getId();

            if (sourceId == -1) {
                builder.append("SS -> S")
                        .append(transition.getTarget().getId())
                        .append(";");

                if (newlineCount % transitionsPerLine == 0) {
                    lines.add(builder.toString());
                    builder.setCharAt(0, '\t');
                    builder.setLength(1);
                } else {
                    builder.append(" ");
                }

                ++newlineCount;
                continue;
            }

            final Action action = transition.getAction();
            final Optional<BinomialOpinion> opinion
                    = Optional.ofNullable(opinionPerState.get(transition.getSource()))
                    .map(a -> a.opinionOfAction(transition.getAction()))
                    .filter(op -> op.getUncertainty() != 1.0);

            if (opinion.isPresent()) {
                final String actionAndOpinionStr = actionAndOpinionToStr(action, opinion.get());
                final Integer index = actionIndex.get(actionAndOpinionStr);
                if (index != null) {
                    builder.append("S")
                            .append(sourceId)
                            .append(" -> A")
                            .append(index)
                            .append(" -> S")
                            .append(transition.getTarget().getId())
                            .append(";");

                    if (newlineCount % transitionsPerLine == 0) {
                        lines.add(builder.toString());
                        builder.setCharAt(0, '\t');
                        builder.setLength(1);
                    } else {
                        builder.append(" ");
                    }

                    ++newlineCount;
                } else {
                    MATE.log_warn(String.format("Action '%s' not found.", actionAndOpinionStr));
                }
            }
        }
    }

    /**
     * Converts actions to DOT format.
     *
     * @param builder The current string builder.
     * @param lines The current line-by-line DOT representation.
     * @return Returns a mapping of actions to its index.
     */
    private Map<String, Integer> convertActionsToDOT(final StringBuilder builder,
                                                     List<String> lines) {

        requireNonNull(builder);
        requireNonNull(lines);

        final Map<String, Integer> actionsIndex = new HashMap<>();

        final String formatAsBox = "\",shape=\"box\",style=\"filled\",fillcolor=\"#E6E6E6\"," +
                "color=\"#FFFFFF\"];";

        for (final Transition transition : fsm.getTransitions()) {

            final Action action = transition.getAction();

            Optional.ofNullable(opinionPerState.get(transition.getSource()))
                    .map(actionAndOpinion -> actionAndOpinion.opinionOfAction(transition.getAction()))
                    .filter(op -> op.getUncertainty() != 1.0)
                    .ifPresent(op -> {
                        final String actionAndOpinionStr = actionAndOpinionToStr(action, op);
                        if (!actionsIndex.containsKey(actionAndOpinionStr)) {
                            actionsIndex.put(actionAndOpinionStr, actionsIndex.size());
                            final int index = actionsIndex.get(actionAndOpinionStr);
                            builder.append("\tA")
                                    .append(index)
                                    .append(" [label=\"")
                                    .append(actionAndOpinionStr)
                                    .append(formatAsBox);
                            lines.add(builder.toString());
                            builder.setLength(0);
                        }
                    });
        }

        return actionsIndex;
    }

    /**
     * Converts states to DOT format.
     *
     * @param builder The current string builder.
     * @param lines The current line-by-line DOT representation.
     */
    private void convertStatesToDOT(final StringBuilder builder, List<String> lines) {

        requireNonNull(builder);
        requireNonNull(lines);

        for (final State state : fsm.getStates()) {

            /* The virtual root state has id '-1', but 'S-1' is not a valid identifier in DOT, so we
             * just print 'SS' instead.
             */
            final int id = state.getId();
            builder.append("\tS").append(id != -1 ? Integer.toString(id) : "S");
            Optional.ofNullable(state.getScreenState())
                    .ifPresent(screenState -> builder.append(" [label=\"")
                            .append(state.getScreenState().getActivityName())
                            .append("\"]"));
            builder.append(";");
            lines.add(builder.toString());
            builder.setLength(0);
        }
    }

    @Override
    public String toString() {
        return String.format("Sosm{inference=%s, s=%s}", inference, opinionPerState);
    }
}

