package org.mate.ui;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by marceloe on 08/12/16.
 */
public class Widget {

    private Widget parent;
    private String id;
    private String idByActivity;
    private String clazz;
    private String text;
    private String resourceID;
    private int index;
    private String packageName;
    private String contentDesc;
    private String labeledBy;
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
    private String bounds;
    private String originalBounds;
    private int X;
    private int Y;
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int maxLength;

    private int inputType;
    private boolean hasChildren;

    private List<Widget> children;

    private boolean usedAsStateDiff;

    private String hint;
    private boolean heading;

    public Widget(String id, String clazz, String idByActivity){
        setId(id);
        setClazz(clazz);
        originalBounds="";
        setBounds("[0,0][0,0]");
        setContentDesc("");
        setText("");
        children = new ArrayList<>();
        maxLength=-1;
        this.idByActivity=idByActivity;
        usedAsStateDiff=false;
        hint="";

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

    public void setVisibleToUser(boolean visibleToUser){
        this.visibleToUser = visibleToUser;
    }

    public boolean isVisibleToUser(){
        return visibleToUser;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public int getIndex() {
        return index;
    }

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

    public String getBounds() {
        return bounds;
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

    public boolean isEditable(){

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
        return false;
    }

    public boolean isExecutable(){
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

    public void setBounds(String bounds) {
        this.bounds = bounds;

        if (originalBounds!=null && originalBounds.equals(""))
            originalBounds=bounds;

        String value = bounds;
        value = value.replace("][","|");
        value = value.replace("[","");
        value = value.replace("]","");
        String[] twoPos = value.split("\\|");
        String[] first = twoPos[0].split(",");
        String[] second = twoPos[1].split(",");
        x1 = Integer.valueOf(first[0]);
        y1 = Integer.valueOf(first[1]);

        x2 = Integer.valueOf(second[0]);
        y2 = Integer.valueOf(second[1]);

        setX((x1+x2)/2);
        setY((y1+y2)/2);
    }

    public boolean directSonOf(String type){
        Widget wparent = this.parent;
        if (wparent!=null)
            if (wparent.getClazz().contains(type))
                return true;
        return false;
    }

    public boolean isSonOf(String type) {
        Widget wparent = this.parent;
        while (wparent!=null){
            if (wparent.getClazz().contains(type))
                return true;
            else
                wparent = wparent.getParent();
        }
        return false;
    }


    public List<Widget> getNextChildWithText(){
        List<Widget> ws = new ArrayList<>();
        for (Widget child: children){
            //System.out.println("has children: " + child.getText());
            if (!child.getText().equals("")){
                ws.add(child);
            }
            ws.addAll(child.getNextChildWithText());
        }

        return ws;
    }

    public List<Widget> getNextChildWithDescContentText(){
        List<Widget> ws = new ArrayList<>();

        for (Widget child: children){
            //System.out.println("has children: " + child.getText());
            if (!child.getContentDesc().equals("")){
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
        for (Widget wg: getNextChildWithText())
            childText+=wg.getText()+" ";
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

    public boolean isSonOfLongClickable(){
        Widget wparent = this.parent;
        while (wparent!=null){
            if (wparent.isLongClickable())
                return true;
            else
                wparent = wparent.getParent();
        }
        return false;
    }

    public boolean isSonOfScrollable(){
        Widget wparent = this.parent;
        while (wparent!=null){
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

    public String getHint(){
        return hint;
    }

    public boolean isEmpty(){
        if (hint.equals(text))
            return true;
        if (text.equals(""))
            return true;
        return false;
    }

    public void setLabeledBy(String labeledBy) {
        this.labeledBy = labeledBy;
    }

    public String getLabeledBy(){
        return this.labeledBy;
    }

    public boolean needsContrastChecked() {
        Set<String> excludedClasses = new HashSet<String>();
        excludedClasses.add("Layout");
        excludedClasses.add("ViewGroup");
        excludedClasses.add("ScrollView");
        excludedClasses.add("Spinner");
        excludedClasses.add("TableRow");
        excludedClasses.add("ListView");
        excludedClasses.add("GridView");

        if (this.bounds.equals("[0,0][0,0]"))
            return false;

        if (this.isEditable() && this.text.equals(""))
            return false;

        if (this.getClazz().contains("Text")&&this.getText().equals(""))
            return false;

        if (this.getClazz().contains("Image")&&!this.isExecutable())
            return false;

        for (String excluded: excludedClasses){
            if (this.clazz.contains(excluded))
                return false;
        }
        if (!this.isExecutable()&&!this.getClazz().contains("Text"))
            return false;


        return true;
    }

    public List<Widget> getChildren() {
        return children;
    }

    public void setHeading(boolean heading) {
        this.heading = heading;
    }

    public boolean isHeading(){
        return heading;
    }
}
