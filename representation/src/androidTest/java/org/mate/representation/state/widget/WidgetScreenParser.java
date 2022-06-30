package org.mate.representation.state.widget;

import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Utils;
import org.mate.representation.DeviceInfo;
import org.mate.representation.ExplorationInfo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

/**
 * Auxiliary class to parse the widgets in the current screen.
 */
public class WidgetScreenParser {

    /**
     * A list of discovered widgets on the app screen.
     */
    private final List<Widget> widgets;

    /**
     * Defines the activity name that corresponds to the app screen.
     */
    private final String activityName;

    /**
     * Collects the text hints for editable fields over all app screens.
     * The key is defined through the resource id of the widget.
     */
    private static final Map<String, String> editTextHints = new Hashtable<String, String>();

    /**
     * The UiDevice provided by the DeviceInfo class.
     */
    private final UiDevice device;

    public WidgetScreenParser() {
        this.widgets = new ArrayList<>();
        this.activityName = ExplorationInfo.getInstance().getCurrentActivityName();
        this.device = DeviceInfo.getInstance().getUiDevice();
        parseWidgets();
    }

    /**
     * @return the widgets parsed.
     */
    public List<Widget> getWidgets() {
        return widgets;
    }

    /**
     * Parse the widgets in the current screen.
     * This is done by inspecting the UI hierarchy from the root view in the active window.
     */
    public void parseWidgets() {

        AccessibilityNodeInfo rootNode = getRootNode();

        if (rootNode == null) {
            throw new IllegalStateException("UIAutomator disconnected, couldn't retrieve app screen!");
        }

        parseWidgets(rootNode, null, 0, 0, 0);
    }

    /**
     * Retrieves the root node in the ui hierarchy via the {@link android.app.UiAutomation}.
     *
     * @return Returns the root node in the ui hierarchy.
     */
    private AccessibilityNodeInfo getRootNode() {

        AccessibilityNodeInfo rootNode = getInstrumentation().getUiAutomation().getRootInActiveWindow();

        for (int numberOfRetries = 0; numberOfRetries < ExplorationInfo.UiAutomatorDisconnectedRetries
                && rootNode == null; numberOfRetries++) {

            /*
             * It can happen that the device is in an unstable state and hence the UIAutomator
             * connection may get lost. In this case, we should wait some time until we try to
             * re-connect.
             */
            MATELog.log_acc("UIAutomator disconnected, try re-connecting!");
            Utils.sleep(3000);

            // try to reconnect
            rootNode = getInstrumentation().getUiAutomation().getRootInActiveWindow();
        }

        return rootNode;
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

        MATELog.log_debug("Node: " + node.getViewIdResourceName() + ", depth: " + depth
                + ", globalIndex: " + globalIndex + ", localIndex: " + localIndex);
        MATELog.log_debug("Node class: " + node.getClassName());

        Widget widget = new Widget(parent, node, activityName, depth, globalIndex, localIndex, ExplorationInfo.getInstance().getStateEquivalenceLevel());
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
                MATELog.log_warn("Child node " + i + " at depth " + depth + " not available!");
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
                    if(node.getHintText() instanceof String){
                        hint = (String) node.getHintText();
                    }
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
}
