package org.mate.utils;

import org.mate.commons.utils.MATELog;

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
            MATELog.log_acc("Starting timeout run...");
            future.get(milliseconds, TimeUnit.MILLISECONDS);
            MATELog.log_acc("Finished run before timeout.");
            finishedWithoutTimeout = true;
        } catch (TimeoutException e) {
            MATELog.log_acc("Timeout. Requesting shutdown...");
            executor.shutdownNow();
            try {
                executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                MATELog.log_acc("Unexpected exception awaiting termination of timeout run: " + e.getMessage());
                e.printStackTrace();
            }
            MATELog.log_acc("Finished run due to timeout.");
        } catch (InterruptedException | ExecutionException e) {
            MATELog.log_acc("Unexpected exception in timeout run: " + e.getMessage());
            e.printStackTrace();
        }

        executor.shutdownNow();
        return  finishedWithoutTimeout;
    }
}
