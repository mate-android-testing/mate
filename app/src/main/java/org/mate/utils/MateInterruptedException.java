package org.mate.utils;

/**
 * A custom {@link RuntimeException} that is thrown when a sleeping thread has been interrupted.
 */
public final class MateInterruptedException extends RuntimeException {

    /**
     * Constructs a new MateInterruptedException.
     */
    public MateInterruptedException() {
        super();
    }

    /**
     * Constructs a new MateInterruptedException with the given cause.
     *
     * @param cause The cause of the exception.
     */
    public MateInterruptedException(final Throwable cause) {
        super(cause);
    }
}

