package org.mate.exploration.qlearning.qbe.transition_system;

import org.mate.MATE;
import org.mate.exploration.qlearning.qbe.abstractions.action.QBEAction;
import org.mate.exploration.qlearning.qbe.abstractions.state.QBEState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Provides a serialization mechanism for the ELTS.
 */
public final class TransitionSystemSerializer {

    /**
     * The directory where the ELTS should be serialized to.
     */
    private static final String TRANSITION_SYSTEM_DIR = "/data/data/org.mate/transition_systems";

    /**
     * The file name of the serialized ELTS.
     */
    private static final String FILE_NAME = "transition_system.gz";

    /**
     * The directory where the ELTS should be serialized to.
     */
    private final File directory;

    /**
     * Initialises the transition system serializer.
     */
    public TransitionSystemSerializer() {
        this.directory = new File(TRANSITION_SYSTEM_DIR);
    }

    /**
     * Serializes the given ELTS.
     *
     * @param ts The transition system that should be serialized.
     */
    public void serialize(final TransitionSystem<QBEState, QBEAction> ts) {

        Objects.requireNonNull(ts);

        if (ts.getActions().stream().map(QBEAction::hashCode).distinct().count() != ts.getActions().size()) {
            MATE.log_warn("Found hash collision while serializing.");
            MATE.log_warn("The transition system will not be serialized!");
            return;
        }

        if (!directory.exists()) {
            MATE.log("Creating transition system folder succeeded: " + directory.mkdirs());
        }

        final File file = new File(directory, FILE_NAME);

        if (file.isDirectory()) {
            MATE.log_error("File " + file + " exists, but is a directory");
            return;
        }

        try (final PrintWriter writer
                     = new PrintWriter(new GZIPOutputStream(new FileOutputStream(file)), false)) {
            serializeTransitionSystem(ts, writer);
        } catch (IOException e) {
            MATE.log_error(e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<QBEAction, Integer> serializeQBEActions(final Set<QBEAction> actions,
                                                        final PrintWriter writer) {

        final Map<QBEAction, Integer> map = new HashMap<>(actions.size());
        int index = 0;
        writer.write("{");

        for (final QBEAction action : actions) {
            if (index > 0) {
                writer.write(",");
            }
            writer.printf("\"%d\":%s", index, action);
            map.put(action, index);
            ++index;
        }

        writer.write("}");
        return map;
    }

    private Map<QBEState, Integer> serializeQBEStates(final Set<QBEState> states,
                                                      final Map<QBEAction, Integer> actionIndexes,
                                                      final PrintWriter writer) {

        final Map<QBEState, Integer> map = new HashMap<>(states.size());
        int index = 0;
        writer.write("{");

        for (final QBEState state : states) {

            if (index > 0) {
                writer.write(",");
            }

            writer.printf("\"%d\":{\"actions\":[", index);
            boolean firstEntry = true;

            for (final QBEAction action : state.getActions()) {
                if (!firstEntry) {
                    writer.write(",");
                } else {
                    firstEntry = false;
                }
                writer.write(actionIndexes.get(action).toString());
            }

            writer.write("],\"featureMap\":{");
            firstEntry = true;

            for (final Map.Entry<String, Integer> entry : state.getFeatureMap().entrySet()) {
                if (!firstEntry) {
                    writer.write(",");
                } else {
                    firstEntry = false;
                }
                writer.printf("\"%s\":\"%d\"", entry.getKey(), entry.getValue());
            }

            writer.write("}}");
            map.put(state, index);
            ++index;
        }

        writer.write("}");
        return map;
    }

    private void serializeTransitionRelation(final TransitionRelation<QBEState, QBEAction> tr,
                                             final Map<QBEState, Integer> stateIndexes,
                                             final Map<QBEAction, Integer> actionIndexes,
                                             final PrintWriter writer) {
        writer.printf("{\"from\":%d,\"trigger\":%d,\"to\":%d,\"actionResult\":\"%s\"}",
                stateIndexes.get(tr.from), actionIndexes.get(tr.trigger), stateIndexes.get(tr.to),
                tr.actionResult);
    }

    private void serializeTransitionSystem(final TransitionSystem<QBEState, QBEAction> ts,
                                           final PrintWriter writer) {

        ts.removeUnreachableStates();

        writer.write("{\"actions\":");
        final Map<QBEAction, Integer> actionIndexes = serializeQBEActions(ts.getActions(), writer);
        writer.write(",\"states\":");
        final Map<QBEState, Integer> stateIndexes = serializeQBEStates(ts.getStates(), actionIndexes, writer);
        writer.printf(",\"initialState\":%d,\"transitionRelations\":[", stateIndexes.get(ts.getInitialState()));

        boolean firstEntry = true;

        for (TransitionRelation<QBEState, QBEAction> tr : ts.getTransitions()) {
            if (!firstEntry) {
                writer.write(",");
            } else {
                firstEntry = false;
            }
            serializeTransitionRelation(tr, stateIndexes, actionIndexes, writer);
        }
        writer.write("]}");
    }
}
