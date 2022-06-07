package org.mate.representation.state.espresso;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Looper;
import android.util.Pair;
import android.view.View;

import androidx.test.espresso.Root;
import androidx.test.espresso.base.ActiveRootLister;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.utils.MATELog;
import org.mate.representation.DeviceInfo;
import org.mate.representation.ExplorationInfo;
import org.mate.commons.interaction.action.espresso.matchers_combination.EspressoViewMatcherBuilder;
import org.mate.commons.interaction.action.espresso.matchers_combination.EspressoViewMatcherCombination;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.matcher.RootMatchers.isDialog;

/**
 * Auxiliary class to parse the Espresso actions in the current screen.
 */
public class EspressoScreenParser {

    /**
     * A list of discovered EspressoActions on the app screen.
     */
    private final List<EspressoAction> espressoActions;

    /**
     * The Instrumentation provided by the DeviceInfo class.
     */
    private final Instrumentation instrumentation;

    public EspressoScreenParser() {
        espressoActions = new ArrayList<>();
        instrumentation = DeviceInfo.getInstance().getInstrumentation();

        EspressoViewTree viewTree = fetchViewTree();
        loadEspressoActions(viewTree);
    }

    public List<EspressoAction> getActions() {
        return espressoActions;
    }

    /**
     * Parse actions of all Views on the screen.
     * If a View does not have a unique matcher combination, it is skipped.
     * @param viewTree
     */
    private void loadEspressoActions(EspressoViewTree viewTree) {
        long startTime = System.nanoTime();

        for (EspressoViewTreeNode node : viewTree.getAllNodes()) {
            EspressoViewActionsParser viewActionsParser =
                    new EspressoViewActionsParser(node.getEspressoView().getView());
            List<EspressoViewAction> espressoViewActions = viewActionsParser.parse();

            if (espressoViewActions.size() == 0) {
                // nothing to do on this view, skip view matcher generation
                continue;
            }

            EspressoViewMatcherCombination matcherCombination = EspressoViewMatcherCombination.buildUniqueCombination(node, viewTree);

            if (matcherCombination == null) {
                // we weren't able to generate a unique matcher combination for this view, skip it.
                continue;
            }

            EspressoViewMatcher espressoViewMatcher = EspressoViewMatcherBuilder
                    .addMatcherCombination(matcherCombination)
                    .build();

            // Create and save the EspressoAction instances
            for (EspressoViewAction espressoViewAction : espressoViewActions) {
                EspressoAction espressoAction = new EspressoAction(
                        espressoViewAction,
                        espressoViewMatcher);
                espressoActions.add(espressoAction);
            }
        }


        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get ms.
        MATELog.log_debug(String.format("Loading of Espresso actions took %d ms", duration));
    }

    /**
     * Fetch all views in current screen.
     */
    private EspressoViewTree fetchViewTree() {
        Pair<View, Activity> result = getRootView();
        View rootView = result.first;

        if (rootView == null) {
            MATELog.log_error("Unable to find root view on a resumed activity to get available " +
                    "Espresso actions");
            return new EspressoViewTree();
        }

        return new EspressoViewTree(rootView);
    }

    /**
     * Fetches the root View of current activity in Resumed state.
     *
     * @return
     */
    private Pair<View, Activity> getRootView() {
        final View[] rootView = {null};
        final Activity[] resumedActivity = {null};

        instrumentation.runOnMainSync(() -> {
            ArrayList<Activity> resumedActivities = new ArrayList<>(
                    ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED));

            if (resumedActivities.size() == 0) {
                // No activity is found in resumed state, we probably left the AUT.
                return;
            }

            Activity activity = resumedActivities.get(0);

            if (!ExplorationInfo.getInstance().getTargetPackageName().equals(
                    activity.getPackageName())) {
                // The resumed activity is for a different package name than the one we are
                // targeting. Exit this function as if we haven't found a root view.
                return;
            }

            resumedActivity[0] = activity;

            List<Root> roots;
            try {
                Class rootsOracleClass = Class.forName("androidx.test.espresso.base.RootsOracle");
                Constructor<ActiveRootLister> rootsOracleConstructor = rootsOracleClass.getDeclaredConstructor(new Class[]{Looper.class});
                rootsOracleConstructor.setAccessible(true);
                ActiveRootLister sRootsOracle = rootsOracleConstructor.newInstance(Looper.getMainLooper());

                Method sRootsOracle_listActiveRoots = rootsOracleClass.getMethod("listActiveRoots");
                roots = (List) sRootsOracle_listActiveRoots.invoke(sRootsOracle);
            } catch (Exception e) {
                MATELog.log_error("A problem occurred listing roots: " + e.getMessage());
                throw new RuntimeException("A problem occurred listing roots", e);
            }

            if (roots != null && roots.size() > 0) {
                rootView[0] = getRootFromMultipleRoots(roots).getDecorView();
            }
        });

        return new Pair<>(rootView[0], resumedActivity[0]);
    }

    /**
     * Method taken from the Android test open source project.
     * URL: https://github.com/android/android-test/blob/eaddf2ba037e79ac13a2e70c716773ed4c7e3cf2/espresso/core/java/androidx/test/espresso/base/RootViewPicker.java
     *
     * <p>
     * If there are multiple roots, pick one root window to interact with. By default we try to
     * select the top most window, except for dialogs were we return the dialog window.
     *
     * <p>Multiple roots only occur:
     *
     * <ul>
     *   <li>When multiple activities are in some state of their lifecycle in the application. We
     *       don't care about this, since we only want to interact with the RESUMED activity, all
     *       other Activities windows are not visible to the user so, out of scope.
     *   <li>When a {@link android.widget.PopupWindow} or {@link
     *       android.support.v7.widget.PopupMenu} is used. This is a case where we definitely want
     *       to consider the top most window, since it probably has the most useful info in it.
     *   <li>When an {@link android.app.Dialog} is shown. Again, this is getting all the users
     *       attention, so it gets the test attention too.
     * </ul>
     *
     * @param roots
     */
    private Root getRootFromMultipleRoots(List<Root> roots) {
        Root topMostRoot = roots.get(0);
        // String packageName = topMostRoot.getWindowLayoutParams().get().packageName;
        if (roots.size() > 1) {
            for (Root currentRoot : roots) {
                if (isDialog().matches(currentRoot)) {
                    return currentRoot;
                }
                if (isTopmostRoot(topMostRoot, currentRoot)) {
                    topMostRoot = currentRoot;
                }
            }
        }
        return topMostRoot;
    }

    /**
     * Returns a boolean indicating whether a newly found root is in a higher position than the
     * top-most root found so far.
     *
     * @param topMostRoot
     * @param newRoot
     * @return
     */
    private boolean isTopmostRoot(Root topMostRoot, Root newRoot) {
        return newRoot.getWindowLayoutParams().get().type
                > topMostRoot.getWindowLayoutParams().get().type;
    }
}
