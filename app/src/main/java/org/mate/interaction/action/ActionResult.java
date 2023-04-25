package org.mate.interaction.action;

/**
 * The possible outcomes of applying an action.
 */
public enum ActionResult {
    SUCCESS,
    SUCCESS_OUTBOUND,
    FAILURE_APP_CRASH,
    FAILURE_UIAUTOMATOR,
    FAILURE_UNKNOWN;
}
