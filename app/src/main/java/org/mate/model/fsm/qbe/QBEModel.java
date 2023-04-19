package org.mate.model.fsm.qbe;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.interaction.action.StartAction;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;
import org.mate.state.IScreenState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines an Extended Labeled Transition System (ELTS) as described on page 107/108 in the paper
 * "QBE: QLearning-Based Exploration of Android Applications".
 */
public final class QBEModel implements IGUIModel {

    public final static QBEState VIRTUAL_ROOT_STATE = ELTS.VIRTUAL_ROOT_STATE;

    public final static QBEState CRASH_STATE = ELTS.CRASH_STATE;

    private final ELTS elts;

    /**
     * The package name of the AUT.
     */
    private final String packageName;

    private final List<List<QBETransition>> testsuite = new ArrayList<>();
    private List<QBETransition> testcase = new ArrayList<>();

    /**
     * Creates a new ELTS model with a given initial state.
     *
     * @param rootState   The root or start state of the ELTS model.
     * @param packageName The package name of the AUT.
     */
    public QBEModel(IScreenState rootState, String packageName) {
        this.packageName = requireNonNull(packageName);
        elts = new ELTS(VIRTUAL_ROOT_STATE, packageName);
        elts.addTransition(new QBETransition(VIRTUAL_ROOT_STATE, new QBEState(1, rootState),
                new StartAction(), ActionResult.SUCCESS));
    }

    /**
     * Updates the QBE model and inherently the underlying FSM with a new transition.
     *
     * @param source The source state.
     * @param target the target state.
     * @param action The action leading from the source to the target state.
     * @param actionResult The action result associated with the given action.
     */
    public void update(final IScreenState source, final IScreenState target, final Action action,
                       final ActionResult actionResult) {
        QBEState sourceState = (QBEState) elts.getState(source);
        QBEState targetState = actionResult.equals(ActionResult.FAILURE_APP_CRASH)
                ? CRASH_STATE : (QBEState) elts.getState(target);
        QBETransition transition = new QBETransition(sourceState, targetState, action, actionResult);
        elts.addTransition(transition);
        testcase.add(transition);
        if (!elts.isDeterministic()) {
                elts.passiveLearn(testsuite, testcase);
        }
    }

    @Override
    public void update(IScreenState source, IScreenState target, Action action) {
        throw new UnsupportedOperationException("QBEModel requires an ActionResult when updating.");
    }

    @Override
    public Set<IScreenState> getStates() {
        return elts.getStates().stream().map(State::getScreenState).collect(toSet());
    }

    public QBEState getCurrentState() {
        return (QBEState) elts.getCurrentState();
    }

    @Override
    public boolean reachedNewState() {
        return elts.reachedNewState();
    }

    @Override
    public int getNumberOfStates() {
        return elts.getNumberOfStates();
    }

    @Override
    public Set<Edge> getEdges(Action action) {
        return elts.getTransitions(action)
                .stream()
                .map(t -> new Edge(action, t.getSource().getScreenState(),
                        t.getTarget().getScreenState()))
                .collect(toSet());
    }

    @Override
    public Set<Edge> getEdges() {
        return elts.getTransitions()
                .stream()
                .map(t -> new Edge(t.getAction(), t.getSource().getScreenState(),
                        t.getTarget().getScreenState()))
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<List<Edge>> shortestPath(IScreenState from, IScreenState to) {
        State fromState = elts.getState(from);
        State toState = elts.getState(to);

        MATE.log_acc("Trying to find the shortest path from " + fromState + " to " + toState);
        return elts.shortestPath(fromState, toState).map(transitions ->
                transitions.stream()
                        .map(t -> new Edge(t.getAction(), t.getSource().getScreenState(),
                                t.getTarget().getScreenState()))
                        .collect(Collectors.toList()));
    }

    @Override
    public IScreenState getScreenStateById(String screenStateId) {
        return getStates().stream()
                .filter(screenState -> screenState.getId().equals(screenStateId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<IScreenState> getRootStates() {
        return elts.getOutgoingTransitions(elts.getRootState())
                .stream()
                .map(Transition::getTarget)
                .map(State::getScreenState)
                .collect(Collectors.toSet());
    }

    @Override
    public void addRootState(IScreenState rootState) {
        State root = elts.getState(rootState);
        elts.addTransition(new QBETransition(VIRTUAL_ROOT_STATE, root, new StartAction(), ActionResult.SUCCESS));
        if (!testcase.isEmpty()) {
            testsuite.add(testcase);
            testcase = new ArrayList<>();
        }
    }

    @Override
    public Set<IScreenState> getActivityStates(String activity) {
        return getStates().stream()
                .filter(screenState -> screenState.getActivityName().equals(activity))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<IScreenState> getAppStates() {
        return getStates().stream()
                .filter(screenState -> screenState.getPackageName().equals(packageName))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getActivityPredecessors(String activity) {
        return elts.getActivityPredecessors(activity);
    }

    public ELTS getELTS() {
        return elts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return elts.toString();
    }
}
