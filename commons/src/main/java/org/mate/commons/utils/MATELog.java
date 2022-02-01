package org.mate.commons.utils;

import static android.util.Log.d;
import static android.util.Log.e;
import static android.util.Log.i;
import static android.util.Log.v;
import static android.util.Log.w;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Auxiliary class for logging MATE stuff to Android's Log.
 */
public class MATELog {
    public static void log(String msg) {
        i("apptest", msg);
    }

    public static void log_acc(String msg) {
        e("acc", msg);
    }

    public static void log_debug(String msg) {
        d("debug", msg);
    }

    public static void log_warn(String msg) {
        w("warning", msg);
    }

    public static void log_error(String msg) {
        e("error", msg);
    }
}
