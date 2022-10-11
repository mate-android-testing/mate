package org.mate.model.fsm;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.StartAction;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateType;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the gui model through a finite state machine.
 */
public class FSMModel implements IGUIModel {

    public static final String VIRTUAL_ROOT_REPRESENTATION = "VIRTUAL_ROOT_STATE";

    /**
     * Since the AUT can be non-deterministic, there might be multiple start screen states. To handle
     * them appropriately, we introduce a virtual root state that has an outgoing edge to each start
     * screen state.
     */
    private static final State VIRTUAL_ROOT_STATE = new State(-1, new IScreenState() {

        @Override
        public String getId() {
            return VIRTUAL_ROOT_REPRESENTATION;
        }

        @Override
        public void setId(String stateId) {
            throw new UnsupportedOperationException("Do not call this method!");
        }

        @Override
        public List<Widget> getWidgets() {
            throw new UnsupportedOperationException("Do not call this method!");
        }

        @Override
        public List<UIAction> getActions() {
            throw new UnsupportedOperationException("Do not call this method!");
        }

        @Override
        public List<WidgetAction> getWidgetActions() {
            throw new UnsupportedOperationException("Do not call this method!");
        }

        @Override
        public List<MotifAction> getMotifActions() {
            throw new UnsupportedOperationException("Do not call this method!");
        }

        @Override
        public String getActivityName() {
            return "VIRTUAL_ROOT_STATE_ACTIVITY";
        }

        @Override
        public String getPackageName() {
            return "VIRTUAL_ROOT_STATE_PACKAGE";
        }

        @Override
        public ScreenStateType getType() {
            return ScreenStateType.ACTION_SCREEN_STATE;
        }
    });

    /**
     * The finite state machine.
     */
    protected final FSM fsm;

    /**
     * The package name of the AUT.
     */
    protected final String packageName;

    /**
     * Creates a new FSM based model with a given initial state.
     *
     * @param rootState The root or start state of the FSM model.
     * @param packageName The package name of the AUT.
     */
    public FSMModel(IScreenState rootState, String packageName) {
        this.packageName = packageName;
        fsm = new FSM(VIRTUAL_ROOT_STATE, packageName);
        fsm.addTransition(new Transition(VIRTUAL_ROOT_STATE, new State(0, rootState),
                new StartAction()));
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Edge> getEdges(Action action) {
        Set<Transition> transitions = fsm.getTransitions(action);
        Set<Edge> edges = new HashSet<>();
        for (Transition transition : transitions) {
            IScreenState source = transition.getSource().getScreenState();
            IScreenState target = transition.getTarget().getScreenState();
            edges.add(new Edge(action, source, target));
        }
        return edges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Edge> getEdges() {
        return fsm.getTransitions().stream()
                .map(transition -> new Edge(transition.getAction(),
                        transition.getSource().getScreenState(),
                        transition.getTarget().getScreenState()))
                .collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean reachedNewState() {
        return fsm.reachedNewState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<IScreenState> getStates() {
        Set<IScreenState> screenStates = new HashSet<>();
        for (State state : fsm.getStates()) {
            screenStates.add(state.getScreenState());
        }
        return screenStates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfStates() {
        return fsm.getNumberOfStates();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<Edge>> shortestPath(IScreenState from, IScreenState to) {
        State fromState = fsm.getState(from);
        State toState = fsm.getState(to);
        MATE.log_acc("Trying to find the shortest path from " + fromState + " to " + toState);
        return fsm.shortestPath(fromState, toState)
                .map(transitions -> transitions.stream()
                        .map(transition -> new Edge(transition.getAction(),
                                transition.getSource().getScreenState(),
                                transition.getTarget().getScreenState()))
                        .collect(Collectors.toList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IScreenState getScreenStateById(String screenStateId) {
        return getStates().stream()
                .filter(screenState -> screenState.getId().equals(screenStateId))
                .findFirst()
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<IScreenState> getRootStates() {
        return fsm.getOutgoingTransitions(fsm.getRootState())
                .stream()
                .map(Transition::getTarget)
                .map(State::getScreenState)
                .collect(Collectors.toSet());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRootState(IScreenState rootState) {
        State root = fsm.getState(rootState);
        fsm.addTransition(new Transition(VIRTUAL_ROOT_STATE, root, new StartAction()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<IScreenState> getActivityStates(String activity) {
        return getStates().stream()
                .filter(screenState -> screenState.getActivityName().equals(activity))
                .collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<IScreenState> getAppStates() {
        return getStates().stream()
                .filter(screenState -> screenState.getPackageName().equals(packageName))
                .collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getActivityPredecessors(String activity) {
        return fsm.getActivityPredecessors(activity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return fsm.toString();
    }
}
