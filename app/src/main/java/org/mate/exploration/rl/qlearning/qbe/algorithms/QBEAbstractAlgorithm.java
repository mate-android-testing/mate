package org.mate.exploration.rl.qlearning.qbe.algorithms;

import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.interaction.UIAbstractionLayer;

/**
 * An abstract QBE algorithm that runs until a specified timeout is reached.
 */
public abstract class QBEAbstractAlgorithm implements Algorithm {

    /**
     * The specified timeout in milli seconds.
     */
    protected final long timeoutInMilliseconds;

    /**
     * Enables the interaction with the AUT.
     */
    protected final UIAbstractionLayer uiAbstractionLayer = Registry.getUiAbstractionLayer();

    /**
     *
     * @param timeoutInMilliseconds
     */
    public QBEAbstractAlgorithm(final long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    /**
     * Checks whether the specified timeout has been reached.
     *
     * @param startTime The starting time of the exploration.
     * @return Returns {@code true} if the timeout has been reached, otherwise {@code false}.
     */
    protected boolean reachedTimeout(final long startTime) {
        final long currentTime = System.currentTimeMillis();
        return currentTime - startTime > timeoutInMilliseconds;
    }
}
