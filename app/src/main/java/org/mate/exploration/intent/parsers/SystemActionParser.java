package org.mate.exploration.intent.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a simple parser for the broadcast_actions.txt file, which is located within the folder
 * Android/Sdk/platforms/<api-level>/.
 */
public final class SystemActionParser {

    /**
     * The location where the broadcast_actions.txt file needs to be pushed in advance.
     */
    private static final File SYSTEM_EVENTS_FILE
            = new File("/data/data/org.mate/broadcast_actions.txt");

    private SystemActionParser() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    /**
     * Parses the supported system event actions from the specified broadcast_actions.txt file.
     *
     * @return Returns a list of system events that can be received by broadcast receivers.
     */
    public static List<String> parseSystemEventActions() {

        try(BufferedReader br = new BufferedReader(new FileReader(SYSTEM_EVENTS_FILE))) {

            List<String> systemEvents = new ArrayList<>();
            String line = br.readLine();

            while (line != null) {
                systemEvents.add(line);
                line = br.readLine();
            }

            return systemEvents;
        } catch (IOException e) {
            throw new IllegalStateException("Reading system events from broadcast_actions.txt failed!", e);
        }
    }
}
