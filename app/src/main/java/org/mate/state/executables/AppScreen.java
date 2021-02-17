package org.mate.state.executables;

import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.interaction.EnvironmentManager;
import org.mate.interaction.action.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Models an app screen with all (discoverable) widgets on it.
 */
public class AppScreen {

    /**
     * Defines the activity name that corresponds to the app screen.
     */
    private String activityName;

    /**
     * Defines the package name that corresponds to the app screen.
     */
    private String packageName;

    /**
     * A list of discovered widgets on the app screen.
     */
    private List<Widget> widgets;

    /**
     * Collects the text hints for editable fields over all app screens.
     */
    private static Map<String, String> editTextHints = new Hashtable<String,String>();

    /**
     * Stores relevant information about the device, e.g. the display height.
     * Also enables the interaction with the device, e.g. perform a click on some ui element.
     */
    private UiDevice device;

    /**
     * Determines whether a scroll action is necessary to see ANY widget.
     * This flags control whether swipe actions are added to the list of executable actions.
     */
    private boolean hasToScrollUp;
    private boolean hasToScrollDown;
    private boolean hasToScrollLeft;
    private boolean hasToScrollRight;

    /**
     * Models a ui element in the ui hierarchy. This can be a layout or a widget like a button
     * for instance. Check the 'uiautomatorviewer' tool coming with the Android-SDK for a
     * concrete instance of a such a ui element.
     */
    private AccessibilityNodeInfo rootNodeInfo;

    /**
     * Creates a new app screen containing the widgets on it.
     */
    public AppScreen() {

        Instrumentation instrumentation = getInstrumentation();
        this.device = UiDevice.getInstance(instrumentation);

        this.widgets = new ArrayList<>();
        this.activityName = Registry.getEnvironmentManager().getCurrentActivityName();

        if (activityName.equals(EnvironmentManager.ACTIVITY_UNKNOWN)) {
            this.packageName = device.getCurrentPackageName();
        } else {
            this.packageName = activityName.split("/")[0];
        }

        AccessibilityNodeInfo nodeInfo = InstrumentationRegistry.getInstrumentation()
                .getUiAutomation().getRootInActiveWindow();

        if (nodeInfo == null) {
            /*
            * TODO: Check whether this is the expected behaviour. I would rather assume that
            *  the UIAutomator throws an exception and we can't react it properly, similar
            *  to what can happen when executing an action in the DeviceMgr.
             */
            MATE.log_acc("UIAutomator disconnected, try re-connecting!");
            
            // try to reconnect
            nodeInfo = InstrumentationRegistry.getInstrumentation()
                    .getUiAutomation().getRootInActiveWindow();

            if (nodeInfo == null) {
                throw new IllegalStateException("UIAutomator disconnected, couldn't retrieve app screen!");
            }
        }

        // retrieve widgets from current screen
        this.rootNodeInfo = nodeInfo;
        readNodes(nodeInfo,null);
    }

    /**
     * Recursively retrieves the widgets from the current screen.
     *
     * @param node A node in the ui hierarchy. Describes basically a ui element.
     * @param parent The parent widget, {@code null} for the root node.
     */
    private void readNodes(AccessibilityNodeInfo node, Widget parent) {

        Widget widget = null;

        if (node != null) {

            try {
                widget = createWidget(node, parent, activityName);
            } catch(StaleObjectException e) {
                MATE.log_warn("Couldn't extract widget!");
                e.printStackTrace();
            }

            if (widget != null) {
                widgets.add(widget);
            }

            // retrieve the child widgets
            for (int i = 0; i < node.getChildCount(); i++)
                readNodes(node.getChild(i), widget);
        }
    }

    /**
     * Extracts the widget described by the given node (ui element).
     *
     * @param node Describes a node in the ui hierarchy.
     * @param parent The parent widget, {@code null} for the root node.
     * @param activityName The activity name that corresponds to the app screen.
     * @return Returns the extracted widget or {@code null} if the widget is outside of visibility.
     */
    private Widget createWidget(AccessibilityNodeInfo node, Widget parent, String activityName) {

        String parentResourceId = getValidResourceIdFromTree(parent);
        String id = Objects.toString(node.getViewIdResourceName(), "");
        String clazz = Objects.toString(node.getClassName(), "");
        String text = Objects.toString(node.getText(), "");

        if (id.isEmpty()) {
            if (parent != null && parentResourceId != null) {
                id = parentResourceId + "-child-" + parent.getChildren().size() + "#" + clazz;
            } else {
                id = clazz + "-" + text;
            }
        }

        String idByActivity = activityName + "_" + id;

        MATE.log_debug("Widget idByActivity: " + idByActivity);
        MATE.log_debug("Widget clazz: " + clazz);
        MATE.log_debug("Widget id: " + id);
        Widget widget = new Widget(id, clazz, idByActivity);

        String res = Objects.toString(node.getViewIdResourceName(), "");
        widget.setResourceID(res);
        MATE.log_debug("Widget resource name: " + res);

        widget.setParent(parent);
        widget.setText(text);
        widget.setEnabled(node.isEnabled());

        String widgetPackageName = Objects.toString(node.getPackageName(), packageName);
        widget.setPackageName(widgetPackageName);

        // define the widget border
        Rect rec = new Rect();
        node.getBoundsInScreen(rec);
        widget.setBounds(rec);
        MATE.log_debug("Widget boundaries: " + rec.toShortString());

        /*
        * The following checks verify whether the widget is outside of visibility.
        * Note that the point (0,0) is the left top corner. If so, the widget is
        * ignored.
         */
        if (widget.getX1() < 0 || widget.getX2() < 0) {
            MATE.log_debug("Widget outside of visibility: " + widget.getBounds().toShortString());
            this.hasToScrollLeft = true;
            return null;
        }

        if (widget.getX1() > device.getDisplayWidth() || widget.getX2() > device.getDisplayWidth()) {
            MATE.log_debug("Widget outside of visibility: " + widget.getBounds().toShortString());
            this.hasToScrollRight = true;
            return null;
        }

        if (widget.getY1() < 0 || widget.getY2() < 0) {
            MATE.log_debug("Widget outside of visibility: " + widget.getBounds().toShortString());
            this.hasToScrollUp = true;
            return null;
        }

        if (widget.getY1() > device.getDisplayHeight() || widget.getY2() > device.getDisplayHeight()) {
            MATE.log_debug("Widget outside of visibility: " + widget.getBounds().toShortString());
            this.hasToScrollDown = true;
            return null;
        }

        // FIXME: it seems like there is no way to retrieve the widget index from the node object
        widget.setIndex(0);

        widget.setCheckable(node.isCheckable());
        widget.setChecked(node.isChecked());
        widget.setClickable(node.isClickable());
        widget.setFocusable(node.isFocusable());
        widget.setHasChildren(node.getChildCount() != 0);
        widget.setLongClickable(node.isLongClickable());
        widget.setPassword(false);
        widget.setScrollable(node.isScrollable());
        widget.setSelected(node.isSelected());
        widget.setMaxLength(node.getMaxTextLength());
        widget.setInputType(node.getInputType());
        widget.setVisibleToUser(node.isVisibleToUser());
        widget.setAccessibilityFocused(node.isAccessibilityFocused());

        String contentDesc = Objects.toString(node.getContentDescription(), "");
        widget.setContentDesc(contentDesc);

        String textError = Objects.toString(node.getError(), "");
        widget.setErrorText(textError);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            widget.setImportantForAccessibility(node.isImportantForAccessibility());
        } else {
            widget.setImportantForAccessibility(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            widget.setScreenReaderFocusable(node.isScreenReaderFocusable());
        } else {
            widget.setScreenReaderFocusable(true);
        }

        AccessibilityNodeInfo.CollectionItemInfo cinfo = node.getCollectionItemInfo();
        if (cinfo != null) {
            widget.setHeading(cinfo.isHeading());
        } else {
            widget.setHeading(false);
        }

        AccessibilityNodeInfo lf = node.getLabelFor();
        String labelFor = "";
        if (lf != null) {
            labelFor = Objects.toString(lf.getViewIdResourceName(), "");
        }
        widget.setLabelFor(labelFor);

        AccessibilityNodeInfo lb = node.getLabeledBy();
        String labelBy = "";
        if (lb != null) {
            labelBy = Objects.toString(lb.getViewIdResourceName(), "");
        }
        widget.setLabeledBy(labelBy);

        /*
        * TODO: We should replace the following code with node.isShowingHintText()
        *  and node.getHintText() in the future, however this requires min API level 26.
         */
        // try to retrieve the hint of a widget
        if (widget.isEditable()) {

            String hint = editTextHints.get(id);

            if (hint == null) {

                UiObject2 uiObject = null;

                if (widget.getResourceID().isEmpty()) {
                    uiObject = device.findObject(By.text(widget.getText()));
                } else {
                    uiObject = device.findObject(By.res(id));
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

                    if (!widget.getResourceID().isEmpty()) {
                        editTextHints.put(id, hint);
                    }

                    // TODO: I don't understand this check.
                    if (textAfterClear.equals(textBeforeClear)) {
                        uiObject.setText("");
                    }
                }
            }

            hint = Objects.toString(hint, "");
            widget.setHint(hint);
            widget.setContentDesc(hint);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                widget.setShowingHintText(node.isShowingHintText());
            }
        }

        widget.setFocused(node.isFocused());

        if (parent != null)
            parent.addChild(widget);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            widget.setContextClickable(node.isContextClickable());
        }

        return widget;
    }

    /**
     * Returns the resource id of the given widget.
     *
     * @param widget The widget for which the resource id should be derived.
     * @return Returns the resource id of the widget or {@code null} if not available.
     */
    private String getValidResourceIdFromTree(Widget widget) {

        String resourceId = null;

        // look up in ui hierarchy for resource id
        while (widget != null && resourceId == null) {
            if (widget.getResourceID() != null && !widget.getResourceID().equals(""))
                resourceId = widget.getResourceID();
            else
                widget = widget.getParent();
        }
        return resourceId;
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
    public List<Widget> getWidgets(){
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
     * Whether scrolling up (wipe) is necessary to reach some widget on the app screen.
     *
     * @return Returns {@code true} if some widget is outside of visibility,
     *          otherwise {@code false} is returned.
     */
    public boolean isHasToScrollUp() {
        return hasToScrollUp;
    }

    /**
     * Whether scrolling down (wipe) is necessary to reach some widget on the app screen.
     *
     * @return Returns {@code true} if some widget is outside of visibility,
     *          otherwise {@code false} is returned.
     */
    public boolean isHasToScrollDown() {
        return hasToScrollDown;
    }

    /**
     * Whether scrolling left (wipe) is necessary to reach some widget on the app screen.
     *
     * @return Returns {@code true} if some widget is outside of visibility,
     *          otherwise {@code false} is returned.
     */
    public boolean isHasToScrollLeft() {
        return hasToScrollLeft;
    }

    /**
     * Whether scrolling right (wipe) is necessary to reach some widget on the app screen.
     *
     * @return Returns {@code true} if some widget is outside of visibility,
     *          otherwise {@code false} is returned.
     */
    public boolean isHasToScrollRight() {
        return hasToScrollRight;
    }

    /**
     * Compares two app screens for equality.
     *
     * @param o The other app screen to compare against.
     * @return Returns {@code true} if the two app screens are identical,
     *          otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            AppScreen appScreen = (AppScreen) o;
            // TODO: check whether comparing those boolean flags is desired
            return hasToScrollUp == appScreen.hasToScrollUp &&
                    hasToScrollDown == appScreen.hasToScrollDown &&
                    hasToScrollLeft == appScreen.hasToScrollLeft &&
                    hasToScrollRight == appScreen.hasToScrollRight &&
                    Objects.equals(activityName, appScreen.activityName) &&
                    Objects.equals(packageName, appScreen.packageName) &&
                    Objects.equals(widgets, appScreen.widgets);
        }
    }

    /**
     * Returns the hash code of this app screen.
     *
     * @return Returns the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(activityName, packageName, widgets, hasToScrollUp,
                hasToScrollDown, hasToScrollLeft, hasToScrollRight);
    }
}
