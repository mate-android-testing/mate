package org.mate.exploration.qlearning.qbe.transitionSystem;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.MATE;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEAction;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class TransitionSystemSerializer {

    private final File directory;


    public TransitionSystemSerializer(final String directory) {
        this.directory = new File(Objects.requireNonNull(directory));
    }


    public void serialize(final TransitionSystem<QBEState, QBEAction> ts, final String fileName) {
        Objects.requireNonNull(ts);
        final File file = new File(directory, Objects.requireNonNull(fileName));

        if (!directory.exists()) {
            MATE.log("Creating transition system folder succeeded: " + directory.mkdirs());
        }

        if (file.isDirectory()) {
            MATE.log_error("File " + file + " exists, but is a directory");
            return;
        }

        try (final PrintWriter writer = new PrintWriter(new GZIPOutputStream(new FileOutputStream(file)), false)) {
            serializeTransitionSystem(ts, writer);
        } catch (IOException e) {
            MATE.log_error(e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<QBEAction, Integer> serializeQBEActions(final Set<QBEAction> actions, final PrintWriter writer) {
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

    private Map<QBEState, Integer> serializeQBEStates(final Set<QBEState> states, final Map<QBEAction, Integer> actionIndexes, final PrintWriter writer) {
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

    private void serializeTransitionRelation(final TransitionRelation<QBEState, QBEAction> tr, final Map<QBEState, Integer> stateIndexes, final Map<QBEAction, Integer> actionIndexes, final PrintWriter writer) {
        writer.printf("{\"from\":%d,\"trigger\":%d,\"to\":%d,\"actionResult\":\"%s\"}", stateIndexes.get(tr.from), actionIndexes.get(tr.trigger), stateIndexes.get(tr.to), tr.actionResult);
    }


    private void serializeTransitionSystem(final TransitionSystem<QBEState, QBEAction> ts, final PrintWriter writer) {
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
