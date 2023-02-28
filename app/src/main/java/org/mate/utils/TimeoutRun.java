package org.mate.utils;

import org.mate.MATE;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Performs a timeout-based execution task, i.e. the algorithm we like to run.
 */
public class TimeoutRun {

    public static boolean timeoutRun(Callable<Void> c, long milliseconds) {

        // We like to log the exception that caused terminating the thread.
        final Callable<Void> logExceptions = () -> {
            try {
                c.call();
                return null;
            } catch (final MateInterruptedException e) {
                MATE.log_acc("Terminating timeout thread with interrupt: " + e.getMessage());
                throw e;
            } catch(final Exception e) {
                MATE.log_acc("Unexpected exception in timeout thread: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        };

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<Void> future = executor.submit(logExceptions);
        boolean finishedWithoutTimeout = false;

        try {
            MATE.log_acc("Starting timeout run...");
            future.get(milliseconds, TimeUnit.MILLISECONDS);
            MATE.log_acc("Finished run before timeout.");
            finishedWithoutTimeout = true;
        } catch (TimeoutException e) {
            MATE.log_acc("Timeout. Requesting shutdown...");
            executor.shutdownNow();
            try {
                // We may need to wait until the tracer finished writing its traces.
                executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                MATE.log_acc("Unexpected exception awaiting termination of timeout run: " + e.getMessage());
                e.printStackTrace();
            }
            MATE.log_acc("Finished run due to timeout.");
        } catch (InterruptedException | ExecutionException e) {
            MATE.log_acc("Unexpected exception in timeout run: " + e.getMessage());
            e.printStackTrace();
        }

        executor.shutdownNow();
        return  finishedWithoutTimeout;
    }
}
