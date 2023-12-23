package org.mate.exploration.genetic.util.eda.pipe.ppt;

import android.support.annotation.NonNull;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.StartAction;
import org.mate.interaction.action.intent.IntentAction;
import org.mate.interaction.action.intent.IntentBasedAction;
import org.mate.interaction.action.intent.SystemAction;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateType;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * The probabilistic prototype tree (PPT).
 */
public class ApplicationStateTree {

    /**
     * Initialises the probabilities (action weights) of a state.
     */
    private final BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializeNodeFunction;

    /**
     * The actual PPT.
     */
    private final Tree<ApplicationStateNode> tree;

    /**
     * The current position in the PPT.
     */
    private TreeNode<ApplicationStateNode> cursor;

    /**
     * Since the AUT can be non-deterministic, there might be multiple start screen states. To handle
     * them appropriately, we introduce a virtual root state that has an outgoing edge to each start
     * screen state.
     */
    private static final IScreenState VIRTUAL_ROOT_STATE = new IScreenState() {

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
        public List<Action> getActions() {
            return Collections.singletonList(new StartAction());
        }

        @Override
        public List<UIAction> getUIActions() {
            throw new UnsupportedOperationException("Do not call this method!");
        }

        @Override
        public List<SystemAction> getSystemActions() {
            throw new UnsupportedOperationException("Do not call this method!");
        }

        @Override
        public List<IntentBasedAction> getIntentBasedActions() {
            throw new UnsupportedOperationException("Do not call this method!");
        }

        @Override
        public List<IntentAction> getIntentActions() {
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

        @NonNull
        @Override
        public String toString() {
            return getId() + " [" + getActivityName() + "]";
        }
    };

    /**
     * Initialises a new PPT.
     *
     * @param initializeNodeFunction The initialization function for the action weights of a state.
     */
    public ApplicationStateTree(BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializeNodeFunction) {
        this.initializeNodeFunction = initializeNodeFunction;
        tree = new Tree<>(initializeNode(Collections.emptyList(), VIRTUAL_ROOT_STATE));
        cursor = addRootState(Registry.getUiAbstractionLayer().getLastScreenState());
    }

    /**
     * Adds a new root state below the virtual root state.
     *
     * @param rootState The new root state.
     * @return Returns the newly added root state in the tree.
     */
    private TreeNode<ApplicationStateNode> addRootState(IScreenState rootState) {
        final ApplicationStateNode rootNode
                = initializeNode(Collections.singletonList(new StartAction()), rootState);
        // TODO: Enable 'non-deterministic' actions in the PPT. Right now, this overwrites the
        //  outgoing action transition to a previous root state. In turn, the action will not be
        //  attached to all root states in the PPT when drawn.
        getRoot().getContent().actionToNextState.put(new StartAction(), rootState);
        return getRoot().addChild(rootNode);
    }

    /**
     * Returns the action probabilities of the current PPT state.
     *
     * @return Returns the action probabilities of the current PPT state.
     */
    public Map<Action, Double> getActionProbabilities() {
        return cursor.getContent().actionProbabilities;
    }

    /**
     * Returns the current PPT state.
     *
     * @return Returns the current PPT state.
     */
    public IScreenState getState() {
        return cursor.getContent().state;
    }

    /**
     * Updates the position in the PPT with the given action transition.
     *
     * @param testCase The currently executed test case.
     * @param action The lastly executed action of the test case.
     * @param currentScreenState The current screen state.
     */
    public void updatePosition(final TestCase testCase, final Action action, final IScreenState currentScreenState) {
        cursor.getContent().updateActionToNextState(action, currentScreenState);
        cursor = cursor.getChild(s -> s.state.equals(currentScreenState))
                .orElseGet(() -> cursor.addChild(initializeNode(testCase.getActionSequence(), currentScreenState)));
    }

    /**
     * Updates the current position in the PPT.
     *
     * @param currentScreenState The new position in the PPT.
     */
    public void updatePositionImmutable(final IScreenState currentScreenState) {

        // relative change from current position
        cursor = cursor.getChild(s -> s.state.equals(currentScreenState))
                .orElseThrow(() -> new IllegalStateException("Can't locate state in child nodes!"));
    }

    /**
     * Resets the cursor position to the given root node of the PPT.
     *
     * @param currentScreenState The new root state.
     */
    public void resetPosition(final IScreenState currentScreenState) {

        // create new root state if not yet existent
        cursor = tree.getRoot();
        cursor = cursor.getChild(s -> s.state.equals(currentScreenState))
                .orElseGet(() -> addRootState(currentScreenState));
    }

    /**
     * Retrieves the root node of the PPT.
     *
     * @return Returns the root node of the PPT.
     */
    public TreeNode<ApplicationStateNode> getRoot() {
        return tree.getRoot();
    }

    /**
     * Provides a textual representation of the PPT.
     *
     * @return Returns a textual representation of the PPT.
     */
    @Override
    public String toString() {

        // TODO: Checkout https://www.geeksforgeeks.org/print-n-ary-tree-graphically/.

        final StringBuilder builder = new StringBuilder();

        final Queue<TreeNode<ApplicationStateNode>> bfsQueue = new LinkedList<>();
        bfsQueue.add(tree.getRoot());

        while(!bfsQueue.isEmpty()) {

            int size = bfsQueue.size(); // allows level-wise printing

            for (int i = 0; i < size; i++) {
                final TreeNode<ApplicationStateNode> node = bfsQueue.poll();
                builder.append(node.getContent().state.getId() + "(" + node.getChildren().size() + ") ");

                for (TreeNode<ApplicationStateNode> child : node.getChildren()) {
                    bfsQueue.add(child);
                }
            }
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    /**
     * Initialises a new node in the PPT, i.e. computes the action probabilities.
     *
     * @param prevActions The list of actions that lead to the current state.
     * @param state The current screen state.
     * @return Returns the initialized node.
     */
    private ApplicationStateNode initializeNode(final List<Action> prevActions, final IScreenState state) {
        return new ApplicationStateNode(state, initializeNodeFunction.apply(prevActions, state));
    }

    /**
     * A single node in the PPT.
     */
    public static class ApplicationStateNode {

        /**
         * The underlying screen state.
         */
        private final IScreenState state;

        /**
         * The action probabilities of the state.
         */
        private final Map<Action, Double> actionProbabilities;

        /**
         * The outgoing action transitions of the state.
         */
        private final Map<Action, IScreenState> actionToNextState = new HashMap<>();

        /**
         * Constructs a new node in the PPT.
         *
         * @param state The underlying screen state.
         * @param actionProbabilities The action probabilities of the state.
         */
        private ApplicationStateNode(IScreenState state, Map<Action, Double> actionProbabilities) {
            this.state = state;
            this.actionProbabilities = actionProbabilities;
        }

        /**
         * Adds a new outgoing action transition.
         *
         * @param action The action triggering a transition.
         * @param nextState The resulting state upon applying the given action.
         */
        private void updateActionToNextState(final Action action, final IScreenState nextState) {
            actionToNextState.put(action, nextState);
        }

        /**
         * Retrieves the action with the highest action probability.
         *
         * @return Returns the action with the highest assigned probability if possible, otherwise
         *          {@code null} is returned.
         */
        public Action getActionWithBiggestProbability() {
            return actionProbabilities.entrySet().stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    // States that don't belong to the AUT do not have any outgoing actions.
                    .orElse(null);
        }

        /**
         * Retrieves the action probabilities for the given state.
         *
         * @return Returns the action probabilities for the given state.
         */
        public Map<Action, Double> getActionProbabilities() {
            return actionProbabilities;
        }

        /**
         * Retrieves the outgoing action transitions.
         *
         * @return Returns the outgoing action transitions.
         */
        public Map<Action, IScreenState> getActionToNextState() {
            return actionToNextState;
        }

        /**
         * Retrieves the underlying screen state.
         *
         * @return Returns the underlying screen state.
         */
        public IScreenState getState() {
            return state;
        }

        @Override
        public String toString() {
            return state.toString();
        }
    }
}
