package org.mate.interaction.action.intent;

import org.mate.commons.utils.MATELog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SystemActionParser {

    private static final String SYSTEM_EVENTS_FILE = "data/data/org.mate/broadcast_actions.txt";

    private SystemActionParser() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    /**
     * Reads the list of system events, in particular the name of the corresponding actions.
     *
     * @return Returns a list of system events that can be received by broadcast receivers.
     */
    public static List<String> loadSystemEventActions() {

        try(BufferedReader br = new BufferedReader(new FileReader(SYSTEM_EVENTS_FILE))) {

            List<String> systemEvents = new ArrayList<>();
            String line = br.readLine();

            while (line != null) {
                systemEvents.add(line);
                line = br.readLine();
            }

            return systemEvents;
        } catch (IOException e) {
            MATELog.log("Reading system events from file failed!");
            throw new IllegalStateException(e);
        }
    }
}
