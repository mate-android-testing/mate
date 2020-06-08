package org.mate.accessibility.check;

public enum AccessibilityViolationType {

    //WCAG

    NON_TEXT_CONTENT("1.1.1 - Non-text Content (A)"),

    ORIENTATION("1.3.4 - Orientation(AA)"),
    IDENTIFY_INPUT_PURPOSE("1.3.5 - Identify Input Purpose (AA)"),

    USE_OF_COLOR("1.4.1 - Use of Color (A)"),
    AUDIO_CONTROL("1.4.2 - Audio Control (A)"),
    CONSTRAST_MINUMUM("1.4.3 - Contrast Minimum (AA)"),
    RESIZE_TEXT("1.4.4 - Resize text (AA)"),
    IMAGE_OF_TEXT("1.4.5 - Images of Text (AA)"),
    CONSTRAST_ENHANCED("1.4.3 - Contrast Enhanced (AAA)"),
    LOW_NO_BACKGROUND("1.4.7 Low or No Background Audio (AAA)"),
    VISUAL_PRESENTATION("1.4.8 - Visual Presentation(AAA"),
    IMAGE_OF_TEXT_NO_EXCEPTION("1.4.9 - Images Of Text No Exception (AAA)"),
    REFLOW("1.4.10 - Reflow (AA)"),
    NON_TEXT_CONTRAST("1.4.11 - Non-Text Contrast (AA)"),
    TEXT_SPACING("1.4.12 Text Spacing (AA)"),
    CONTENT_HOVER_FOCUS("1.4.13 - Content on Hover or Focus (AA)"),


    PAGE_TITLED("2.4.2 - Page Titled (A)"),

    FOCUS_VISIBLE("2.4.7 - Focus Visible (AA)"),

    TARGET_SIZE("2.5.5 - Target Size (AAA)"),




    //BBC
    EDITABLE_CONTENT_DESC("EDITABLE_CONTENT_DESC"),
    MISSING_FORM_CONTROL_LABEL("MISSING_FORM_CONTROL_LABEL"),
    MISSING_ALTERNATIVE_TEXT("MISSING_ALTERNATIVE_TEXT"),
    LOW_CONTRAST_RATIO("LOW_CONTRAST_RATIO"),
    SMALL_TOUCH_AREA("SMALL_TOUCH_AREA"),
    DUPLICATE_CONTENT_DESCRIPTION("GENERAL - Duplicate Content Description (label)"),
    NON_CLICKABLE_SPAN("NON_CLICKABLE_SPAN"),
    MISSING_INPUT_TYPE("MISSING_INPUT_TYPE"),
    LABEL_NOT_DEFINED("BBC - Label is not defined"),
    LABEL_FAR_FROM_INPUTTEXT("BBC - FORM - Form Layout (Label far from input area)"),
    COLOUR_MEANING("COLOUR_MEANING"),
    SPACING("BBC - DESIGN - Spacing"),
    HIDDEN_DECORATIVE_CONTENT("HIDDEN_DECORATIVE_CONTENT"),
    DESCRIPTIVE_LINKS("BBC - LINK - Descriptive link (repeated)"),
    VISIBLE_FOCUS("VISIBLE_FOCUS"),
    MANAGING_FOCUS("BBC - FOCUS - Managing Focus"),
    RADIOGROUPCHECK("RADIOGROUPCHECK"),
    ACTIONABLE_ELEMENTS("BBC - DESIGN - Actionable Element"),
    LINK_TO_ALTERNATIVE_FORMAT("LINK_TO_ALTERNATIVE_FORMAT"),
    PHANTOM_ELEMENT("GENERAL - Phantom Element"),
    ERROR_MESSAGE("ERROR_MESSAGE");


    private String type;

    private AccessibilityViolationType(String type){
        this.type = type;
    }

    public String getValue(){
        return this.type;
    }

}
