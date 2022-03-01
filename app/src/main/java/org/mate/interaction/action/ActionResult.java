package org.mate.interaction.action;

/**
 * The possible outcomes of applying an action.
 */
public enum ActionResult {
    FAILURE_UNKNOWN,
    FAILURE_EMULATOR_CRASH,
    FAILURE_APP_CRASH,
    SUCCESS_NEW_STATE,
    SUCCESS,
    SUCCESS_OUTBOUND;
}
