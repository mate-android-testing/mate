package org.mate.exploration.ant;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class AntStatsLogger {
    private FileOutputStream fos;
    private BufferedWriter writer;

    public AntStatsLogger() {
        try {
            fos = InstrumentationRegistry.getTargetContext().openFileOutput("antstats.log", Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            //Fehlerbehandlung
        }
        writer = new BufferedWriter(new OutputStreamWriter(fos));
    }

    public void write(String log) {
        try {
            writer.write(log);
            writer.flush();
        } catch (IOException e) {
            //Fehlerbehandlung
        }
    }

    public void close() {
        try {
            writer.close();
            fos.close();
        } catch (IOException e) {
            //Fehlerbehandlung
            throw new IllegalStateException("Error while closing ant stats logger", e);
        }
    }
}
