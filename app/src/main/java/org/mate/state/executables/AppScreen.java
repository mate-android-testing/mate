package org.mate.state.executables;

import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.MATE;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.action.ui.Widget;
import org.mate.utils.UIAutomatorException;
import org.mate.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Models an app screen with all (discoverable) widgets on it.
 */
public class AppScreen {

    /**
     * Defines the activity name that corresponds to the app screen.
     */
    private final String activityName;

    /**
     * Defines the package name that corresponds to the app screen.
     */
    private final String packageName;

    /**
     * A list of discovered widgets on the app screen.
     */
    private final List<Widget> widgets;

    /**
     * Collects the text hints for editable fields over all app screens.
     * The key is defined through the resource id of the widget.
     */
    private static final Map<String, String> editTextHints = new Hashtable<String, String>();

    /**
     * Stores relevant information about the device, e.g. the display height.
     * Also enables the interaction with the device, e.g. perform a click on some ui element.
     */
    private final UiDevice device;

    /**
     * The waiting time for a stable screen state. If this time interval is chosen too short, we
     * may end up getting outdated information, e.g. a wrong package or activity name, and decide
     * to abort the current test case. Unfortunately there is no deterministic way to choose the
     * waiting time, but 200 ms seems to be the best compromise between accuracy and performance.
     */
    private static final int WAIT_FOR_STABLE_STATE = 200;

    /**
     * Creates a new app screen containing the widgets on it.
     */
    public AppScreen(DeviceMgr deviceMgr) {

        Utils.sleep(WAIT_FOR_STABLE_STATE);

        this.device = deviceMgr.getDevice();
        this.widgets = new ArrayList<>();
        this.activityName = deviceMgr.getCurrentActivity();
        this.packageName = getCurrentPackageName();

        if (packageName == null) {
            throw new UIAutomatorException("UIAutomator disconnected, couldn't retrieve package name!");
        }

        final AccessibilityNodeInfo rootNode = getRootNode();

        if (rootNode == null) {
            throw new UIAutomatorException("UIAutomator disconnected, couldn't retrieve app screen!");
        }

        // retrieve widgets from current screen
        MATE.log_debug("AppScreen: " + activityName);
        try {
            parseWidgets(rootNode, null, 0, 0, 0);
        } catch (Exception e) {
            MATE.log_debug("Couldn't parse widgets: " + e.getMessage());
            e.printStackTrace();
            throw new UIAutomatorException("UIAutomator disconnected, couldn't parse widgets!", e);
        }
        MATE.log_debug("Number of widgets: " + widgets.size());
    }

    /**
     * Retrieves the root node in the ui hierarchy via the {@link android.app.UiAutomation}.
     *
     * @return Returns the root node in the ui hierarchy.
     */
    private AccessibilityNodeInfo getRootNode() {

        AccessibilityNodeInfo rootNode = null;

        try {
            rootNode = InstrumentationRegistry.getInstrumentation()
                    .getUiAutomation().getRootInActiveWindow();

            /*
            * A certain combination of actions that bring the AUT in a state where uiautomator
            * fetches a ui hierarchy (window) consisting of a single widget and the actual window
            * is labeled as inactive at that moment. Although we could access the widgets of the
            * inactive window as well, performing an action on them fails without first introducing
            * some dummy action like clicking in the middle of the screen. It seems like we need to
            * bring back the focus to the correct window by injecting some dummy action.
             */
            if (rootNode.getChildCount() == 0) {
                MATE.log_warn("Fetched wrong active window.");

                device.click(device.getDisplayWidth() / 2, device.getDisplayHeight() / 2);

                rootNode = InstrumentationRegistry.getInstrumentation()
                        .getUiAutomation().getRootInActiveWindow();

                if (rootNode.getChildCount() == 0) {
                    MATE.log_warn("Couldn't fetch correct root node!");
                }
            }

        } catch (Exception e) {
            MATE.log_debug("Couldn't retrieve root node: " + e.getMessage());
            e.printStackTrace();
        }

        return rootNode;
    }

    /**
     * Retrieves the current package name via the {@link UiDevice}.
     *
     * @return Returns the current package name associated with the app screen.
     */
    private String getCurrentPackageName() {

        String packageName = null;

        try {
            packageName = device.getCurrentPackageName();
        } catch (Exception e) {
            MATE.log_debug("Couldn't retrieve package name: " + e.getMessage());
            e.printStackTrace();
        }

        // NOTE: We could also retrieve the package name via the root node!
        return packageName;
    }

    /**
     * Extracts the widgets from the ui hierarchy.
     *
     * @param node Describes a node in the ui hierarchy. Initially, the root node.
     * @param parent The parent widget, {@code null} for the root node.
     * @param depth The depth of the node in the ui hierarchy (tree).
     * @param globalIndex A global index based on DFS order.
     * @param localIndex A local index for each child widget, i.e. the child number.
     * @return Returns the current global index.
     */
    private int parseWidgets(final AccessibilityNodeInfo node, Widget parent, int depth,
                             int globalIndex, final int localIndex) {

        Widget widget = new Widget(parent, node, activityName, depth, globalIndex, localIndex);
        widgets.add(widget);

        if (widget.isEditable() && widget.isVisible()) {
            checkForHint(node, widget);
        }

        if (parent != null) {
            parent.addChild(widget);
        }

        depth++;
        globalIndex++;

        // traverse children
        for (int i = 0; i < node.getChildCount(); i++) {
            // the local index is simply the child number
            if (node.getChild(i) == null) {
                MATE.log_warn("Child node " + i + " at depth " + depth + " not available!");
            } else {
                globalIndex = parseWidgets(node.getChild(i), widget, depth, globalIndex, i);
            }
        }
        return globalIndex;
    }

    /**
     * Checks whether an editable widget displays some hint.
     *
     * @param node A node in the ui hierarchy.
     * @param widget The widget corresponding to the node.
     */
    private void checkForHint(AccessibilityNodeInfo node, Widget widget) {

        String hint = editTextHints.get(widget.getResourceID());

        if (hint == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (node.isShowingHintText()) {
                    hint = node.getHintText().toString(); 
                }
            } else {
                // fallback mechanism for older devices
                hint = getHintAPI25AndLower(widget);
            }

            // save hint for subsequent requests
            if (hint != null) {
                if (!widget.getResourceID().isEmpty()) {
                    editTextHints.put(widget.getResourceID(), hint);
                }
            }
        }

        hint = Objects.toString(hint, "");
        widget.setHint(hint);
    }

    /**
     * Retrieves the hint of an input field for an emulator running an API level <= 25.
     *
     * @param widget The widget representing the input field.
     * @return Returns the hint contained in the input field.
     */
    private String getHintAPI25AndLower(Widget widget) {

        String hint = null;
        UiObject2 uiObject;

        if (widget.getResourceID().isEmpty()) {
            uiObject = device.findObject(By.text(widget.getText()));
        } else {
            uiObject = device.findObject(By.res(widget.getResourceID()));
        }

        if (uiObject != null) {

            /*
             * In order to retrieve the hint of a widget, we have to clear the
             * input, and this in turn should display the hint if we are lucky.
             */
            try {
                // save original input
                String textBeforeClear = Objects.toString(uiObject.getText(), "");

                // reset input and hope that this causes a hint to be set
                uiObject.setText("");
                String textAfterClear = Objects.toString(uiObject.getText(), "");

                // restore original input
                uiObject.setText(textBeforeClear);

                hint = textAfterClear;
            } catch (StaleObjectException e) {
                MATE.log_warn("Stale UiObject2!");
                e.printStackTrace();
                MATE.log_warn("Couldn't derive hint for widget: " + widget);
            }
        }

        return hint;
    }

    /**
     * Returns the activity name that app screen corresponds to.
     *
     * @return Returns the activity name.
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * Returns the widgets linked to the app screen.
     *
     * @return Returns the widgets of the app screen.
     */
    public List<Widget> getWidgets() {
        return Collections.unmodifiableList(widgets);
    }

    /**
     * Returns the package name the app screens corresponds to.
     *
     * @return Returns the package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the bounding box of the status bar. This depends on display width
     * and has a fixed height of 72 pixels, e.g. [0,0][1080][72].
     *
     * @return Returns the bounding box of the status bar.
     */
    public Rect getStatusBarBoundingBox() {
        return new Rect(0, 0, getWidth(), 72);
    }

    /**
     * Returns the bounding box of the menu bar. This depends on display width
     * and has a fixed height of 240 - 72 pixels, e.g. [0,72][1080][240].
     *
     * @return Returns the bounding box of the status bar.
     */
    public Rect getMenuBarBoundingBox() {
        return new Rect(0, 72, getWidth(), 240);
    }

    /**
     * Returns the bounding box of the app screen. This depends on display width
     * and the display height, e.g. [0,0][1080][1920].
     *
     * @return Returns the bounding box of the app screen.
     */
    @SuppressWarnings("unused")
    public Rect getBoundingBox() {
        return new Rect(0, 0, getWidth(), getHeight());
    }

    /**
     * Returns the screen width.
     *
     * @return Returns the screen width in pixels.
     */
    public int getWidth() {
        return device.getDisplayWidth();
    }

    /**
     * Returns the screen height.
     *
     * @return Returns the screen height in pixels.
     */
    public int getHeight() {
        return device.getDisplayHeight();
    }

    /**
     * Compares two app screens for equality.
     *
     * @param o The other app screen to compare against.
     * @return Returns {@code true} if the two app screens are identical,
     *         otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            AppScreen appScreen = (AppScreen) o;
            return Objects.equals(activityName, appScreen.activityName) &&
                    Objects.equals(packageName, appScreen.packageName) &&
                    Objects.equals(widgets, appScreen.widgets);
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the app screen.
     */
    @Override
    public int hashCode() {
        return Objects.hash(activityName, packageName, widgets);
    }

    /**
     * A simple textual representation of the app screen.
     *
     * @return Returns the string representation of the app screen.
     */
    @NonNull
    @Override
    public String toString() {
        return "AppScreen{activity: " + activityName + ", widgets: " + widgets.size() + "}";
    }
}
