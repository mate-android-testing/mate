package org.mate.utils;

import org.mate.MATE;

public class TimeoutRun {
    private static boolean stopMasked = false;
    private static boolean wasStopped = false;
    private static boolean activeRun = false;

    public static void maskStop() {
        if (!activeRun) {
            return;
        }

        withStopLock(new Runnable() {
            @Override
            public void run() {
                if (wasStopped) {
                    throw new IllegalStateException("Entering a new stop masked block in an already stopped thread.");
                }
                stopMasked = true;
            }
        });
    }

    public static void unmaskStop() {
        if (!activeRun) {
            return;
        }

        withStopLock(new Runnable() {
            @Override
            public void run() {
                stopMasked = false;
                if (wasStopped) {
                    Thread.currentThread().stop();
                }
            }
        });
    }

    private static synchronized void withStopLock(Runnable r) {
        r.run();
    }

    public static boolean timeoutRun(Runnable r, long milliseconds) {
        stopMasked = false;
        wasStopped = false;
        activeRun = true;

        StopMaskableThread run = new StopMaskableThread(r);
        run.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                MATE.log_acc("Unexpected exception during timeout run: " + e.getMessage());
            }
        });
        run.start();

        boolean finishedBeforeTimeout = false;
        try {
            MATE.log_acc("Starting timeout run...");
            run.join(milliseconds);
            if (!run.isAlive()) {
                MATE.log_acc("Finished run before timeout.");
                finishedBeforeTimeout = true;
            } else {
                MATE.log_acc("Timeout. Requesting shutdown...");
                run.saveStop();
                try {
                    run.join(30000);
                    if (run.isAlive()) {
                        MATE.log_acc("Timeout didn't finish up after shutdown request.");
                    }
                } catch (InterruptedException e) {
                    MATE.log_acc("Unexpected exception awaiting termination of timeout run: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            MATE.log_acc("Unexpected exception in timeout run: " + e.getMessage());
            e.printStackTrace();
        } finally {
            activeRun = false;
        }

        MATE.log_acc("Finished run due to timeout.");
        return  finishedBeforeTimeout;
    }

    private static class StopMaskableThread extends Thread {
        private StopMaskableThread(Runnable r) {
            super(r);
        }

        public void saveStop() {
            withStopLock(new Runnable() {
                @Override
                public void run() {
                    if (stopMasked) {
                        wasStopped = true;
                    } else {
                        StopMaskableThread.super.stop();
                    }
                }
            });
        }
    }
}
