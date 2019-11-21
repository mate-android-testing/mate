package org.mate.accessibility;

public class AccessibilityViolationTypes {


    public static final String[] NAMES = new String[13];

    static {
        NAMES[1] = "EDITABLE-CONTENT-DESC";
        NAMES[2] = "MISSING-FORM-CONTROL-LABEL";
        NAMES[3] = "MISSING ALTERNATIVE TEXT";
        NAMES[4] = "LOW-CONTRAST-RATIO";
        NAMES[5] = "SMALL-TOUCH-AREA";
        NAMES[6] = "DUPLICATE-CONTENT-DESCRIPTION-OR-HINT";
        NAMES[7] = "NON-CLICKABLE-SPAN";
        NAMES[8] = "MISSING-OUTPUT-TYPE";
        NAMES[9] = "LABEL-NOT-DEFINED";
        NAMES[10] = "LABEL-FAR-FROM-FORM-CONTROL";
        NAMES[11] = "COLOUR-AND-MEANING";
        NAMES[12] = "SPACING";
    }

    public static final int EDITABLE_CONTENT_DESC = 1;
    public static final int MISSING_FORM_CONTROL_LABEL = 2;
    public static final int MISSING_ALTERNATIVE_TEXT = 3;
    public static final int LOW_CONTRAST_RATIO = 4;
    public static final int SMALL_TOUCH_AREA = 5;
    public static final int DUPLICATE_CONTENT_DESCRIPTION = 6;
    public static final int NON_CLICKABLE_SPAN = 7;
    public static final int MISSING_INPUT_TYPE = 8;
    public static final int LABEL_NOT_DEFINED = 9;
    public static final int LABEL_FAR_FROM_INPUTTEXT = 10;
    public static final int COLOUR_MEANING = 11;
    public static final int SPACING = 12;






}
