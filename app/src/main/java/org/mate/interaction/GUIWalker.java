package org.mate.interaction;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enables to move directly or indirectly to a given {@link IScreenState} or activity.
 */
public class GUIWalker {

    /**
     * The current gui model.
     */
    private final IGUIModel guiModel;

    /**
     * Enables the interaction with the AUT.
     */
    private final UIAbstractionLayer uiAbstractionLayer;

    /**
     * Initialises the gui walker.
     *
     * @param uiAbstractionLayer The ui abstraction layer.
     */
    public GUIWalker(UIAbstractionLayer uiAbstractionLayer){
        this.guiModel = uiAbstractionLayer.getGuiModel();
        this.uiAbstractionLayer = uiAbstractionLayer;
    }

    /**
     * Moves the AUT into the given (screen) state.
     *
     * @param screenStateId The screen state id.
     * @return Returns {@code true} if the transition to the screen state was successful, otherwise
     *          {@code false} is returned.
     */
    public boolean goToState(String screenStateId) {
        IScreenState screenState = guiModel.getScreenStateById(screenStateId);
        return screenState != null && goToState(screenState);
    }

    /**
     * Moves the AUT into the given (screen) state.
     *
     * @param screenState The screen state.
     * @return Returns {@code true} if the transition to the screen state was successful, otherwise
     *          {@code false} is returned.
     */
    public boolean goToState(final IScreenState screenState) {

        Objects.requireNonNull(screenState, "Screen state is null!");

        if (uiAbstractionLayer.getLastScreenState().equals(screenState)) {
            // we are already at the desired screen state
            return true;
        }

        return goFromTo(uiAbstractionLayer.getLastScreenState(), screenState);
    }

    /**
     * Replays the given actions on the current screen state.
     *
     * @param actions The list of actions that should be executed.
     * @return Returns {@code true} if all actions could be applied successfully, otherwise
     *          {@code false} is returned.
     */
    private boolean replayActions(List<Action> actions) {
        for (Action action : actions) {
            ActionResult result = uiAbstractionLayer.executeAction(action);
            if (result != ActionResult.SUCCESS && result != ActionResult.SUCCESS_NEW_STATE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Moves the AUT from the given source to the given target state.
     *
     * @param source The source state.
     * @param target The target state.
     * @return Returns {@code true} if the transition to the target state was possible, otherwise
     *          {@code false} is returned.
     */
    private boolean goFromTo(IScreenState source, IScreenState target) {

        Optional<List<Action>> shortestPath = guiModel.shortestPath(source, target)
                .map(path -> path.stream().map(Edge::getAction).collect(Collectors.toList()));

        if (shortestPath.isPresent()) {
            return replayActions(shortestPath.get())
                    // check that we actually reached the target state
                    && uiAbstractionLayer.getLastScreenState().equals(target);
        } else {
            MATE.log_acc("No path from " + source.getId() + " to " + target.getId() + "!");

            // If there is not direct path from the source state, re-try it from the initial state
            uiAbstractionLayer.restartApp();
            shortestPath = guiModel.shortestPath(uiAbstractionLayer.getLastScreenState(), target)
                    .map(path -> path.stream().map(Edge::getAction).collect(Collectors.toList()));

            if (shortestPath.isPresent()) {
                return replayActions(shortestPath.get())
                        // check that we actually reached the target state
                        && uiAbstractionLayer.getLastScreenState().equals(target);
            }
        }
        return false;
    }
    
    public boolean goToActivity(String activity) {

        if (uiAbstractionLayer.getCurrentActivity().equals(activity)) {
            // we are already on the wanted activity
            return true;
        }

        throw new UnsupportedOperationException();
    }

    public boolean goToMainActivity() {
        throw new UnsupportedOperationException();
    }
}
