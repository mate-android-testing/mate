package org.mate.utils;

import org.mate.MATE;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutRun {
    private static synchronized void withInterruptLock(Runnable r) {
        r.run();
    }

    public static boolean timeoutRun(Callable<Void> c, long milliseconds) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Void> future = executor.submit(c);
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
