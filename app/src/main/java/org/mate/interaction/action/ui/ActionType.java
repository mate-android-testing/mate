package org.mate.interaction.action.ui;

public enum ActionType {
    CLICK,
    LONG_CLICK,
    TYPE_TEXT,
    TYPE_SPECIFIC_TEXT,
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    BACK,
    MENU,
    CLEAR_TEXT,
    MANUAL_ACTION,
    TOGGLE_ROTATION,
    HOME,
    QUICK_SETTINGS,
    SEARCH,
    NOTIFICATIONS,
    SLEEP,
    WAKE_UP,
    DELETE,
    DPAD_UP,
    DPAD_DOWN,
    DPAD_LEFT,
    DPAD_RIGHT,
    DPAD_CENTER,
    ENTER,
    CHANGE_SEEK_BAR,
    CHANGE_CHECK_BOX,
    FILL_FORM_AND_SUBMIT,
    FILL_FORM,
    SPINNER_SCROLLING,
    MENU_CLICK_AND_ITEM_SELECTION,
    SORT_MENU_CLICK_AND_SORT_ORDER_SELECTION,
    OPEN_NAVIGATION_AND_OPTION_SELECTION,
    TYPE_TEXT_AND_PRESS_ENTER,
    CHANGE_RADIO_GROUP_SELECTIONS,
    CHANGE_LIST_VIEW_SELECTION,
    CHANGE_SEEK_BARS,
    CHANGE_DATE;

    public static final ActionType[] primitiveActionTypes = {
            CLICK,
            LONG_CLICK,
            SWIPE_UP,
            SWIPE_DOWN,
            SWIPE_LEFT,
            SWIPE_RIGHT,
            TYPE_TEXT,
            CLEAR_TEXT,
            BACK,
            MENU
    };

    public static final ActionType[] motifActionTypes = {
            FILL_FORM_AND_SUBMIT,
            FILL_FORM,
            SPINNER_SCROLLING,
            MENU_CLICK_AND_ITEM_SELECTION,
            SORT_MENU_CLICK_AND_SORT_ORDER_SELECTION,
            OPEN_NAVIGATION_AND_OPTION_SELECTION,
            TYPE_TEXT_AND_PRESS_ENTER,
            CHANGE_RADIO_GROUP_SELECTIONS,
            CHANGE_LIST_VIEW_SELECTION,
            CHANGE_SEEK_BARS,
            CHANGE_DATE
    };
}
