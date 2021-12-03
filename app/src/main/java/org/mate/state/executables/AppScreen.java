package org.mate.state.executables;

import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.MATE;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.EnvironmentManager;
import org.mate.interaction.action.ui.Widget;
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
     * Creates a new app screen containing the widgets on it.
     */
    public AppScreen(DeviceMgr deviceMgr) {

        this.device = deviceMgr.getDevice();

        this.widgets = new ArrayList<>();
        this.activityName = deviceMgr.getCurrentActivity();

        if (activityName.equals(EnvironmentManager.ACTIVITY_UNKNOWN)) {
            this.packageName = device.getCurrentPackageName();
        } else {
            this.packageName = activityName.split("/")[0];
        }

        AccessibilityNodeInfo rootNode = InstrumentationRegistry.getInstrumentation()
                .getUiAutomation().getRootInActiveWindow();

        if (rootNode == null) {

            /*
             * It can happen that the device is in an unstable state and hence the UIAutomator
             * connection may get lost. In this case, we should wait some time until we try to
             * re-connect.
             */
            MATE.log_acc("UIAutomator disconnected, try re-connecting!");
            Utils.sleep(1000);

            // try to reconnect
            rootNode = InstrumentationRegistry.getInstrumentation()
                    .getUiAutomation().getRootInActiveWindow();

            if (rootNode == null) {
                throw new IllegalStateException("UIAutomator disconnected, couldn't retrieve app screen!");
            }
        }

        // retrieve widgets from current screen
        MATE.log_debug("AppScreen: " + activityName);
        parseWidgets(rootNode, null, 0, 0, 0);
        MATE.log_debug("Number of widgets: " + widgets.size());
    }

    /**
     * Extracts the widgets from the ui hierarchy.
     *
     * @param node        Describes a node in the ui hierarchy. Initially, the root node.
     * @param parent      The parent widget, {@code null} for the root node.
     * @param depth       The depth of the node in the ui hierarchy (tree).
     * @param globalIndex A global index based on DFS order.
     * @param localIndex  A local index for each child widget, i.e. the child number.
     * @return Returns the current global index.
     */
    private int parseWidgets(final AccessibilityNodeInfo node, Widget parent, int depth,
                             int globalIndex, final int localIndex) {

        MATE.log_debug("Node: " + node.getViewIdResourceName() + ", depth: " + depth
                + ", globalIndex: " + globalIndex + ", localIndex: " + localIndex);
        MATE.log_debug("Node class: " + node.getClassName());

        Widget widget = new Widget(parent, node, activityName, depth, globalIndex, localIndex);
        widgets.add(widget);

        if (widget.isEditable()) {
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
     * @param node   A node in the ui hierarchy.
     * @param widget The widget corresponding to the node.
     */
    private void checkForHint(AccessibilityNodeInfo node, Widget widget) {

        String hint = editTextHints.get(widget.getResourceID());

        if (hint == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (node.isShowingHintText()) {
                    hint = (String) node.getHintText();
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

            // save original input
            String textBeforeClear = Objects.toString(uiObject.getText(), "");

            // reset input and hope that this causes a hint to be set
            uiObject.setText("");
            String textAfterClear = Objects.toString(uiObject.getText(), "");

            // restore original input
            uiObject.setText(textBeforeClear);

            hint = textAfterClear;
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
     * otherwise {@code false} is returned.
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
