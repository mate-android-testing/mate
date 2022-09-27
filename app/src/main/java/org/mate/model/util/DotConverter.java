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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
    private static final Map<Integer, TestCase> recordedCases = new HashMap<>();

    // List of 25 colors to color the test cases
    private static final String[] colors = {
            "tomato", "blue", "green", "orange", "yellow",
            "deeppink", "aqua", "lime", "chocolate", "darkviolet",
            "gray", "midnightblue", "goldrod", "lightpink", "darkred",
            "saddlebrown", "lightblue", "magenta", "olive", "yellowgreen",
            "darkslategray", "orangered", "plum", "darkcyan", "indigo"
    };

    /**
     * Converts the gui model to a dot file. The dot file is stored on the app-internal storage of
     * MATE, see {@link #DOT_DIR}.
     *
     * @param guiModel The gui model that should be converted.
     */
    public static void convertFinal(IGUIModel guiModel) {
        assert guiModel != null;

        String dotFileName = "Final_Model.dot";
        String dotFileContent = toDOT(guiModel);

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
        assert guiModel != null;
        assert testCase != null;

        String dotFileName = "Testcase" + counter + ".dot";
        String dotFileContent = toDOT(guiModel, testCase);

        if (Arrays.stream(Properties.TESTCASES_HIGHLIGHTED()).anyMatch(x -> x == counter)) {
            recordedCases.put(counter, testCase);
        }

        convert(dotFileName, dotFileContent);

        counter++;
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

    private static String toDOT(IGUIModel guiModel) {
        assert guiModel != null;

        StringBuilder builder = new StringBuilder();

        /*
         * The keys of recordedCases are saved in this list, so that the order of the key set
         * doesn't change between the two functions toDotEdges(...) and toDotLegend(...)
         */
        List<Integer> keys = new ArrayList<>(recordedCases.keySet());

        builder.append("strict digraph g {\n");
        builder.append(toDotNodes(guiModel));
        builder.append(toDotEdgesFinal(guiModel, keys));
        builder.append(toDotFinalLegend(keys));
        builder.append("}\n");

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
        builder.append(toDotNodes(guiModel));
        builder.append(toDotEdges(guiModel, testCase));
        builder.append("}\n");

        return builder.toString();
    }

    /**
     * Creates a string representation of the screen states for a dot graph.
     *
     * @param guiModel the gui model containing all screen states.
     * @return A string representation for the nodes of the dot graph.
     */
    private static String toDotNodes(IGUIModel guiModel) {
        StringBuilder builder = new StringBuilder();

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

        return builder.toString();
    }

    /**
     * Creates a string representation of the edges for a dot graph of the final model.
     *
     * @param guiModel the gui model containing all edges
     * @param keys A list of keys from {@link DotConverter#recordedCases}.
     * @return A string representation of all
     */
    private static String toDotEdgesFinal(IGUIModel guiModel, List<Integer> keys) {
        StringBuilder builder = new StringBuilder();
        Set<Action>[] actionSet = new Set[keys.size()];

        for (int i = 0;  i < actionSet.length; i++) {
            TestCase testCase = recordedCases.get(keys.get(i));
            assert testCase != null;

            actionSet[i] = new HashSet<>(testCase.getEventSequence());
        }

        for (Edge edge : guiModel.getEdges()) {
            String color = getEdgeColor(edge, actionSet);

            builder.append(toDotEdge(edge, color, "black"));
        }

        return builder.toString();
    }

    /**
     * Creates a string representation of all edges for a given gui model.
     * The edges for a highlighting a certain test case.
     *
     * @param guiModel contains the edges.
     * @param testCase contains the edges which will be highlighted.
     * @return A string representation for the edges in the dot graph.
     */
    private static String toDotEdges(IGUIModel guiModel, TestCase testCase) {
        StringBuilder builder = new StringBuilder();
        Set<Action> actionSet = new HashSet<>(testCase.getEventSequence());
        MATE.log("Get Edges set: " + guiModel.getEdges().size());

        for (Edge edge : guiModel.getEdges()) {
            String edgeString = "";

            if (actionSet.contains(edge.getAction())) {
                edgeString = toDotEdge(edge, "tomato", "tomato");
            } else {
                edgeString = toDotEdge(edge, "black", "black");
            }

            builder.append(edgeString);
        }

        return builder.toString();
    }

    /**
     * Creates a string representation of a single edge in the dot graph.
     *
     * @param edge The edge which is converted into a string representation.
     * @param edgeColor The color of the edge in the dot graph.
     * @param fontcolor The color of the tag assigned to the edge.
     * @return The string representation of the edge.
     */
    private static String toDotEdge(Edge edge, String edgeColor, String fontcolor) {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format(Locale.getDefault(), "%s -> %s [label=\"<%s>\"",
                edge.getSource().getId(),
                edge.getTarget().getId(),
                edge.getAction().toShortString()));

        builder.append(", color = \"");
        builder.append(edgeColor);
        builder.append("\", fontcolor = \"");
        builder.append(fontcolor);
        builder.append('\"');

        if (Properties.DOT_WITH_SCREENSHOTS()) {
            builder.append(", fontsize=50, arrowsize=4, penwidth=5");
        }

        builder.append("];\n");

        return builder.toString();
    }

    /**
     * Determines the colors an edge contains for the final model.
     * An edge can be used by more than one testcase, therefore this method can highlight it with
     * different colors.
     *
     * @param edge The edge for which the color is calculated.
     * @param actionsSets A set of actions. If the set contains the respective edge, the edge is
     *                    colored by a color assigned to the actions.
     * @return A string containing all colors for this edge.
     */
    private static String getEdgeColor(Edge edge, Set<Action>[] actionsSets) {
        StringBuilder colorOfEdge = new StringBuilder();

        for (int i = 0; i < actionsSets.length; i++) {
            if (actionsSets[i].contains(edge.getAction())) {
                String color = i < colors.length ? colors[i] : "black";

                colorOfEdge.append(color);
                colorOfEdge.append(':');
            }
        }

        if (colorOfEdge.length() == 0) {
            return "black";
        } else {
            colorOfEdge.deleteCharAt(colorOfEdge.length() - 1);

            // Without this the dot graph draws multiple edges instead of one
            colorOfEdge.append(":black;0.001");
        }

        return colorOfEdge.toString();
    }

    /**
     * Creates the string representation for the legend in the final dot graph
     *
     * @param keys The keys for the highlighted testcases. The legend shows the color of those.
     * @return The legend as a string representation for dot.
     */
    private static String toDotFinalLegend(List<Integer> keys) {
        StringBuilder builder = new StringBuilder();

        builder.append("subgraph cluster_01 { \n \t label = \"Legend\";\n \tshape = rectangle;\n" +
                "\tcolor = black;\n\t");

        for (Integer integer : keys) {
            builder.append(integer);
            builder.append("1 [style=invis]\n\t");
            builder.append(integer);
            builder.append("2 [style=invis]\n\t");
        }

        for (int i = 0; i < recordedCases.size(); i++) {
            int id = keys.get(i);
            String color = i < colors.length ? colors[i] : "black";

            builder.append(id);
            builder.append("1 -> ");
            builder.append(id);
            builder.append("2 [label=\"Testcase ");
            builder.append(id);
            builder.append("\", color=\"");
            builder.append(color);
            builder.append("\"] \n\t");
        }

        builder.append("\t }");

        return builder.toString();
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
