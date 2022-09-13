package org.mate.utils;

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
            throw new MateInterruptedException(e);
        }
    }

    /**
     * Throws a {@code MateInterruptedException} if the calling thread has been interrupted and
     * clears the interrupted status. Does nothing, if the calling thread has not been interrupted.
     */
    public static void throwOnInterrupt() {
        if (Thread.interrupted()) {
            throw new MateInterruptedException();
        }
    }

}
