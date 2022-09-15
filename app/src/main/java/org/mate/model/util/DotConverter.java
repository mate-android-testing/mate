package org.mate.model.util;

import org.mate.MATE;
import org.mate.Properties;
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
    private static final String SCREENSHOTS_DIR = "/screenshots";

    private static final String GRAPH_DIR = "/graphs";

    // an internal counter to enumerate the dot files
    private static int counter = 0;

    // A collection of testcases for the final dot graph
    private static TestCase[] recordedCases;

    // List of 25 colors to color the test cases
    private static final String[] colors = {
            "tomato", "blue", "green", "orange", "yellow",
            "deeppink", "aqua", "lime", "chocolate", "darkviolet",
            "gray", "midnightblue", "goldrod", "lightpink", "darkred",
            "saddlebrown", "lightblue", "magenta", "olive", "yellowgreen",
            "darkslategray", "orangered", "plum", "darkcyan", "indigo"
    };

    private static String[] edges = {};

    /**
     * Converts the gui model to a dot file. The dot file is stored on the app-internal storage of
     * MATE, see {@link #DOT_DIR}.
     *
     * @param guiModel The gui model that should be converted.
     */
    public static void convertFinal(IGUIModel guiModel) {
        String dotFileName = "Final_Model";
        String dotFileContent = toDOT(guiModel, null);

        convert(dotFileName, dotFileContent);
    }

    /**
     * Converts the gui model to a dot file. The dot file is stored on the app-internal storage of
     * MATE, see {@link #DOT_DIR}. Highlights the given test case in the dot file.
     *
     * @param guiModel The gui model to be converted.
     * @param testCase The test case that should be highlighted.
     */
    public static void convertTestcase(IGUIModel guiModel, TestCase testCase) {
        String dotFileName = "Testcase" + counter + ".dot";
        String dotFileContent = toDOT(guiModel, testCase);
        counter++;

        convert(dotFileName, dotFileContent);
    }

    private static void convert(String dotFileName, String dotFileContent) {
        File dotDir = new File(DOT_DIR);

        if (!dotDir.exists()) {
            MATE.log("Creating dot folder succeeded: " + dotDir.mkdir());
        }

        File dotFile = new File(dotDir, dotFileName);

        try (Writer fileWriter = new FileWriter(dotFile)) {
            fileWriter.write(dotFileContent);
            fileWriter.flush();

            // Fetch and remove dot file from emulator!
            Registry.getEnvironmentManager().fetchDotGraphFromDevice(DOT_DIR, dotFileName);
            MATE.log("Fetch and remove dot file from " + dotFile.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't save dot file!", e);
        }
    }

    /**
     * Fetches the screenshots into the graph file.
     */
    public static void fetchScreenshots() {
        String dir = GRAPH_DIR + SCREENSHOTS_DIR;

        Registry.getEnvironmentManager().fetchScreenshots(dir, GRAPH_DIR);
    }

    /**
     * Takes a screenshot.
     *
     * @param state Name of the screenshot.
     */
    public static void takeScreenshot(String state) {
        if (Properties.DOT_WITH_SCREENSHOTS()) {
            String screenshotDir = GRAPH_DIR + SCREENSHOTS_DIR;
            File dir = new File(screenshotDir);

            if (!dir.exists()) {
                MATE.log("Creating screenshot folder succeeded: " + dir.mkdir());
            }

            Registry.getEnvironmentManager().takeScreenshot(screenshotDir, state);
        }
    }

    private static String toDot(IGUIModel guiModel) {
        StringBuilder builder = new StringBuilder();

        return builder.toString();
    }

    /**
     * Converts a gui model to a dot file for a certain test case.
     *
     * @param guiModel The gui model to be converted.
     * @param testCase The test case, that should be highlighted. If it's {@code null},
     *                 no edge gets highlighted.
     * @return Returns the dot conform gui model.
     */
    private static String toDOT(IGUIModel guiModel, TestCase testCase) {

        StringBuilder builder = new StringBuilder();
        builder.append("strict digraph g {\n");

        for (IScreenState state : guiModel.getStates()) {
            String stateId = state.getId();
            builder.append(String.format(Locale.getDefault(), "%s [label=\"", stateId));

            if (!Properties.DOT_WITH_SCREENSHOTS()) {
                builder.append(String.format(Locale.getDefault(), "%s\"", stateId));
            } else {
                builder.append("\", image=\".");
                builder.append(SCREENSHOTS_DIR);
                builder.append('/');
                builder.append(stateId);
                builder.append(".png\"");
                builder.append(", shape=\"box\"");
            }

            builder.append("]\n");
        }

        List<Action> actionList = testCase != null ? testCase.getEventSequence() : new ArrayList<>();

        for (Edge edge : guiModel.getEdges()) {
            builder.append(String.format(Locale.getDefault(), "%s -> %s [label=\"<%s>\"",
                    edge.getSource().getId(),
                    edge.getTarget().getId(),
                    edge.getAction().toShortString()));

            if (actionList.contains(edge.getAction())) {
                builder.append(", color = tomato, fontcolor = tomato");
            }

            if (Properties.DOT_WITH_SCREENSHOTS()) {
                builder.append(", fontsize=50, arrowsize=4, penwidth=5");
            }

            builder.append("];\n");
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
