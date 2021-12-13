package org.mate.interaction.action.ui;


import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.MATE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an element in the ui hierarchy, i.e. a wrapper around a {@link AccessibilityNodeInfo}
 * object. Defines a parent, child and sibling relation.
 */
public class Widget {

    /**
     * A reference to the parent widget or {@code null} if it is the root widget.
     */
    private final Widget parent;

    /**
     * A list of direct descendants.
     */
    private final List<Widget> children;

    /**
     * A unique identifier for this widget, which is based on the activity name, the global
     * and local index. This id is unique within the same ui hierarchy / screen.
     */
    private final String id;

    /**
     * The widget class name, e.g. android.view.ListView, or the empty string.
     * See {@link AccessibilityNodeInfo#getClassName()}.
     */
    private final String clazz;

    /**
     * The resource id of the widget or the empty string.
     * See {@link AccessibilityNodeInfo#getViewIdResourceName()}.
     */
    private final String resourceID;

    /**
     * A unique index within the ui hierarchy. This index is based on the order of BFS.
     */
    private final int index;

    /**
     * The index of the widget in the ui hierarchy. This conforms
     * to the index of the 'uiautomatorviewer' tool. This is solely
     * a local index for each widget's children, i.e. the children
     * of a widget are labeled starting from 0.
     */
    private final int localIndex;

    /**
     * The depth of the widget in the ui hierarchy.
     */
    private final int depth;

    /**
     * The activity name.
     */
    private final String activity;

    /**
     * The package name the widget is referring to.
     * See {@link AccessibilityNodeInfo#getPackageName()}.
     */
    private final String packageName;

    /**
     * The coordinates/boundaries of the widget.
     * <p>
     * (0,0)      (xMax,0)
     * left       right
     * x1         x2 (X = x1 + x2 / 2)
     * ------------
     * |          |
     * |          |
     * |          |
     * ------------
     * y1         y2 (Y = y1 + y2 / 2)
     * top        bottom
     * (yMax,0)   (xMax,yMax)
     */
    private final Rect bounds;
    private final int X;
    private final int Y;
    private final int x1;
    private final int x2;
    private final int y1;
    private final int y2;

    /**
     * Only set if the widget stores a text, e.g. the text of an input box, otherwise the empty string.
     * See {@link AccessibilityNodeInfo#getText()}.
     */
    private String text;

    /**
     * A possible content description of the widget, otherwise the empty string.
     * See {@link AccessibilityNodeInfo#getContentDescription()}.
     */
    private final String contentDesc;

    private final String labeledBy;
    private final boolean showingHintText;
    private final boolean focused;
    private final String errorText;
    private final boolean contextClickable;
    private final boolean importantForAccessibility;
    private final boolean accessibilityFocused;
    private final String labelFor;
    private final boolean checkable;
    private final boolean checked;
    private final boolean editable;
    private final boolean enabled;
    private final boolean focusable;
    private final boolean scrollable;
    private final boolean selected;
    private final boolean visible;
    private final int maxTextLength;
    private final boolean screenReaderFocusable;
    private final int inputType;
    private final boolean hasChildren;
    private final boolean heading;
    private final boolean password;
    private final boolean clickable;
    private final boolean longClickable;

    // mutable properties
    private String hint;

    // deprecated properties
    private String color;
    private String maxminLum;

    /**
     * Creates a new widget.
     *
     * @param node       A node in the ui hierarchy.
     * @param activity   The activity name the widget belongs to.
     * @param depth      The depth of the node in the ui hierarchy.
     * @param localIndex A local index for the widget's children.
     */
    public Widget(Widget parent, AccessibilityNodeInfo node, String activity,
                  int depth, int index, int localIndex) {

        this.parent = parent;
        this.activity = activity;
        this.packageName = Objects.toString(node.getPackageName(), activity.split("/")[0]);
        this.resourceID = Objects.toString(node.getViewIdResourceName(), "");
        this.clazz = Objects.toString(node.getClassName(), "");
        this.depth = depth;
        this.index = index;
        this.localIndex = localIndex;
        this.id = activity + "->" + depth + "->" + index + "->" + localIndex;
        children = new ArrayList<>();

        /*
        * NOTE: An AccessibilityNodeInfo object is only valid (non null) for a certain
        * amount of time, afterwards it expires. This means, we can't re-use this object
        * to get an up-to-date state of the widget, e.g. the currently displayed text.
        * Thus, we need to save all node attributes in dedicated variables and request
        * an ui object instead of performing the action directly on the node object.
         */
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        this.bounds = bounds;
        this.x1 = bounds.left;
        this.x2 = bounds.right;
        this.y1 = bounds.top;
        this.y2 = bounds.bottom;
        this.X = bounds.centerX();
        this.Y = bounds.centerY();
        this.editable = node.isEditable();
        this.checkable = node.isCheckable();
        this.password = node.isPassword();
        this.enabled = node.isEnabled();
        this.selected = node.isSelected();
        this.checked = node.isChecked();
        this.clickable = node.isClickable();
        this.focusable = node.isFocusable();
        this.focused = node.isFocused();
        this.longClickable = node.isLongClickable();
        this.scrollable = node.isScrollable();
        this.maxTextLength = node.getMaxTextLength();
        this.visible = node.isVisibleToUser();
        this.inputType = node.getInputType();
        this.accessibilityFocused = node.isAccessibilityFocused();
        this.hasChildren = node.getChildCount() > 0;

        this.text = Objects.toString(node.getText(), "");
        this.contentDesc = Objects.toString(node.getContentDescription(), "");
        this.errorText = Objects.toString(node.getError(), "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.importantForAccessibility = node.isImportantForAccessibility();
        } else {
            this.importantForAccessibility = true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.screenReaderFocusable = node.isScreenReaderFocusable();
        } else {
            this.screenReaderFocusable = true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.showingHintText = node.isShowingHintText();
        } else {
            this.showingHintText = false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.contextClickable = node.isContextClickable();
        } else {
            this.contextClickable = false;
        }

        AccessibilityNodeInfo.CollectionItemInfo cinfo = node.getCollectionItemInfo();
        if (cinfo != null) {
            this.heading = cinfo.isHeading();
        } else {
            this.heading = false;
        }

        AccessibilityNodeInfo lf = node.getLabelFor();
        String labelFor = "";
        if (lf != null) {
            labelFor = Objects.toString(lf.getViewIdResourceName(), "");
        }
        this.labelFor = labelFor;

        AccessibilityNodeInfo lb = node.getLabeledBy();
        String labelBy = "";
        if (lb != null) {
            labelBy = Objects.toString(lb.getViewIdResourceName(), "");
        }
        this.labeledBy = labelBy;
    }

    /**
     * Updates the text of the widget (only internally).
     *
     * @param text The new text for the widget.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the name of the activity the widget is placed on.
     *
     * @return Returns the full-qualified activity name.
     */
    public String getActivity() {
        return activity;
    }

    /**
     * Returns the parent widget.
     *
     * @return Returns the parent widget or {@code null} if the widget is the root widget.
     */
    public Widget getParent() {
        return parent;
    }

    /**
     * Returns the unique id of the widget. This is a combination of activity name, depth,
     * global and local index, see the constructor.
     *
     * @return Returns the widget id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the class name of the widget, e.g. android.view.ViewGroup.
     *
     * @return Returns the widget's class name or the empty string if none is defined.
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Returns the local index, i.e. the child number. Each widget that holds children, labels
     * those widgets starting from 0. This index is only unique within the children of a widget.
     *
     * @return Returns the local index.
     */
    public int getLocalIndex() {
        return localIndex;
    }

    /**
     * Returns the resource id.
     *
     * @return Returns the resource id or the empty string if none is defined.
     */
    public String getResourceID() {
        return resourceID;
    }

    /**
     * Returns the index of the widget in the ui hierarchy.
     *
     * @return Returns the widget's index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the package name of the widget.
     *
     * @return Returns the package name of the widget.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the siblings of this widget. If there are no siblings, an empty list is returned.
     *
     * @return Returns the siblings of the widget.
     */
    public List<Widget> getSiblings() {

        List<Widget> siblings = new ArrayList<>();

        if (parent != null) {
            for (Widget sibling : parent.getChildren()) {
                if (this.localIndex != sibling.getLocalIndex()) {
                    // only add siblings, not this widget itself
                    siblings.add(sibling);
                }
            }
        }

        return Collections.unmodifiableList(siblings);
    }

    /**
     * Returns the list of children.
     *
     * @return Returns the widget's children.
     */
    public List<Widget> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns the x1 coordinate of the widget.
     *
     * @return Returns the x1 coordinate.
     */
    public int getX1() {
        return x1;
    }

    /**
     * Returns the x2 coordinate of the widget.
     *
     * @return Returns the x2 coordinate.
     */
    public int getX2() {
        return x2;
    }

    /**
     * Returns the y1 coordinate of the widget.
     *
     * @return Returns the y1 coordinate.
     */
    public int getY1() {
        return y1;
    }

    /**
     * Returns the y2 coordinate of the widget.
     *
     * @return Returns the y2 coordinate.
     */
    public int getY2() {
        return y2;
    }

    /**
     * Returns the X coordinate of the widget.
     *
     * @return Returns the X coordinate.
     */
    public int getX() {
        return X;
    }

    /**
     * Returns the Y coordinate of the widget.
     *
     * @return Returns the Y coordinate.
     */
    public int getY() {
        return Y;
    }

    /**
     * Retrieves the boundaries of the widget.
     *
     * @return Returns a rectangle describing the widget boundaries.
     */
    public Rect getBounds() {
        return bounds;
    }

    /**
     * Returns whether the widget has children.
     *
     * @return Returns {@code true} if the widget has children, otherwise {@code false}
     *          is returned.
     */
    public boolean hasChildren() {
        return hasChildren;
    }

    /**
     * Adds  a child widget.
     *
     * @param widget Adds a child widget.
     */
    public void addChild(Widget widget) {
        children.add(widget);
    }

    /**
     * Returns whether the widget is visible or not.
     *
     * @return Returns {@code true} if the widget is visible, otherwise {@code false}
     *          is returned.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Returns the content description of the widget.
     *
     * @return Returns the content description or the empty string if undefined.
     */
    public String getContentDesc() {
        return contentDesc;
    }

    /**
     * Returns whether this widget represents a container, e.g. a linear layout.
     *
     * @return Returns {@code true} if the widget is a container, otherwise {@code false}
     *          is returned.
     */
    public boolean isContainer() {
        // TODO: extend with layouts defined at https://developer.android.com/reference/androidx/classes.html
        return getClazz().equals("android.widget.LinearLayout")
                || getClazz().equals("android.widget.FrameLayout")
                || getClazz().equals("android.widget.RelativeLayout")
                || getClazz().equals("android.widget.AbsoluteLayout")
                || getClazz().equals("android.widget.TableLayout")
                || getClazz().equals("android.widget.GridLayout")
                || getClazz().equals("androidx.constraintlayout.motion.widget.MotionLayout")
                || getClazz().equals("androidx.drawerlayout.widget.DrawerLayout")
                || getClazz().equals("androidx.constraintlayout.widget.ConstraintLayout")
                || getClazz().equals("androidx.appcompat.widget.LinearLayoutCompat")
                || getClazz().equals("androidx.coordinatorlayout.widget.CoordinatorLayout")
                || getClazz().equals("androidx.gridlayout.widget.GridLayout")
                || getClazz().equals("android.support.v4.widget.DrawerLayout")
                || getClazz().equals("android.support.v7.widget.GridLayout")
                || getClazz().equals("android.support.v7.widget.LinearLayoutCompat");
    }

    /**
     * Checks whether the widget is a son of an actionable container, e.g. a clickable
     * linear layout.
     *
     * @return Returns {@code true} if this widget is a son of an actionable container,
     *          otherwise {@code false} is returned.
     */
    public boolean isSonOfActionableContainer() {
        Widget parent = this.parent;
        while (parent != null) {
            if (parent.isActionable() && parent.isContainer())
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks whether the widget is either clickable, long-clickable or checkable.
     *
     * @return Returns {@code true} if this widget is actionable,
     *          otherwise {@code false} is returned.
     */
    public boolean isActionable() {
        return isClickable() || isLongClickable() || isCheckable();
    }

    public boolean isContextClickable() {
        return contextClickable;
    }

    public String getErrorText() {
        return errorText;
    }

    public boolean isCheckable() {
        return checkable;
    }

    public boolean isChecked() {
        return checked;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isFocusable() {
        return focusable;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public boolean isClickable() {
        return clickable;
    }

    public boolean isLongClickable() {
        return longClickable;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getText() {
        return text;
    }

    public boolean isShowingHintText() {
        return showingHintText;
    }

    public boolean isEditable() {
        return editable;
    }

    /**
     * Checks whether this widget represents an edit text widget.
     *
     * @return Returns {@code true} if this widget is an edit text widget, otherwise {@code false}
     *          is returned.
     */
    public boolean isEditTextType() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.EditText.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    public boolean isExecutable() {
        return clickable || longClickable || scrollable || isEditable();
    }

    /**
     * Checks whether the parent widget is of the given type, e.g. android.widget.TextView.
     *
     * @param type The type (class name) to check for.
     * @return Returns {@code true} if the parent widget is of the given type,
     *          otherwise {@code false} is returned.
     */
    public boolean directSonOf(String type) {
        Widget parent = this.parent;
        if (parent != null)
            return parent.getClazz().contains(type);
        return false;
    }

    /**
     * Checks whether any parent widget is of the given type, e.g. android.widget.TextView.
     *
     * @param type The type (class name) to check for.
     * @return Returns {@code true} if any parent widget is of the given type,
     *          otherwise {@code false} is returned.
     */
    public boolean isSonOf(String type) {
        Widget parent = this.parent;
        while (parent != null) {
            if (parent.getClazz().contains(type))
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks whether any parent widget is checkable.
     *
     * @return Returns {@code true} if a parent widget is checkable,
     *          otherwise {@code false} is returned.
     */
    public boolean isSonOfCheckable() {
        Widget parent = this.parent;
        while (parent != null) {
            if (parent.isCheckable())
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks whether any parent widget is long clickable.
     *
     * @return Returns {@code true} if a parent widget is long clickable,
     *          otherwise {@code false} is returned.
     */
    public boolean isSonOfLongClickable() {
        Widget parent = this.parent;
        while (parent != null) {
            if (parent.isLongClickable())
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks whether any parent widget is clickable.
     *
     * @return Returns {@code true} if a parent widget is clickable,
     *          otherwise {@code false} is returned.
     */
    public boolean isSonOfClickable() {
        Widget parent = this.parent;
        while (parent != null) {
            if (parent.isClickable())
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks whether any parent widget is scrollable.
     *
     * @return Returns {@code true} if a parent widget is scrollable,
     *          otherwise {@code false} is returned.
     */
    public boolean isSonOfScrollable() {
        Widget parent = this.parent;
        while (parent != null) {
            if (parent.isScrollable())
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public int getInputType() {
        return inputType;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    /**
     * Checks whether a hint is present.
     *
     * @return Returns {@code true} if a hint is present, otherwise {@code false}.
     */
    public boolean isHintPresent() {
        return hint != null && !hint.isEmpty();
    }

    public String getHint() {
        return hint;
    }

    public String getLabeledBy() {
        return this.labeledBy;
    }

    public String getLabelFor() {
        return this.labelFor;
    }

    public boolean isScreenReaderFocusable() {
        return screenReaderFocusable;
    }

    public boolean isImportantForAccessibility() {
        return importantForAccessibility;
    }

    public boolean isAccessibilityFocused() {
        return accessibilityFocused;
    }

    public boolean isHeading() {
        return heading;
    }

    public boolean isFocused() {
        return focused;
    }

    /**
     * Checks whether this widget represents a button.
     *
     * @return Returns {@code true} if this widget is a button, otherwise {@code false}
     *          is returned.
     */
    public boolean isButtonType() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.Button.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    /**
     * Checks whether this widget represents an image button.
     *
     * @return Returns {@code true} if this widget is an image button, otherwise {@code false}
     *          is returned.
     */
    public boolean isImageButtonType() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.ImageButton.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    /**
     * Checks whether this widget represents an image switcher.
     *
     * @return Returns {@code true} if this widget is an image switcher, otherwise {@code false}
     *          is returned.
     */
    public boolean isImageSwitcherType() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.ImageSwitcher.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    // TODO: find something more accurate if possible
    public boolean mightBeImage() {
        // android.widget.TextView components are drawables - they can contain images
        if (this.getClazz().contains("android.widget.TextView") && this.isClickable()) {
            if (this.getText().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isImageType() {

        if (this.clazz.contains("Image"))
            return true;
        if (this.mightBeImage())
            return true;

        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.ImageView.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    /**
     * Checks whether the widget represents a vertical or horizontal scroll view.
     *
     * @return Returns {@code true} if this widget is a scrollview,
     *          otherwise {@code false} is returned.
     */
    public boolean isScrollView() {
        return isVerticalScrollView() || isHorizontalScrollView();
    }

    /**
     * Checks whether this widget represents a vertical scrollview, see
     * https://developer.android.com/reference/android/widget/ScrollView.
     *
     * @return Returns {@code true} if this widget is a vertical scrollview,
     *          otherwise {@code false} is returned.
     */
    public boolean isVerticalScrollView() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.ScrollView.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    /**
     * Checks whether this widget represents a horizontal scrollview, see
     * https://developer.android.com/reference/android/widget/HorizontalScrollView and
     * https://developer.android.com/reference/android/support/v4/view/ViewPager.html.
     *
     * @return Returns {@code true} if this widget is a horizontal scrollview,
     *          otherwise {@code false} is returned.
     */
    public boolean isHorizontalScrollView() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.HorizontalScrollView.class.isAssignableFrom(clazz);
            // TODO: find androidX conform check
            //       || android.support.v4.view.ViewPager.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    /**
     * Checks whether this widget represents a spinner, see
     * https://developer.android.com/reference/android/widget/AbsSpinner.
     *
     * @return Returns {@code true} if this widget is a spinner, otherwise {@code false}
     *          is returned.
     */
    public boolean isSpinnerType() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.AbsSpinner.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    /**
     * Checks whether a parent widget represents an abstract list view.
     *
     * @return Returns {@code true} if a parent widget is an abstract list view,
     *          otherwise {@code false} is returned.
     */
    public boolean isSonOfListView() {
        Widget parent = this.parent;
        while (parent != null) {
            if (parent.isListViewType())
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks whether a parent widget represents an abstract spinner.
     *
     * @return Returns {@code true} if a parent widget is a spinner,
     *          otherwise {@code false} is returned.
     */
    public boolean isSonOfSpinner() {
        Widget parent = this.parent;
        while (parent != null) {
            if (parent.isSpinnerType())
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks whether this widget represents an abstract list view.
     *
     * @return Returns {@code true} if this widget is an abstract list view, otherwise {@code false}
     *          is returned.
     */
    public boolean isListViewType() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.AbsListView.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    /**
     * Checks whether this widget represents a text view.
     *
     * @return Returns {@code true} if this widget is a text view, otherwise {@code false}
     *          is returned.
     */
    public boolean isTextViewType() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.TextView.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    /**
     * Checks whether this widget implements the checkable interface, see
     * https://developer.android.com/reference/android/widget/Checkable.
     *
     * @return Returns {@code true} if this widget implements checkable, otherwise {@code false}
     *          is returned.
     */
    public boolean isCheckableType() {
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            return android.widget.Checkable.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            // classes from androidx package fail for instance (no dependency defined)
            MATE.log_warn("Class " + getClazz() + " not found!");
            return false;
        }
    }

    public boolean isPassword() {
        return password;
    }

    @Deprecated
    public String getColor() {
        return color;
    }

    @Deprecated
    public String getMaxminLum() {
        return maxminLum;
    }

    @Deprecated
    public void setMaxminLum(String maxminLum) {
        this.maxminLum = maxminLum;
    }

    @Deprecated
    public void setColor(String color) {

        String parts[] = color.split("#");
        if (parts.length == 2) {
            this.color = parts[0];
            setMaxminLum(parts[1]);
        } else {
            this.color = color;
            setMaxminLum("");
        }
    }

    /**
     * Compares two widgets for equality.
     *
     * @param o The object to which we compare.
     * @return Returns {@code true} if both widgets are equal, otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Widget other = (Widget) o;
            return getId().equals(other.getId())
                    && getX1() == other.getX1() &&
                    getX2() == other.getX2() &&
                    getY1() == other.getY1() &&
                    getY2() == other.getY2();
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the widget action.
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                getId(),
                getX1(),
                getX2(),
                getY1(),
                getY2());
    }

    /**
     * A simple string representation of the widget.
     *
     * @return Returns a simple string representation of the widget.
     */
    @NonNull
    @Override
    public String toString() {
        return "Widget{Activity: " + activity + ", resourceID: " + resourceID + ", clazz: " + clazz
                + ", depth: " + depth + ", index: " + index + ", local index: " + localIndex + "}";
    }
}
