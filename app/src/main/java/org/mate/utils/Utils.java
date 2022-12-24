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
            throw new MateInterruptedException(e);
        }
    }

    /**
     * Suspends the current thread for a given time and suppresses any interrupt in the meantime.
     *
     * @param time The time amount in milliseconds to suspend the current thread.
     */
    public static void sleepWithoutInterrupt(final long time) {

        long currentTime = System.currentTimeMillis();
        final long startTime = currentTime;
        final long endTime = startTime + time;
        boolean interrupted = false;

        while ((currentTime = System.currentTimeMillis()) < endTime) {
            final long remainingTime = endTime - currentTime;
            try {
                Thread.sleep(remainingTime);
            } catch (final InterruptedException ignored) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Throws a {@code MateInterruptedException} if the calling thread has been interrupted and
     * clears the interrupted status. Does nothing, if the calling thread has not been interrupted.
     */
    public static void throwOnInterrupt() {
        if (Thread.interrupted()) {
            MATE.log_debug("Interrupt detected!");
            throw new MateInterruptedException();
        }
    }

}
