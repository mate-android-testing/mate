package org.mate.interaction;

import android.os.RemoteException;
import android.util.Log;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exceptions.AUTCrashException;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.model.TestCase;
import org.mate.model.fsm.FSMModel;
import org.mate.model.util.DotConverter;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.state.ScreenStateType;
import org.mate.utils.Randomness;
import org.mate.utils.StackTrace;
import org.mate.utils.Utils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.mate.interaction.action.ActionResult.FAILURE_APP_CRASH;
import static org.mate.interaction.action.ActionResult.FAILURE_EMULATOR_CRASH;
import static org.mate.interaction.action.ActionResult.FAILURE_UNKNOWN;
import static org.mate.interaction.action.ActionResult.SUCCESS;
import static org.mate.interaction.action.ActionResult.SUCCESS_OUTBOUND;

/**
 * TODO: make singleton
 * Enables high-level interactions with the AUT.
 */
public class UIAbstractionLayer {

    /**
     * The maximal number of retries (screen state fetching) when the ui automator is disconnected.
     */
    private static final int UiAutomatorDisconnectedRetries = 3;

    /**
     * The error message when the ui automator is disconnected.
     */
    private static final String UiAutomatorDisconnectedMessage = "UiAutomation not connected!";

    /**
     * The package name of the AUT.
     */
    private final String packageName;

    /**
     * Provides the low-level routines to execute different kind of actions.
     */
    private final DeviceMgr deviceMgr;

    /**
     * The last fetched screen state.
     */
    private IScreenState lastScreenState;

    /**
     * The assigned index to the last screen state.
     */
    private int lastScreenStateNumber = 0;

    /**
     * The current gui model.
     */
    private final IGUIModel guiModel;

    /**
     * Enables moving the AUT into an arbitrary state or activity.
     */
    private final GUIWalker guiWalker;

    /**
     * The activities belonging to the AUT.
     */
    private final List<String> activities;

    /**
     * Initialises the ui abstraction layer.
     *
     * @param deviceMgr The device manager responsible for executing all kind of actions.
     * @param packageName The package name of the AUT.
     */
    public UIAbstractionLayer(DeviceMgr deviceMgr, String packageName) {
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        activities = deviceMgr.getActivities();
        // check for any kind of dialogs (permission, crash, ...) initially
        lastScreenState = clearScreen();
        String id = "S" + lastScreenStateNumber;
        lastScreenState.setId(id);
        lastScreenStateNumber++;
        if (Properties.SURROGATE_MODEL()) {
            guiModel = new SurrogateModel(lastScreenState, packageName);
        } else {
            guiModel = new FSMModel(lastScreenState, packageName);
        }
        guiWalker = new GUIWalker(this);

        // Save screenshot
        if ((Properties.CONVERT_GUI_TO_DOT() != DotConverter.Option.NONE)
                && Properties.DOT_WITH_SCREENSHOTS()) {
            DotConverter.takeScreenshot(id, lastScreenState.getPackageName());
        }
    }

    /**
     * Returns the list of executable ui actions on the current screen.
     *
     * @return Returns the list of executable widget actions.
     */
    public List<UIAction> getExecutableActions() {
        return getLastScreenState().getActions();
    }

    /**
     * Executes the given action. Retries execution the action when the
     * UIAutomator is disconnected for a pre-defined number of times.
     *
     * @param action The action that should be executed.
     * @return Returns the outcome of the execution, e.g. success.
     */
    public ActionResult executeAction(Action action) {
        boolean retry = true;
        int retryCount = 0;

        /*
        * FIXME: The UIAutomator bug seems to be unresolvable right now.
        *  We have tried to restart the ADB server, but afterwards the
        *   connection is still broken. Fortunately, this bug seems to appear
        *   very rarely recently.
         */
        while (retry) {
            retry = false;
            try {
                return executeActionUnsafe(action);
            } catch (Exception e) {
                if (e instanceof IllegalStateException
                        && Objects.equals(e.getMessage(), UiAutomatorDisconnectedMessage)
                        && retryCount < UiAutomatorDisconnectedRetries) {
                    retry = true;
                    retryCount += 1;
                    continue;
                }
                Log.e("acc", "", e);
            }
        }
        return FAILURE_UNKNOWN;
    }

    /**
     * Executes the given action. As a side effect, the gui model is updated.
     *
     * @param action The action to be executed.
     * @return Returns the outcome of the execution, e.g. success.
     */
    private ActionResult executeActionUnsafe(Action action) {

        IScreenState state;

        if (Properties.SURROGATE_MODEL()) {

            SurrogateModel surrogateModel = (SurrogateModel) guiModel;

            if (surrogateModel.isInPrediction()) {

                // check if the surrogate model can predict the action
                ActionResult actionResult = surrogateModel.predictAction(action);

                if (actionResult != null) {
                    surrogateModel.addPredictedAction(action);
                    lastScreenState = surrogateModel.getCurrentScreenState();
                    return actionResult;
                } else {
                    /*
                    * The surrogate model couldn't successfully predict the action, thus we need to
                    * return to the last check point and execute all cached actions.
                     */
                    surrogateModel.setInPrediction(false);
                    lastScreenState = surrogateModel.goToLastCheckPointState();
                    ActionResult result = executeCachedActions(surrogateModel.getPredictedActions());
                    surrogateModel.resetPredictedActions();
                    surrogateModel.setInPrediction(true);

                    // If a cached action closes the AUT, we abort the action execution here.
                    if(result != SUCCESS && result != null) {
                        return result;
                    }
                }
            }

            /*
            * Since the execution of cached actions may lead to a different screen state than
            * expected, the given action might be not applicable anymore. In such a case, we pick
            * a random action that is applicable on the current screen.
             */
            if(!getExecutableActions().contains(action)) {
                MATE.log_warn("Can't apply given action on current screen! Select random action.");
                action = Randomness.randomElement(getExecutableActions());
            }
        }

        try {
            deviceMgr.executeAction(action);
        } catch (AUTCrashException e) {

            MATE.log_acc("CRASH MESSAGE " + e.getMessage());
            /*
            * TODO: Evaluate whether pressing the home button makes sense, i.e. whether the gui
            *  model is updated correctly. By pressing home, we switch to the home screen but the
            *  crash dialog still appears. As a result, could it happen that actually two different
            *  crashes / crash dialogs are considered equal, because they appear on the same
            *  underlying home screen?
             */
            deviceMgr.pressHome();

            // update gui model
            state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
            state = toRecordedScreenState(state);

            if (Properties.SURROGATE_MODEL()) {
                SurrogateModel surrogateModel = (SurrogateModel) guiModel;
                Set<String> traces = deviceMgr.getTraces();
                surrogateModel.update(lastScreenState, state, action, FAILURE_APP_CRASH, traces);
            } else {
                guiModel.update(lastScreenState, state, action);
            }

            lastScreenState = state;

            return FAILURE_APP_CRASH;
        }

        state = clearScreen();

        // get the package name of the app currently running
        String currentPackageName = state.getPackageName();

        // if current package is null, emulator has crashed/closed
        if (currentPackageName == null) {
            MATE.log_acc("CURRENT PACKAGE: NULL");
            return FAILURE_EMULATOR_CRASH;
            // TODO: what to do when the emulator crashes?
        }

        ActionResult result;

        // check whether the package of the app currently running is from the app under test
        // if it is not, this causes a restart of the app
        if (!currentPackageName.equals(this.packageName)) {
            MATE.log("current package different from app package: " + currentPackageName);
            result = SUCCESS_OUTBOUND;
        } else {
            result = SUCCESS;
        }

        // update gui model
        state = toRecordedScreenState(state);

        if (Properties.SURROGATE_MODEL()) {
            SurrogateModel surrogateModel = (SurrogateModel) guiModel;
            Set<String> traces = deviceMgr.getTraces();
            surrogateModel.update(lastScreenState, state, action, result, traces);
        } else {
            guiModel.update(lastScreenState, state, action);
        }

        lastScreenState = state;

        return result;
    }

    /**
     * Executes the cached actions as long as an action doesn't leave the AUT.
     *
     * @param actions The list of actions to be executed, might be empty.
     * @return Returns the action result associated with the last executed action or {@code null}
     *          if no cached action was executed at all.
     */
    private ActionResult executeCachedActions(final List<Action> actions) {

        assert Properties.SURROGATE_MODEL();

        ActionResult result = null;

        for(Action action : actions) {
            result = executeActionUnsafe(action);
            if(result != SUCCESS) {
                return result;
            }
        }

        return result;
    }

    /**
     * Stores the given traces on the external storage. This needs to be called after each test case
     * and before a call to
     * {@link org.mate.utils.FitnessUtils#storeTestCaseChromosomeFitness(IChromosome)},
     * {@link org.mate.utils.FitnessUtils#storeTestSuiteChromosomeFitness(IChromosome, TestCase)},
     * {@link org.mate.utils.coverage.CoverageUtils#storeTestCaseChromosomeCoverage(IChromosome)} or
     * {@link org.mate.utils.coverage.CoverageUtils#storeTestSuiteChromosomeCoverage(IChromosome, TestCase)}.
     */
    public void storeTraces(Set<String> traces) {
        deviceMgr.storeTraces(traces);
    }

    /**
     * Returns the last recorded screen state.
     *
     * @return Returns the last recorded screen state.
     */
    public IScreenState getLastScreenState() {
        return lastScreenState;
    }

    /**
     * Clears the screen from all sorts of dialog, e.g. a permission dialog.
     *
     * @return Returns the current screen state.
     */
    public IScreenState clearScreen() {

        IScreenState screenState = null;
        boolean change = true;
        boolean retry = true;
        int retryCount = 0;

        // iterate over screen until no dialog appears anymore
        while (change || retry) {
            retry = false;
            change = false;
            try {

                screenState = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

                // check for presence of crash dialog
                if (handleCrashDialog()) {
                    change = true;
                    continue;
                }

                // check for presence of permission dialog
                if (handlePermissionDialog(screenState)) {
                    change = true;
                    continue;
                }

                // TODO: handle progress bar

                // check for presence of build warnings dialog
                if (handleBuildWarnings(screenState)) {
                    change = true;
                    continue;
                }

                // check for google sign in dialog
                if (handleGoogleSignInDialog(screenState)) {
                    change = true;
                    continue;
                }

            } catch (Exception e) {
                if (e instanceof IllegalStateException
                        && Objects.equals(e.getMessage(), UiAutomatorDisconnectedMessage)
                        && retryCount < UiAutomatorDisconnectedRetries) {
                    retry = true;
                    retryCount += 1;
                    continue;
                }
                Log.e("acc", "", e);
            }
        }
        return screenState;
    }

    /**
     * Checks whether the current screen shows a progress bar. If this is the case, we wait 10 seconds.
     * This may take several iterations until the progress bar is gone.
     *
     * @param screenState The current screen.
     * @return Returns {@code true} if the screen may change, otherwise {@code false} is returned.
     */
    @SuppressWarnings("unused")
    private boolean handleProgressBar(IScreenState screenState) {

        /*
        * FIXME: The progress bar is often misused as a rating bar, at least certain sub classes of it.
        *  Moreover, the progress bar is not reliably detected and we faced a real odd issue during
        *  experiments: the progress bar was stucking at 99% forever for the app de.tap.easy_xkcd.
         */

        // TODO: handle a progress dialog https://developer.android.com/reference/android/app/ProgressDialog

        if (deviceMgr.checkForProgressBar(screenState)) {
            MATE.log("Detected progress bar! Waiting...");
            Utils.sleep(10000);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether the current screen shows a 'Google SignIn' dialog. If this is the case,
     * we press the 'BACK' button as we can't login.
     *
     * @return Returns {@code true} if the screen may change, otherwise {@code false} is returned.
     */
    private boolean handleGoogleSignInDialog(IScreenState screenState) {

        if (screenState.getPackageName().equals("com.google.android.gms")) {
            MATE.log("Detected Google SignIn Dialog!");
            deviceMgr.pressBack();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether the current screen shows a crash dialog. If this is the case,
     * we press the 'HOME' button.
     *
     * @return Returns {@code true} if the screen may change, otherwise {@code false} is returned.
     */
    private boolean handleCrashDialog() {

        if (deviceMgr.checkForCrashDialog()) {
            MATE.log("Detected crash dialog!");
            // TODO: Should we really press 'HOME' or better click 'OK' on the dialog?
            deviceMgr.pressHome();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether the current screen shows a permission dialog. If this is the case,
     * the permission is tried to be granted by clicking on the 'allow button'.
     *
     * @param screenState The current screen.
     * @return Returns {@code true} if the screen may change, otherwise {@code false} is returned.
     */
    private boolean handlePermissionDialog(IScreenState screenState) {

        /*
         * The permission dialog has a different package name depending on the API level.
         * We currently support API level 25, 28 and 29.
         */

        // API 25, 28:
        if (screenState.getPackageName().equals("com.google.android.packageinstaller")
                || screenState.getPackageName().equals("com.android.packageinstaller")
                || screenState.getPackageName().startsWith("com.android.packageinstaller.permission")
                // API 29:
                || screenState.getPackageName().equals("com.android.permissioncontroller")
                || screenState.getPackageName().equals("com.google.android.permissioncontroller")) {

            MATE.log("Detected permission dialog!");

            for (WidgetAction action : screenState.getWidgetActions()) {

                Widget widget = action.getWidget();

                /*
                 * The resource id of the allow button may differ as well between different API levels.
                 */
                if (action.getActionType() == ActionType.CLICK
                        // API 25, 28:
                        && (widget.getResourceID()
                                .equals("com.android.packageinstaller:id/permission_allow_button")
                            // API: 29
                            || widget.getResourceID().equals(
                                    "com.android.permissioncontroller:id/permission_allow_button")
                            || widget.getResourceID().equals(
                                    "com.android.packageinstaller:id/continue_button")
                            || widget.getText().equalsIgnoreCase("continue")
                            // API 25, 28, 29:
                            || widget.getText().equalsIgnoreCase("allow"))) {
                    try {
                        deviceMgr.executeAction(action);
                        return true;
                    } catch (AUTCrashException e) {
                        MATE.log_warn("Couldn't click on permission dialog!");
                        throw new IllegalStateException(e);
                    }
                }
            }

            /*
            * In rare circumstances it can happen that the 'ALLOW' button is not discovered for yet
            * unknown reasons. The discovered widgets on the current screen point to the permission
            * dialog, but none of the buttons have the desired resource id. The only reasonable
            * option seems to re-fetch the screen state and hope that the problem is gone.
             */
            MATE.log_warn("Couldn't find any applicable action on permission dialog!");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether the current screen shows a build warnings dialog. If this is the case,
     * we try to click on the 'OK' button.
     *
     * @param screenState The current screen.
     * @return Returns {@code true} if the screen may change, otherwise {@code false} is returned.
     */
    private boolean handleBuildWarnings(IScreenState screenState) {

        for (Widget widget : screenState.getWidgets()) {
            if (widget.getText().equals("This app was built for an older version of Android " +
                    "and may not work properly. Try checking for updates, or contact the developer.")) {

                MATE.log("Detected build warnings dialog!");

                for (WidgetAction action : screenState.getWidgetActions()) {
                    if (action.getActionType() == ActionType.CLICK
                            && action.getWidget().getText().equals("OK")) {
                        try {
                            deviceMgr.executeAction(action);
                            return true;
                        } catch (AUTCrashException e) {
                            MATE.log_warn("Couldn't click on build warnings dialog!");
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the screen width.
     *
     * @return Returns the screen width in pixels.
     */
    public int getScreenWidth() {
        return deviceMgr.getScreenWidth();
    }

    /**
     * Returns the screen height.
     *
     * @return Returns the screen height in pixels.
     */
    public int getScreenHeight() {
        return deviceMgr.getScreenHeight();
    }

    /**
     * Resets an app, i.e. clearing the app cache and restarting the app.
     */
    public void resetApp() {

        if (Properties.SURROGATE_MODEL()) {
            // If the surrogate model was able to predict every action, we can avoid the reset.
            SurrogateModel surrogateModel = (SurrogateModel) guiModel;
            if (surrogateModel.hasPredictedLastTestCase()) {
                MATE.log("Skip reset!");
                // reset screen state
                lastScreenState = toRecordedScreenState(clearScreen());
                guiModel.addRootState(lastScreenState);
                surrogateModel.goToState(lastScreenState);
                return;
            }
        }

        try {
            deviceMgr.getDevice().wakeUp();
        } catch (RemoteException e) {
            MATE.log("Wake up couldn't be performed");
            e.printStackTrace();
        }

        if (!deviceMgr.isInPortraitMode()) {
            deviceMgr.setPortraitMode();
        }

        deviceMgr.reinstallApp();
        Utils.sleep(5000);
        deviceMgr.restartApp();
        Utils.sleep(2000);

        /*
        * Restarting the AUT may lead to a distinct start screen state. Thus, we keep track of all
        * possible root states.
         */
        lastScreenState = toRecordedScreenState(clearScreen());
        guiModel.addRootState(lastScreenState);

        if (Properties.SURROGATE_MODEL()) {
            // We need to move the FSM back in the correct state.
            SurrogateModel surrogateModel = (SurrogateModel) guiModel;
            surrogateModel.goToState(lastScreenState);
        }
    }

    /**
     * Restarts the app without clearing the app cache.
     */
    public void restartApp() {
        deviceMgr.restartApp();
        Utils.sleep(2000);

        /*
         * Restarting the AUT may lead to a distinct start screen state. Thus, we keep track of all
         * possible root states.
         */
        lastScreenState = toRecordedScreenState(clearScreen());
        guiModel.addRootState(lastScreenState);

        if (Properties.SURROGATE_MODEL()) {
            // We need to move the FSM back in the correct state.
            SurrogateModel surrogateModel = (SurrogateModel) guiModel;
            surrogateModel.goToState(lastScreenState);
        }
    }

    /**
     * Returns the edges (a pair of screen states) that is described by the given action.
     *
     * @param action The given action.
     * @return Returns the edges labeled by the given action.
     */
    public Set<Edge> getEdges(Action action) {
        return guiModel.getEdges(action);
    }

    /**
     * Checks whether the given screen state has been recorded earlier. If this is
     * the case, the recorded screen state is returned, otherwise the given state is returned.
     *
     * @param screenState The given screen state.
     * @return Returns the cached screen state, otherwise the given screen state.
     */
    private IScreenState toRecordedScreenState(IScreenState screenState) {
        Set<IScreenState> recordedScreenStates = guiModel.getStates();
        for (IScreenState recordedScreenState : recordedScreenStates) {
            if (recordedScreenState.equals(screenState)) {
                MATE.log_debug("Using cached screen state!");
                /*
                * NOTE: We should only return the cached screen state if we can ensure
                * that equals() actually compares the widgets. Otherwise, we can end up with
                * widget actions that are not applicable on the current screen.
                 */
                return recordedScreenState;
            }
        }

        String id = "S" + lastScreenStateNumber;

        screenState.setId(id);
        lastScreenStateNumber++;

        //Take a screenshot of the new screen state
        if ((Properties.CONVERT_GUI_TO_DOT() != DotConverter.Option.NONE)
                && Properties.DOT_WITH_SCREENSHOTS()) {
            DotConverter.takeScreenshot(id, lastScreenState.getPackageName());
        }

        return screenState;
    }

    /**
     * Checks whether the last action lead to a new screen state.
     *
     * @return Returns {@code} if a new screen state has been reached,
     *          otherwise {@code} false is returned.
     */
    public boolean reachedNewState() {
        return guiModel.reachedNewState();
    }

    /**
     * Retrieves the name of the currently visible activity.
     *
     * @return Returns the name of the currently visible activity.
     */
    public String getCurrentActivity() {
        // TODO: check whether we can use the cached activity -> getLastScreenState().getActivityName();
        return deviceMgr.getCurrentActivity();
    }

    /**
     * Returns the activities of the AUT.
     *
     * @return Returns the activities of the AUT.
     */
    public List<String> getActivities() {
        return activities;
    }

    /**
     * Retrieves the stack trace of the last discovered crash.
     *
     * @return Returns the stack trace of the last crash.
     */
    public StackTrace getLastCrashStackTrace() {
        return deviceMgr.getLastCrashStackTrace();
    }

    /**
     * Returns the current gui model.
     *
     * @return Returns the current gui model.
     */
    public IGUIModel getGuiModel() {
        return guiModel;
    }

    /**
     * Moves the AUT into the given screen state.
     *
     * @param screenState The given screen state.
     * @return Returns {@code true} if the transition to the screen state was successful, otherwise
     *          {@code false} is returned.
     */
    public boolean moveToState(final IScreenState screenState) {
        return guiWalker.goToState(screenState);
    }

    /**
     * Moves the AUT into the given screen state.
     *
     * @param screenStateId The screen state id.
     * @return Returns {@code true} if the transition to the screen state was successful, otherwise
     *          {@code false} is returned.
     */
    public boolean moveToState(String screenStateId) {
        return guiWalker.goToState(screenStateId);
    }

    /**
     * Launches the main activity of the AUT.
     *
     * @return Returns {@code true} if the transition to the screen state was successful, otherwise
     *          {@code false} is returned.
     */
    public boolean moveToMainActivity() {
        return guiWalker.goToMainActivity();
    }

    /**
     * Moves the AUT to the given activity.
     *
     * @param activity The activity that should be launched.
     * @return Returns {@code true} if the transition to the given activity was successful, otherwise
     *          {@code false} is returned.
     */
    public boolean moveToActivity(String activity) {
        return guiWalker.goToActivity(activity);
    }

    /**
     * Checks whether the AUT is currently opened.
     *
     * @return Returns {@code true} if the AUT is currently opened, otherwise {@code false} is returned.
     */
    public boolean isAppOpened() {
        return lastScreenState.getPackageName().equals(packageName);
    }
}
