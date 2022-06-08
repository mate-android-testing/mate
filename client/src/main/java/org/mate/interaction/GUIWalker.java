package org.mate.interaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ActionResult;
import org.mate.commons.interaction.action.intent.IntentBasedAction;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Randomness;
import org.mate.commons.utils.manifest.element.ComponentDescription;
import org.mate.commons.utils.manifest.element.IntentFilterDescription;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
     * Whether quick launch should be used or not.
     */
    private static final boolean quickLaunch = Properties.QUICK_LAUNCH();

    /**
     * The exported activities from the AndroidManifest.xml. Only present, when {@link #quickLaunch}
     * is enabled.
     */
    private List<ComponentDescription> activities;

    /**
     * The name of the main activity.
     */
    private static final String mainActivity = Registry.getMainActivity();

    /**
     * Initialises the gui walker.
     *
     * @param uiAbstractionLayer The ui abstraction layer.
     */
    public GUIWalker(UIAbstractionLayer uiAbstractionLayer) {
        this.guiModel = uiAbstractionLayer.getGuiModel();
        this.uiAbstractionLayer = uiAbstractionLayer;
        if (quickLaunch) {
            activities = Registry.getManifest().getActivities().stream()
                    .filter(activity -> activity.isExported() && activity.isEnabled())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Moves the AUT into the given (screen) state.
     *
     * @param screenStateId The screen state id.
     * @return Returns {@code true} if the transition to the screen state was successful, otherwise
     *         {@code false} is returned.
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
     *         {@code false} is returned.
     */
    public boolean goToState(final IScreenState screenState) {

        Objects.requireNonNull(screenState, "Screen state is null!");

        if (uiAbstractionLayer.getLastScreenState().equals(screenState)) {
            // we are already at the desired screen state
            return true;
        }

        if (quickLaunch) {
            return quickLaunch(screenState)
                    // try to quick launch close activity and then step to final state
                    || quickLaunchCloseActivity(screenState.getActivityName())
                    // fall back mechanism
                    || goFromTo(uiAbstractionLayer.getLastScreenState(), screenState);
        } else {
            return goFromTo(uiAbstractionLayer.getLastScreenState(), screenState);
        }
    }

    /**
     * Tries to reach the given state directly through an intent.
     *
     * @param screenState The state that should be reached.
     * @return Returns {@code true} if the state could be successfully reached, otherwise
     *         {@code false} is returned.
     */
    private boolean quickLaunch(final IScreenState screenState) {
        return quickLaunch(screenState.getActivityName());
    }

    /**
     * Tries to launch the given activity directly through an intent.
     *
     * @param activityName The activity that should be launched.
     * @return Returns {@code true} if the activity could be successfully launched, otherwise
     *         {@code false} is returned.
     */
    private boolean quickLaunch(String activityName) {

        /*
         * TODO: Under certain (yet unknown) conditions a quick launch might not work. This can be
         *  even the main activity or any activity alias referring to the main activity. There might
         *  be a relation to crash that happened shortly before.
         */

        MATELog.log_acc("Try to quick launch activity: " + activityName);

        if (mainActivity.equals(activityName)) {
            return goToMainActivity();
        }

        // look up whether the activity has been exported via the manifest
        Optional<ComponentDescription> optActivityComponent = activities.stream()
                .filter(activity -> activity.getFullyQualifiedName().equals(activityName))
                .findFirst();

        if (!optActivityComponent.isPresent()) {
            // activity hasn't been exported
            return false;
        }

        ComponentDescription activityComponent = optActivityComponent.get();

        if (!activityComponent.hasIntentFilter()) {
            /*
             * If the component doesn't define any intent filter at all, the activity can be only
             * started via an explicit intent. Since we use in any case an explicit intent here, we
             * simply add a dummy intent filter to be able to re-use the same functionality.
             */
            activityComponent.addIntentFilter(new IntentFilterDescription());
        }

        // try out all intent filters until one satisfies the resolver
        for (IntentFilterDescription intentFilter : activityComponent.getIntentFilters()) {

            Intent intent = constructIntentForActivity(activityComponent, intentFilter);

            // check whether the resolver is happy with the constructed intent for the activity
            if (Registry.getContext().getPackageManager().resolveActivity(intent, 0) != null) {
                IntentBasedAction intentBasedAction
                        = new IntentBasedAction(intent, activityComponent, intentFilter);
                boolean success = replayActions(Collections.singletonList(intentBasedAction));

                final String currentActivity = Registry.getUiAbstractionLayer().getCurrentActivity();

                // check that we actually reached the target activity
                if (success && (currentActivity.equals(activityName)
                        /*
                         * TODO: Re-verify this behaviour once the current activity name can be
                         *  fetched reliable. Right now, it seems like the current activity equals
                         *  the name of the activity alias and this alternative in the if statement
                         *  is superfluous.
                         */
                        || (activityComponent.isActivityAlias()
                        && currentActivity.equals(activityComponent.getTargetActivity())))) {
                    return true;
                }
            }
        }

        // we couldn't quick launch the activity through an intent
        return false;
    }

    /**
     * Tries to quick launch a close (neighbouring) activity and then steps to the final activity.
     *
     * @param activity The target activity.
     * @return Returns {@code true} if the activity could be successfully launched, otherwise
     *         {@code false} is returned.
     */
    private boolean quickLaunchCloseActivity(String activity) {

        MATELog.log_acc("Try to quick launch activity from close activity!");

        Set<String> activityPredecessors = guiModel.getActivityPredecessors(activity);

        if (!activityPredecessors.isEmpty()) {
            // this can only happen if the given activity is the main activity
            return false;
        }

        Set<IScreenState> targetActivityStates = guiModel.getActivityStates(activity);

        for (String activityPredecessor : activityPredecessors) {
            // try to quick launch a neighbouring activity
            if (quickLaunch(activityPredecessor)) {
                // try to move to any state of the target activity
                for (IScreenState targetActivityState : targetActivityStates) {
                    if (goFromTo(uiAbstractionLayer.getLastScreenState(), targetActivityState)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Constructs an intent matching the given intent filter.
     *
     * @param activityComponent The activityComponent component.
     * @param intentFilter The intent filter.
     * @return Returns an intent matching the intent filter.
     */
    private Intent constructIntentForActivity(final ComponentDescription activityComponent,
                                              final IntentFilterDescription intentFilter) {

        Intent intent = new Intent();

        // add a random action if present
        if (intentFilter.hasAction()) {
            String action = Randomness.randomElement(intentFilter.getActions());
            intent.setAction(action);
        }

        // add a random category if present
        if (intentFilter.hasCategory()) {
            String category = Randomness.randomElement(intentFilter.getCategories());
            intent.addCategory(category);
        }

        // make the intent explicit
        intent.setComponent(new ComponentName(Registry.getPackageName(),
                activityComponent.getFullyQualifiedName()));

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } catch (Exception e) {
            e.printStackTrace();
            MATELog.log("EXCEPTION CLEARING ACTIVITY FLAG");
        }

        // TODO: add further properties like a data uri if necessary
        return intent;
    }

    /**
     * Replays the given actions on the current screen state.
     *
     * @param actions The list of actions that should be executed.
     * @return Returns {@code true} if all actions could be applied successfully, otherwise
     *         {@code false} is returned.
     */
    private boolean replayActions(final List<Action> actions) {
        for (Action action : actions) {
            ActionResult result = uiAbstractionLayer.executeAction(action);
            if (result != ActionResult.SUCCESS) {
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
     *         {@code false} is returned.
     */
    private boolean goFromTo(final IScreenState source, final IScreenState target) {

        Optional<List<Action>> shortestPath = guiModel.shortestPath(source, target)
                .map(path -> path.stream().map(Edge::getAction).collect(Collectors.toList()));

        if (shortestPath.isPresent()) {
            return replayActions(shortestPath.get())
                    // check that we actually reached the target state
                    && uiAbstractionLayer.getLastScreenState().equals(target);
        } else {
            MATELog.log_acc("No path from " + source.getId() + " to " + target.getId() + "!");

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

    /**
     * Moves the AUT to the given activity.
     *
     * @param activity The activity that should be launched.
     * @return Returns {@code true} if the activity could be reached, otherwise {@code false} is
     *         returned.
     */
    public boolean goToActivity(String activity) {

        if (uiAbstractionLayer.getCurrentActivity().equals(activity)) {
            // we are already on the target activity
            return true;
        }

        if (quickLaunch) {
            return quickLaunch(activity)
                    // try to quick launch close activity and then step to final state
                    || quickLaunchCloseActivity(activity)
                    // fall back mechanism
                    || goToActivityByState(activity);
        } else {
            return goToActivityByState(activity);
        }
    }

    /**
     * Moves the AUT to the given activity.
     *
     * @param activity The activity that should be launched.
     * @return Returns {@code true} if the activity could be reached, otherwise {@code false} is
     *         returned.
     */
    private boolean goToActivityByState(String activity) {

        /*
         * In general, an activity consists of multiple screen states and in theory we should reach
         * any state, i.e. we could pick a single state on the target activity. But during replaying
         * the actions, we may encounter some inconsistency, i.e. we end up in a completely different
         * state. Thus, we try out all possible target states in the worst case.
         */
        Set<IScreenState> targetActivityStates = guiModel.getActivityStates(activity);

        for (IScreenState targetActivityState : targetActivityStates) {
            if (uiAbstractionLayer.getCurrentActivity().equals(activity)) {
                // we reached the target activity
                return true;
            } else {
                // internally re-starts the app if there is no path from the current state
                goFromTo(uiAbstractionLayer.getLastScreenState(), targetActivityState);
            }
        }
        return false;
    }

    /**
     * Moves the AUT to the main activity, i.e. the start screen.
     *
     * @return Returns {@code true} if the main activity could be launched, otherwise {@code false}
     *         is returned.
     */
    public boolean goToMainActivity() {
        Context context = Registry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(Registry.getPackageName());
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } catch (Exception e) {
            e.printStackTrace();
            MATELog.log("EXCEPTION CLEARING ACTIVITY FLAG");
        }
        context.startActivity(intent);
        return uiAbstractionLayer.getCurrentActivity().equals(mainActivity);
    }
}
