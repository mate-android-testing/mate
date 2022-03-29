package org.mate.model.util;

import org.mate.MATE;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * Provides a conversion method to export a {@link IGUIModel} to a dot file.
 */
public final class DotConverter {

    // the location where dot file should be stored
    private static final String DOT_DIR = "/data/data/org.mate/gui-model";

    // an internal counter to enumerate the dot files
    private static int counter = 0;

    /**
     * Converts a gui model to a dot file. The dot file is stored on the app-internal storage of
     * MATE, see {@link #DOT_DIR}.
     *
     * @param guiModel The gui model that should be converted.
     */
    public static void convert(IGUIModel guiModel) {

        File dotDir = new File(DOT_DIR);
        if (!dotDir.exists()) {
            MATE.log("Creating dot folder succeeded: " + dotDir.mkdir());
        }

        File dotFile = new File(dotDir, "GUIModel" + counter + ".dot");
        MATE.log_acc("Dot: ");
        MATE.log_acc(toDOT(guiModel));

        try (Writer fileWriter = new FileWriter(dotFile)) {
            fileWriter.write(toDOT(guiModel));
            fileWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't save dot file!", e);
        }
        counter++;
    }

    /**
     * Converts a gui model to a dot file.
     *
     * @param guiModel The gui model to be converted.
     * @return Returns the dot conform gui model.
     */
    private static String toDOT(IGUIModel guiModel) {

        StringBuilder builder = new StringBuilder();
        builder.append("strict digraph g {\n");

        for (IScreenState state : guiModel.getStates()) {
            String stateId = state.getId();
            builder.append(String.format(Locale.getDefault(), "%s [label=\"%s\"]\n",
                    stateId, stateId));
        }

        for (Edge edge : guiModel.getEdges()) {
            builder.append(String.format(Locale.getDefault(), "%s -> %s [label=<%s>];\n",
                    edge.getSource().getId(),
                    edge.getTarget().getId(),
                    edge.getAction().toShortString()));
        }

        builder.append("}\n");
        return builder.toString();
    }

    /**
     * Describes the different conversion options.
     */
    public enum Option {

        /**
         * There should be no conversion at all.
         */
        NONE,

        /**
         * Only the final gui model should be converted.
         */
        ONLY_FINAL_MODEL,

        /**
         * The conversion should take place after each test case and at the end.
         */
        ALL;
    }
}
