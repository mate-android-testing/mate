package org.mate.model.fsm.qbe;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.StartAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.fsm.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Provides a serialization mechanism for the ELTS.
 * <p>
 * The serialization uses the JSON format and compresses the resulting string with GZIP.
 */
public final class ELTSSerializer {

    /**
     * The directory where the ELTS should be serialized to.
     */
    private static final String OUT_DIR = "/data/data/org.mate/transition_systems";

    /**
     * The file name of the serialized ELTS.
     */
    private static final String FILE_NAME = "transition_system.gz";

    /**
     * The directory where the ELTS should be serialized to.
     */
    private final File directory;

    /**
     * Initialises the ELTSSerializer.
     */
    public ELTSSerializer() {
        this.directory = new File(OUT_DIR);
    }

    /**
     * Serializes the given ELTS.
     *
     * @param elts The ELTS that should be serialized.
     */
    public void serialize(final ELTS elts) {
        requireNonNull(elts);

        if (elts.getActions().stream().map(Object::hashCode).distinct().count() != elts.getActions().size()) {
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
            serializeTransitionSystem(elts, writer);
        } catch (IOException e) {
            MATE.log_error(e.getMessage());
            e.printStackTrace();
        }
    }

    private String actionToString(Action action) {
        if (action instanceof StartAction) {
            final StartAction a = (StartAction) action;
            return "{\"startAction\":{}}";
        }

        if (action instanceof UIAction) {
            UIAction a = (UIAction) action;
            return String.format("{\"uiAction\":{\"actionType\":\"%s\",\"activityName\":\"%s\",\"hash\":%d}}",
                    a.getActionType(), a.getActivityName(), a.hashCode());
        }

        throw new UnsupportedOperationException("Unsupported action type: " + action);
    }

    private Map<Action, Integer> serializeQBEActions(final Set<Action> actions,
                                                     final PrintWriter writer) {
        final Map<Action, Integer> map = new HashMap<>(actions.size());

        int index = 0;
        writer.write("{");
        for (final Action action : actions) {
            if (index > 0) writer.write(",");
            writer.printf("\"%d\":%s", index, actionToString(action));
            map.put(action, index);
            ++index;
        }

        writer.write("}");
        return map;
    }

    private Map<QBEState, Integer> serializeQBEStates(final Set<QBEState> states,
                                                      final Map<Action, Integer> actionIndexes,
                                                      final PrintWriter writer) {

        final Map<QBEState, Integer> map = new HashMap<>(states.size());
        int index = 0;
        writer.write("{");

        for (final QBEState state : states) {
            if (state == ELTS.VIRTUAL_ROOT_STATE)
                continue;

            if (index > 0) {
                writer.write(",");
            }

            writer.printf("\"%d\":{\"actions\":[", index);
            boolean firstEntry = true;

            for (final Action action : state.getActions()) {
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

    private void serializeTransitionRelation(final QBETransition tr,
                                             final Map<QBEState, Integer> stateIndexes,
                                             final Map<Action, Integer> actionIndexes,
                                             final PrintWriter writer) {
        writer.printf("{\"from\":%d,\"trigger\":%d,\"to\":%d,\"actionResult\":\"%s\"}",
                stateIndexes.get(tr.getSource()), actionIndexes.get(tr.getAction()),
                stateIndexes.get(tr.getTarget()), tr.getActionResult());
    }

    private void serializeTransitionSystem(final ELTS elts, final PrintWriter writer) {
        elts.removeUnreachableStates();

        writer.write("{\"actions\":");
        final Map<Action, Integer> actionIndexes = serializeQBEActions(elts.getActions(), writer);
        writer.write(",\"states\":");
        final Set<QBEState> states = elts.getStates().stream().map(s -> (QBEState) s).collect(toSet());
        final Map<QBEState, Integer> stateIndexes = serializeQBEStates(states, actionIndexes, writer);
        writer.printf(",\"initialState\":%d,\"transitionRelations\":[", stateIndexes.get(ELTS.VIRTUAL_ROOT_STATE));

        boolean firstEntry = true;

        for (Transition tr : elts.getTransitions()) {
            if (!firstEntry) {
                writer.write(",");
            } else {
                firstEntry = false;
            }
            serializeTransitionRelation((QBETransition) tr, stateIndexes, actionIndexes, writer);
        }
        writer.write("]}");
    }
}
