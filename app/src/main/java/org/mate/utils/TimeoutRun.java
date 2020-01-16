package org.mate.utils;

import org.mate.MATE;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutRun {
    private static boolean interruptMasked = false;
    private static boolean wasInterrupted = false;

    public static void maskInterrupt() {
        withInterruptLock(new Runnable() {
            @Override
            public void run() {
                if (wasInterrupted) {
                    throw new IllegalStateException("Entering a new interrupt masked block in an already interrupted thread.");
                }
                interruptMasked = true;
            }
        });
    }

    public static void unmaskInterrupt() {
        withInterruptLock(new Runnable() {
            @Override
            public void run() {
                interruptMasked = false;
                if (wasInterrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private static synchronized void withInterruptLock(Runnable r) {
        r.run();
    }

    public static boolean timeoutRun(Callable<Void> c, long milliseconds) {
        interruptMasked = false;
        wasInterrupted = false;

        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new InterruptMaskableThread(r);
            }
        });

        Future<Void> future = executor.submit(c);
        boolean finishedWithoutTimeout = false;

        try {
            MATE.log_acc("Starting timeout run...");
            future.get(milliseconds, TimeUnit.MILLISECONDS);
            MATE.log_acc("Finished run before timeout.");
            finishedWithoutTimeout = true;
        } catch (TimeoutException e) {
            executor.shutdownNow();
            try {
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

    private static class InterruptMaskableThread extends Thread {
        private InterruptMaskableThread(Runnable r) {
            super(r);
        }

        @Override
        public void interrupt() {
            withInterruptLock(new Runnable() {
                @Override
                public void run() {
                    if (interruptMasked) {
                        wasInterrupted = true;
                    } else {
                        InterruptMaskableThread.super.interrupt();
                    }
                }
            });
        }
    }
}
