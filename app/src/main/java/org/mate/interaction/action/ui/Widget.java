package org.mate.interaction.action.ui;


import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by marceloe on 08/12/16.
 */
public class Widget {

    /**
     * A reference to the parent widget or {@code null}
     * if it is the root widget.
     */
    private Widget parent;

    /**
     * A list of direct descendants.
     */
    private List<Widget> children;

    /**
     * The resource id or a custom representation.
     * See the method createWidget() of the class {@link org.mate.state.executables.AppScreen#)}.
     */
    private String id;

    /**
     * A concatenation of the activity name and the id.
     * See the method createWidget() of the class {@link org.mate.state.executables.AppScreen#)}.
     */
    private String idByActivity;

    /**
     * The widget class name, e.g. android.view.ListView, or the empty string.
     * See {@link AccessibilityNodeInfo#getClassName()}.
     */
    private String clazz;

    /**
     *  The resource id of the widget or the empty string.
     *  See {@link AccessibilityNodeInfo#getViewIdResourceName()}.
     */
    private String resourceID;

    /**
     * The index of the widget in the ui hierarchy. Currently,
     * the index is not set properly.
     */
    @Deprecated
    private int index;

    /**
     * Only set if the widget stores a text, e.g. the text of an input box, otherwise the empty string.
     * See {@link AccessibilityNodeInfo#getText()}.
     */
    private String text;

    /**
     * The package name the widget is referring to.
     * See {@link AccessibilityNodeInfo#getPackageName()}.
     */
    private String packageName;

    /**
     * A possible content description of the widget, otherwise the empty string.
     * See {@link AccessibilityNodeInfo#getContentDescription()}.
     */
    private String contentDesc;

    private String labeledBy;
    private boolean showingHintText;
    private String color;
    private String maxminLum;
    private boolean focused;

    private String errorText;

    private boolean contextClickable;
    private boolean importantForAccessibility;
    private boolean accessibilityFocused;
    private String labelFor;
    private boolean checkable;
    private boolean checked;
    private boolean enabled;
    private boolean focusable;
    private boolean scrollable;
    private boolean clickable;
    private boolean longClickable;
    private boolean password;
    private boolean selected;
    private boolean visibleToUser;

    /**
     *  The coordinates/boundaries of the widget.
     *
     *  (0,0)      (xMax,0)
     *  left       right
     *  x1         x2 (X = x1 + x2 / 2)
     *  ------------
     *  |          |
     *  |          |
     *  |          |
     *  ------------
     *  y1         y2 (Y = y1 + y2 / 2)
     *  top        bottom
     *  (yMax,0)   (xMax,yMax)
     */
    private Rect bounds;
    private int X;
    private int Y;
    private int x1;
    private int x2;
    private int y1;
    private int y2;

    private int maxLength;
    private boolean screenReaderFocusable;
    private int inputType;
    private boolean hasChildren;
    private boolean usedAsStateDiff;
    private String hint;
    private boolean heading;

    /**
     * Creates a new widget.
     *
     * @param id A customized widget id, see createWidget() of the AppScreen class.
     * @param clazz The class name the widget refers to, e.g. android.widget.TextView or the
     *              empty string if not available.
     * @param idByActivity Either the view id resource name, see {@link AccessibilityNodeInfo#getViewIdResourceName()}
     *                     or a customized representation, check createWidget() of the AppScreen class.
     */
    public Widget(String id, String clazz, String idByActivity) {
        setId(id);
        setClazz(clazz);
        setBounds(new Rect());
        setContentDesc("");
        setText("");
        children = new ArrayList<>();
        maxLength = -1;
        this.idByActivity = idByActivity;
        usedAsStateDiff = false;
        hint = "";
        color = "";
    }

    public boolean isContextClickable() {
        return contextClickable;
    }

    public void setContextClickable(boolean contextClickable) {
        this.contextClickable = contextClickable;
    }

    public String getColor() {
        return color;
    }

    public String getMaxminLum() {
        return maxminLum;
    }

    public void setMaxminLum(String maxminLum) {
        this.maxminLum = maxminLum;
    }

    public void setColor(String color) {

        String parts[] = color.split("#");
        if (parts.length==2) {
            this.color = parts[0];
            setMaxminLum(parts[1]);
        }
        else{
            this.color = color;
            setMaxminLum("");
        }
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public Widget getParent() {
        return parent;
    }

    public void setParent(Widget parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClazz() {
        return clazz;
    }

    public void setVisibleToUser(boolean visibleToUser) {
        this.visibleToUser = visibleToUser;
    }

    public boolean isVisibleToUser() {
        return visibleToUser;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    /**
     * Returns the index of the widget in the ui hierarchy.
     *
     * @return Returns the widget's index.
     */
    @Deprecated
    public int getIndex() {
        return index;
    }

    /**
     * Sets the widget's index.
     *
     * @param index The new index.
     */
    @Deprecated
    public void setIndex(int index) {
        this.index = index;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getContentDesc() {
        return contentDesc;
    }

    public void setContentDesc(String contentDesc) {
        this.contentDesc = contentDesc;
    }

    public boolean isCheckable() {
        return checkable;
    }

    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public boolean isLongClickable() {
        return longClickable;
    }

    public void setLongClickable(boolean longClickable) {
        this.longClickable = longClickable;
    }

    public boolean isPassword() {
        return password;
    }

    public void setPassword(boolean password) {
        this.password = password;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isShowingHintText() {
        return showingHintText;
    }

    public void setShowingHintText(boolean showingHintText) {
        this.showingHintText = showingHintText;
    }

    public boolean isEditable() {
        //MATE.log("--Analyse if class i/s editable: " + clazz);
        if (clazz.contains("android.widget.EditText"))
            return true;
        if (clazz.contains("AppCompatEditText"))
            return true;
        if (clazz.contains("AutoCompleteTextView"))
            return true;
        if (clazz.contains("ExtractEditText"))
            return true;
        if (clazz.contains("GuidedActionEditText"))
            return true;
        if (clazz.contains("SearchEditText"))
            return true;
        if (clazz.contains("AppCompatAutoCompleteTextView"))
            return true;
        if (clazz.contains("AppCompatMultiAutoCompleteTextView"))
            return true;
        if (clazz.contains("MultiAutoCompleteTextView"))
            return true;
        if (clazz.contains("TextInputEditText"))
            return true;


        Class<?> clazzx = null;
        //MATE.log("Analyse if class is editable: " + clazzx);
        try {
            clazzx = Class.forName(clazz);
            //MATE.log("Analyse if class is editable: " + clazzx);
            boolean editType = 	android.widget.EditText.class.isAssignableFrom(clazzx);
            if (editType)
                return true;
        } catch (ClassNotFoundException e) {
            //MATE.log("ERRO - class not found: " + clazzx);
        }



        return false;
    }

    public boolean isExecutable() {
        return clickable || longClickable || scrollable || isEditable();
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    /**
     * Defines the boundaries of the widget. As a side effect,
     * the coordinates of the widget are adjusted.
     *
     * @param rectangle The rectangle defining the boundaries of the widget.
     */
    public void setBounds(Rect rectangle) {

        this.bounds = rectangle;

        setX1(rectangle.left);
        setX2(rectangle.right);
        setY1(rectangle.top);
        setY2(rectangle.bottom);

        // TODO: there is 'exactCenter()' for X and Y if desired
        setX(rectangle.centerX());
        setY(rectangle.centerY());
    }

    /**
     * Retrieves the boundaries of the widget.
     *
     * @return Returns a rectangle describing the widget boundaries.
     */
    public Rect getBounds() {
        return bounds;
    }

    public boolean directSonOf(String type) {
        Widget wparent = this.parent;
        if (wparent != null)
            if (wparent.getClazz().contains(type))
                return true;
        return false;
    }

    public boolean isSonOf(String type) {
        Widget wparent = this.parent;
        while (wparent != null) {
            if (wparent.getClazz().contains(type))
                return true;
            else
                wparent = wparent.getParent();
        }
        return false;
    }


    public List<Widget> getNextChildWithText() {
        List<Widget> ws = new ArrayList<>();
        for (Widget child : children) {
            //System.out.println("has children: " + child.getText());
            if (!child.getText().equals("")) {
                ws.add(child);
            }
            ws.addAll(child.getNextChildWithText());
        }

        return ws;
    }

    public List<Widget> getNextChildWithDescContentText() {
        List<Widget> ws = new ArrayList<>();

        for (Widget child : children) {
            //System.out.println("has children: " + child.getText());
            if (!child.getContentDesc().equals("")) {
                ws.add(child);

            }
            ws.addAll(child.getNextChildWithDescContentText());
        }

        return ws;
    }

    public void addChild(Widget widget) {
        children.add(widget);
    }

    public String getNextChildsText() {
        String childText = "";
        for (Widget wg : getNextChildWithText())
            childText += wg.getText() + " ";
        return childText;
    }

    public String getIdByActivity() {
        return idByActivity;
    }

    public void setIdByActivity(String idByActivity) {
        this.idByActivity = idByActivity;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public boolean isSonOfLongClickable() {
        Widget wparent = this.parent;
        while (wparent != null) {
            if (wparent.isLongClickable())
                return true;
            else
                wparent = wparent.getParent();
        }
        return false;
    }

    public boolean isSonOfScrollable() {
        Widget wparent = this.parent;
        while (wparent != null) {
            if (wparent.isScrollable())
                return true;
            else
                wparent = wparent.getParent();
        }
        return false;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public boolean isUsedAsStateDiff() {
        return usedAsStateDiff;
    }

    public void setUsedAsStateDiff(boolean usedAsStateDiff) {
        this.usedAsStateDiff = usedAsStateDiff;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getHint() {
        return hint;
    }

    public boolean isEmpty() {
        if (hint.equals(text))
            return true;
        if (text.equals(""))
            return true;
        return false;
    }

    public void setLabeledBy(String labeledBy) {
        this.labeledBy = labeledBy;
    }

    public String getLabeledBy() {
        return this.labeledBy;
    }

    public void setLabelFor(String labelFor){
        this.labelFor = labelFor;
    }

    public String getLabelFor(){
        return this.labelFor;
    }

    public boolean needsContrastChecked() {
        Set<String> excludedClasses = new HashSet<String>();
       // excludedClasses.add("Layout");
        //excludedClasses.add("ViewGroup");
        //excludedClasses.add("ScrollView");
        //excludedClasses.add("Spinner");
        //excludedClasses.add("TableRow");
        //excludedClasses.add("ListView");
        //excludedClasses.add("GridView");

        if (!this.isImportantForAccessibility())
            return false;

        if (this.bounds.equals("[0,0][0,0]"))
            return false;

        if (this.isEditable() && this.text.equals(""))
            return false;

        //if (this.getClazz().contains("Text") && this.getText().equals(""))
          //  return false;

        if (this.getClazz().contains("Image") && !this.isActionable())
            return false;

        for (String excluded : excludedClasses) {
            if (this.clazz.contains(excluded))
                return false;
        }

        if (!this.isActionable() && !this.getClazz().contains("Text"))
            return false;


        return true;
    }

    public boolean isScreenReaderFocusable() {
        return screenReaderFocusable;
    }

    public void setScreenReaderFocusable(boolean screenReaderFocusable) {
        this.screenReaderFocusable = screenReaderFocusable;
    }

    public boolean isImportantForAccessibility() {
        return importantForAccessibility;
    }

    public void setImportantForAccessibility(boolean importantForAccessibility) {
        this.importantForAccessibility = importantForAccessibility;
    }

    public boolean isAccessibilityFocused() {
        return accessibilityFocused;
    }

    public void setAccessibilityFocused(boolean accessibilityFocused) {
        this.accessibilityFocused = accessibilityFocused;
    }

    public List<Widget> getChildren() {
        return children;
    }

    public void setHeading(boolean heading) {
        this.heading = heading;
    }

    public boolean isHeading() {
        return heading;
    }

    public boolean isButtonType(){

        try {
            Class<?> clazz = Class.forName(this.getClazz());
            boolean buttonType = android.widget.Button.class.isAssignableFrom(clazz);
            return buttonType;
        }
        catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }
        return false;
    }

    public boolean isImageButtonType(){
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            boolean imageButtonType = android.widget.ImageButton.class.isAssignableFrom(clazz);
            return imageButtonType;
        }
        catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }
        return false;
    }

    public boolean isImageSwitcherType(){
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            boolean imageSwitcherType = android.widget.ImageSwitcher.class.isAssignableFrom(clazz);
            return imageSwitcherType;
        }
        catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }
        return false;
    }

    public boolean mightBeImage(){
        //android.widget.TextView components are drawables - they can contain images
        if (this.getClazz().contains("android.widget.TextView") && this.isClickable()){
            if (this.getText().equals("")){
                return true;
            }
        }
        return false;
    }

    public boolean isImageType(){

        if (this.clazz.contains("Image"))
            return true;
        if (this.mightBeImage())
            return true;

        try {
            Class<?> clazz = Class.forName(this.getClazz());
            boolean imageType = android.widget.ImageView.class.isAssignableFrom(clazz);
            return imageType;
        }
        catch (ClassNotFoundException e) {
           // e.printStackTrace();
        }
        return false;
    }

    public boolean isSpinnerType(){
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            boolean spinnerType = android.widget.AbsSpinner.class.isAssignableFrom(clazz);
            return spinnerType;
        }
        catch (ClassNotFoundException e) {
           // e.printStackTrace();
        }
        return false;
    }

    public boolean isTextViewType(){
        try {
            Class<?> clazz = Class.forName(this.getClazz());
            boolean textViewType = android.widget.TextView.class.isAssignableFrom(clazz);
            return textViewType;
        }
        catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }
        return false;
    }


    public boolean isActionable() {
        return this.isEditable()||this.isClickable()||this.isLongClickable()||this.isSpinnerType()||this.isCheckable();
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused(){
        return focused;
    }

    /**
     * Compares two widgets for equality. Note that
     * this check might be not fully unique!
     *
     * @param o The object to which we compare.
     * @return Returns {@code true} if both widgets are equal,
     *          otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Widget other = (Widget) o;
            return Objects.equals(getIdByActivity(), other.getIdByActivity()) &&
                    getX1() == other.getX1() &&
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
                getIdByActivity(),
                getX1(),
                getX2(),
                getY1(),
                getY2());
    }
}
