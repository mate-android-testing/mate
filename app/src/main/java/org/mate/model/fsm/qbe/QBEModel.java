package org.mate.model.fsm.qbe;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.interaction.action.StartAction;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.model.fsm.State;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Defines an Extended Labeled Transition System (ELTS) as described on page 107/108 in the paper
 * "QBE: QLearning-Based Exploration of Android Applications".
 */
public class QBEModel implements IGUIModel {

    /**
     * Since the AUT can be non-deterministic, there might be multiple start screen states. To handle
     * them appropriately, we introduce a virtual root state that has an outgoing edge to each start
     * screen state.
     */
    private static final State VIRTUAL_ROOT_STATE = new QBEState(-1, new IScreenState() {

        @Override
        public String getId() {
            return "VIRTUAL_ROOT_STATE";
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

    private final ELTS elts;

    /**
     * Creates a new ELTS model with a given initial state.
     *
     * @param rootState The root or start state of the ELTS model.
     * @param packageName The package name of the AUT.
     */
    public QBEModel(IScreenState rootState, String packageName) {
        elts = new ELTS(VIRTUAL_ROOT_STATE, packageName);
        elts.addTransition(new QBETransition(VIRTUAL_ROOT_STATE, new QBEState(1, rootState),
                new StartAction(), null));
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


        if (actionResult == ActionResult.FAILURE_APP_CRASH) {
            // non-deterministic -> passive learning
        }

    }

    @Override
    public void update(IScreenState source, IScreenState target, Action action) {

    }

    @Override
    public Set<IScreenState> getStates() {
        return null;
    }

    @Override
    public boolean reachedNewState() {
        return false;
    }

    @Override
    public int getNumberOfStates() {
        return 0;
    }

    @Override
    public Set<Edge> getEdges(Action action) {
        return null;
    }

    @Override
    public Set<Edge> getEdges() {
        return null;
    }

    @Override
    public Optional<List<Edge>> shortestPath(IScreenState from, IScreenState to) {
        return Optional.empty();
    }

    @Override
    public IScreenState getScreenStateById(String screenStateId) {
        return null;
    }

    @Override
    public Set<IScreenState> getRootStates() {
        return null;
    }

    @Override
    public void addRootState(IScreenState rootState) {

    }

    @Override
    public Set<IScreenState> getActivityStates(String activity) {
        return null;
    }

    @Override
    public Set<IScreenState> getAppStates() {
        return null;
    }

    @Override
    public Set<String> getActivityPredecessors(String activity) {
        return null;
    }
}
