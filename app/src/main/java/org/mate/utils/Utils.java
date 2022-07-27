package org.mate.utils;

import org.mate.MATE;

/**
 * A collection of utility functions.
 */
public class Utils {

    private Utils() {
        throw new UnsupportedOperationException("Utility class!");
    }

    /**
     * Suspend the current thread for a given time amount.
     *
     * @param time The time amount in milliseconds describing how long the thread should sleep.
     */
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            MATE.log_warn("Interrupt: " + e.getMessage());
            throw new MateInterruptedException(e);
        }
    }
}
