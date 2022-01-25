package org.mate.representation.util;

import static android.util.Log.*;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Auxiliary class for logging MATE Representation Layer stuff to Android's Log.
 */
public class MATERepLog {
    public static final String TAG = "MATE_REP_LAYER";

    public static void verbose(String msg) {
        v(TAG, msg);
    }

    public static void debug(String msg) {
        d(TAG, msg);
    }

    public static void info(String msg) {
        i(TAG, msg);
    }

    public static void warning(String msg) {
        w(TAG, msg);
    }

    public static void error(String msg) {
        e(TAG, msg);
    }

    public static void error(String msg, Exception e) {
        error(msg);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        error(String.format("Exception occurred: %s", stackTrace));
    }
}
