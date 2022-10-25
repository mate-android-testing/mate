package org.mate.utils;

/**
 * Thrown when an UIAutomator appears.
 */
public class UIAutomatorException extends RuntimeException {
    public UIAutomatorException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Constructs an UIAutomator exception.
     *
     * @param message The error message.
     */
    public UIAutomatorException(String message) {
        super(message);
    }
}
