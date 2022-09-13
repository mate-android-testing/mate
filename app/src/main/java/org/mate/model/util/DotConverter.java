package org.mate.model.util;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides a conversion method to export a {@link IGUIModel} to a dot file.
 */
public final class DotConverter {

    // the location where dot file should be stored
    private static final String DOT_DIR = "/data/data/org.mate/gui-model";

    // the location where the screenshots are stored
    private static final String SCSH_DIR = "/screenshots";

    // the location of the screenshots on the device
    public static final String DEVICE_SCSH_DIR = "/dot";

    // an internal counter to enumerate the dot files
    private static int counter = 0;

    /**
     * Converts the gui model to a dot file. The dot file is stored on the app-internal storage of
     * MATE, see {@link #DOT_DIR}.
     *
     * @param guiModel The gui model that should be converted.
     */
    public static void convert(IGUIModel guiModel) {
        convert(guiModel, null);
    }

    /**
     * Converts the gui model to a dot file. The dot file is stored on the app-internal storage of
     * MATE, see {@link #DOT_DIR}. Highlights the given test case in the dot file.
     *
     * @param guiModel The gui model to be converted.
     * @param testCase The test case that should be highlighted.
     */
    public static void convert(IGUIModel guiModel, TestCase testCase) {
        String dotFileName = "GUIModel" + counter + ".dot";
        File dotDir = new File(DOT_DIR);

        if (!dotDir.exists()) {
            MATE.log("Creating dot folder succeeded: " + dotDir.mkdir());
        }

        File dotFile = new File(dotDir, dotFileName);

        try (Writer fileWriter = new FileWriter(dotFile)) {
            fileWriter.write(toDOT(guiModel, testCase));
            fileWriter.flush();

            // Fetch and remove dot file from emulator!
            Registry.getEnvironmentManager().fetchDotGraphFromDevice(DOT_DIR, dotFileName);
            MATE.log("Fetch and remove dot file from " + dotFile.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't save dot file!", e);
        }
        counter++;
    }

    public static void fetchScreenshots() {
        String targetDir = DOT_DIR + SCSH_DIR;
        File dir = new File(targetDir);

        if (!dir.exists()) {
            MATE.log("Creating screenshot folder succeeded: " + dir.mkdir());
        }

        Registry.getEnvironmentManager().fetchScreenshots(DEVICE_SCSH_DIR, targetDir);
    }

    /**
     * Converts a gui model to a dot file.
     *
     * @param guiModel The gui model to be converted.
     * @param testCase The test case, that should be highlighted. If it's {@code null},
     *                 no edge gets highlighted.
     * @return Returns the dot conform gui model.
     */
    private static String toDOT(IGUIModel guiModel, TestCase testCase) {

        // TODO: Avoid overlapping labels!

        StringBuilder builder = new StringBuilder();
        builder.append("strict digraph g {\n");

        for (IScreenState state : guiModel.getStates()) {
            String stateId = state.getId();
            builder.append(String.format(Locale.getDefault(), "%s [label=\"%s\"]\n",
                    stateId, stateId));
        }

        List<Action> actionList = testCase != null ? testCase.getEventSequence() : new ArrayList<>();

        for (Edge edge : guiModel.getEdges()) {
            builder.append(String.format(Locale.getDefault(), "%s -> %s [label=\"<%s>\"",
                    edge.getSource().getId(),
                    edge.getTarget().getId(),
                    edge.getAction().toDotString()));

            if (actionList.contains(edge.getAction())) {
                builder.append(", color = red, fontcolor = red");
            }

            builder.append("];\n");
        }

        builder.append("}\n");

        MATE.log_acc("BUILD SOME GRAPH STRING!");
        MATE.log_acc(builder.toString());

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
