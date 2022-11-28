package org.mate.model.util;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.model.TestCase;
import org.mate.model.fsm.FSMModel;
import org.mate.state.IScreenState;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a conversion method to export a {@link IGUIModel} to a dot file.
 */
public final class DotConverter {

    // the location where dot files should be stored
    private static final String DOT_DIR = "/data/data/org.mate/gui-model";

    // the location where the screenshots are stored
    private static final String SCREENSHOTS_DIR = "screenshots";

    // an internal counter to enumerate the dot files
    private static int counter = 0;

    // a collection of testcases for the final dot graph
    private static final List<TestCase> RECORDED_TEST_CASES = new ArrayList<>();

    // a subset of possible colors, see https://graphviz.org/doc/info/colors.html
    private static final String[] COLORS = {
            "tomato", "blue", "green", "orange", "yellow",
            "deeppink", "aqua", "lime", "chocolate", "darkviolet",
            "gray", "midnightblue", "goldenrod", "lightpink", "darkred",
            "saddlebrown", "lightblue", "magenta", "olive", "yellowgreen",
            "darkslategray", "orangered", "plum", "darkcyan", "indigo",
            "darkblue", "darksalmon", "fuchsia", "peru", "sienna"
    };

    /**
     * Converts the gui model to a dot file. The dot file is stored on the app-internal storage of
     * MATE, see {@link #DOT_DIR}.
     *
     * @param guiModel The gui model that should be converted.
     */
    public static void convertFinal(final IGUIModel guiModel) {
        String dotFileName = "model.dot";
        String dotFileContent = toDOTFinal(guiModel);
        convert(dotFileName, dotFileContent);
    }

    /**
     * Converts the gui model to a dot file. The dot file is stored on the app-internal storage of
     * MATE, see {@link #DOT_DIR}. Highlights the given test case in the dot file.
     *
     * @param guiModel The gui model to be converted.
     * @param testCase The test case that should be highlighted.
     */
    public static void convertTestcase(final IGUIModel guiModel, final TestCase testCase) {

        String dotFileName = "Testcase" + counter + ".dot";
        String dotFileContent = toDOT(guiModel, testCase);

        // record test cases to highlight them in the final dot graph
        RECORDED_TEST_CASES.add(testCase);

        convert(dotFileName, dotFileContent);
        counter++;
    }

    /**
     * Converts the gui model to a dot file.
     *
     * @param dotFileName The name of the file.
     * @param dotFileContent The content of the dot file.
     */
    private static void convert(final String dotFileName, final String dotFileContent) {

        MATE.log("Converting gui model to dot file!");

        final File dotDir = new File(DOT_DIR);

        if (!dotDir.exists()) {
            MATE.log("Creating dot folder succeeded: " + dotDir.mkdir());
        }

        final File dotFile = new File(dotDir, dotFileName);

        try (Writer fileWriter = new FileWriter(dotFile)) {
            fileWriter.write(dotFileContent);
            fileWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write to dot file!", e);
        }

        // fetch and remove dot file from emulator
        Registry.getEnvironmentManager().fetchDotGraph(DOT_DIR, dotFileName);
    }

    /**
     * Converts the final gui model into a string representation of a dot graph.
     *
     * @param guiModel the model which is converted into a state diagram.
     * @return A string representation of the dot graph.
     */
    private static String toDOTFinal(IGUIModel guiModel) {

        final StringBuilder builder = new StringBuilder();

        builder.append("digraph g {\n");
        builder.append(toDotNodes(guiModel));

        if (RECORDED_TEST_CASES.isEmpty()) {
            builder.append(toDotEdges(guiModel, null));
        } else {
            // highlight the recorded test cases in a distinct color
            builder.append(toDotEdgesFinal(guiModel));
            builder.append(toDotFinalLegend());
        }

        builder.append("}\n");
        return builder.toString();
    }

    /**
     * Converts a gui model to a dot file and highlights the given test case.
     *
     * @param guiModel The gui model to be converted.
     * @param testCase The test case that should be highlighted or {@code null} if no highlighting
     *         should take place.
     * @return Returns the dot graph conforming to the gui model.
     */
    private static String toDOT(IGUIModel guiModel, TestCase testCase) {
        final StringBuilder builder = new StringBuilder();
        builder.append("digraph g {\n");
        builder.append(toDotNodes(guiModel));
        builder.append(toDotEdges(guiModel, testCase));
        builder.append("}\n");
        return builder.toString();
    }

    /**
     * Converts the screen states of the given gui model to dot graph nodes.
     *
     * @param guiModel The gui model containing all the screen states.
     * @return Returns a string representation of the nodes in the dot graph.
     */
    private static String toDotNodes(final IGUIModel guiModel) {

        final StringBuilder builder = new StringBuilder();

        for (IScreenState state : guiModel.getStates()) {

            String stateId = state.getId();
            builder.append(stateId);
            builder.append(" [label=\"");

            if (Properties.DOT_GRAPH_WITH_SCREENSHOTS()) {

                if (stateId.equals(FSMModel.VIRTUAL_ROOT_STATE_ID)) {
                    builder.append("Root\", ");
                    builder.append("fontsize=250");
                } else {
                    builder.append("\", "); // empty label for screenshots
                    builder.append("image=\"../");
                    builder.append(SCREENSHOTS_DIR);
                    builder.append('/');
                    builder.append(stateId);
                    builder.append(".png\", ");
                    builder.append("shape=\"box\"");
                }
            } else {
                // no screenshots, use labels instead
                if (stateId.equals(FSMModel.VIRTUAL_ROOT_STATE_ID)) {
                    builder.append("Root");
                } else {
                    builder.append(stateId);
                }

                builder.append("\"");
            }

            builder.append("]\n");
        }

        return builder.toString();
    }

    /**
     * Converts the edges of the final gui model to dot graph edges.
     *
     * @param guiModel The gui model containing all the edges.
     * @return Returns a string representation for the edges in the dot graph.
     */
    private static String toDotEdgesFinal(final IGUIModel guiModel) {

        // pre-compute once the action sets per test case
        final List<Set<Action>> actionSets = new ArrayList<>(RECORDED_TEST_CASES.size());

        for (int i = 0; i < RECORDED_TEST_CASES.size(); i++) {
            final TestCase testCase = RECORDED_TEST_CASES.get(i);
            actionSets.add(new HashSet<>(testCase.getActionSequence()));
        }

        final StringBuilder builder = new StringBuilder();
        int edgeCounter = 0;

        for (Edge edge : guiModel.getEdges()) {

            final String source = edge.getSource().getId();
            final String target = edge.getTarget().getId();
            final String color = getEdgeColor(edge, actionSets);

            builder.append(toDotEdge(source, target, edge.getAction().toShortString(), color,
                    "lightgray", edgeCounter));
            edgeCounter++;
        }

        return builder.toString();
    }

    /**
     * Converts the edges of the given gui model to dot graph edges. If a test case is supplied,
     * its actions will be highlighted in the dot graph.
     *
     * @param guiModel The gui model containing all the edges.
     * @param testCase A test case that should be highlighted or {@code null} otherwise.
     * @return Returns a string representation for the edges in the dot graph.
     */
    private static String toDotEdges(final IGUIModel guiModel, final TestCase testCase) {

        final StringBuilder builder = new StringBuilder();

        final Set<Action> actionSet = testCase == null
                ? new HashSet<>() : new HashSet<>(testCase.getActionSequence());

        final Map<String, EdgeRepresentation> dotEdges = new HashMap<>();
        int edgeCounter = 0;

        for (Edge edge : guiModel.getEdges()) {
            if (actionSet.contains(edge.getAction())) {
                // highlight the edges of the given test case
                addToMap(dotEdges, edge, "tomato", "tomato");
            } else {
                addToMap(dotEdges, edge, "black", "lightgray");
            }
        }

        // create for each edge its representation
        for (String edge : dotEdges.keySet()) {

            final EdgeRepresentation representation = dotEdges.get(edge);

            final String actions = representation.getActions().toString();
            final String source = representation.getSource();
            final String target = representation.getTarget();
            final String edgeColor = representation.getEdgeColor();
            final String fillColor = representation.getFillColor();

            builder.append(toDotEdge(source, target, actions, edgeColor, fillColor, edgeCounter));
            edgeCounter++;
        }

        return builder.toString();
    }

    /**
     * Adds an edge to the edge representation map with the specified layout.
     *
     * @param edgeRepresentationMap A mapping from an edge to its graphical representation.
     * @param edge The edge to be saved.
     * @param edgeColor The color of the edge.
     * @param fillColor The fill color of the edge.
     */
    private static void addToMap(final Map<String, EdgeRepresentation> edgeRepresentationMap,
                                 final Edge edge, final String edgeColor, final String fillColor) {

        final String edgeRepresentationId = edge.getSource().getId() + "/" + edge.getTarget().getId()
                + "/" + edgeColor + "/" + fillColor;

        // check whether a partially filled representation of the edge already exists
        if (edgeRepresentationMap.containsKey(edgeRepresentationId)) {
            final StringBuilder actions = edgeRepresentationMap.get(edgeRepresentationId).getActions();
            actions.append("\\n<");
            actions.append(edge.getAction().toShortString());
            actions.append('>');
        } else {
            // new edge
            final String action = '<' + edge.getAction().toShortString() + '>';
            final String source = edge.getSource().getId();
            final String target = edge.getTarget().getId();
            final EdgeRepresentation representation
                    = new EdgeRepresentation(action, source, target, edgeColor, fillColor);
            edgeRepresentationMap.put(edgeRepresentationId, representation);
        }
    }

    /**
     * Creates a dot representation of a single edge from the gui model. As a side effect, a dot
     * node is created for the actions.
     *
     * @param source The id of the source node.
     * @param target The id of the target node.
     * @param actions The string representation of all actions that are attached to the edge.
     * @param edgeColor The edge color.
     * @param fillColor The fill color for the edge.
     * @param edgeId An unique id of the edge.
     * @return Returns the dot representation of the edge.
     */
    private static String toDotEdge(final String source, final String target, final String actions,
                                    final String edgeColor, final String fillColor, final int edgeId) {

        // create an additional dot node for the actions
        final String edgeNode = "E" + edgeId;
        final StringBuilder builder = new StringBuilder();

        builder.append(edgeNode);
        builder.append(" [label=\"");
        builder.append(actions);
        builder.append("\", fontcolor = \"black\", shape=\"box\", color=\"white\", " +
                "style = \"filled\", fillcolor = \"");
        builder.append(fillColor);
        builder.append('\"');

        if (Properties.DOT_GRAPH_WITH_SCREENSHOTS()) {
            builder.append(", fontsize=50");
        }

        builder.append("];\n");

        // create an edge from source to actions node to target
        builder.append(source);
        builder.append("->");
        builder.append(edgeNode);
        builder.append(" [label=\"\", arrowhead=none, color=\"");
        builder.append(edgeColor);
        builder.append('\"');

        if (Properties.DOT_GRAPH_WITH_SCREENSHOTS()) {
            builder.append(", fontsize=50, arrowsize=4, penwidth=5, minlen=5");
        }

        builder.append("];\n");

        builder.append(edgeNode);
        builder.append("->");
        builder.append(target);
        builder.append(" [label=\"\", color=\"");
        builder.append(edgeColor);
        builder.append('\"');

        if (Properties.DOT_GRAPH_WITH_SCREENSHOTS()) {
            builder.append(", fontsize=50, arrowsize=4, penwidth=5, minlen=5");
        }

        builder.append("];\n");

        return builder.toString();
    }

    /**
     * Determines the color for the given edge in the final dot graph. An edge can be traversed by
     * multiple test cases, thus it might be highlighted in multiple colors.
     *
     * @param edge The edge for which the color should be determined.
     * @param actionSets Contains for every test case (list entry) the set of actions.
     * @return Returns the edge color.
     */
    private static String getEdgeColor(final Edge edge, final List<Set<Action>> actionSets) {

        final StringBuilder colorOfEdge = new StringBuilder();

        // check for each test case whether the edge was traversed
        for (int i = 0; i < actionSets.size(); i++) {
            if (actionSets.get(i).contains(edge.getAction())) {
                // highlight edge with color corresponding to i-th test case
                final int colorIndex = i % COLORS.length;
                final String color = COLORS[colorIndex];
                colorOfEdge.append(color);
                colorOfEdge.append(':'); // there may follow further colors
            }
        }

        if (colorOfEdge.length() == 0) {
            // the edge was not covered by any test, e.g. the virtual start action
            return "black";
        } else {
            /*
            * We need to add at least a single fraction, otherwise multiple lines are drawn. Since
            * we can't determine how many colors the edge will have in advance, we add the color
            * black with an invisible fraction such that there will be a equal color fraction for
            * the remaining colors, see https://graphviz.org/docs/attrs/color/
             */
            colorOfEdge.append("black;0.001");
        }

        return colorOfEdge.toString();
    }

    /**
     * Creates the legend for the final dot graph.
     *
     * @return Returns the legend for the final dot graph.
     */
    private static String toDotFinalLegend() {

        final StringBuilder builder = new StringBuilder();

        builder.append("subgraph cluster_01 { \n \t label = \"Legend\";\n \tshape = rectangle;\n" +
                "\tcolor = black;\n\t");

        // create for each legend entry a pair of invisible start and end nodes
        for (int i = 0; i < RECORDED_TEST_CASES.size(); i++) {
            builder.append(i);
            builder.append("1 [style=invis]\n\t");
            builder.append(i);
            builder.append("2 [style=invis]\n\t");
        }

        // create for each test case an entry in the legend (a mapping to a color)
        for (int i = 0; i < RECORDED_TEST_CASES.size(); i++) {

            final int colorIndex = i % COLORS.length;
            final String color = COLORS[colorIndex];

            builder.append(i);
            builder.append("1 -> ");
            builder.append(i);
            builder.append("2 [label=\"Testcase ");
            builder.append(i);
            builder.append("\", color=\"");
            builder.append(color);
            builder.append("\"] \n\t");
        }

        builder.append("\t }");

        return builder.toString();
    }

    /**
     * Takes a screenshot of the given state and saves it in the app folder of the AUT.
     *
     * @param state The state id serving as the screenshot name.
     * @param packageName The package name of the AUT.
     */
    public static void takeScreenshot(String state, String packageName) {
        Registry.getEnvironmentManager().takeScreenshot(packageName, state);
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

    /**
     * Maintains the dot representation of an edge.
     */
    private static class EdgeRepresentation {

        /**
         * The actions that are attached to the edge.
         */
        private final StringBuilder actions;

        /**
         * The source node of the edge.
         */
        private final String source;

        /**
         * The target node of the edge.
         */
        private final String target;

        /**
         * The edge color.
         */
        private final String edgeColor;

        /**
         * The fill color of the edge.
         */
        private final String fillColor;

        private EdgeRepresentation(String action, String source, String target, String color,
                                   String fillColor) {
            this.actions = new StringBuilder(action);
            this.source = source;
            this.target = target;
            this.edgeColor = color;
            this.fillColor = fillColor;
        }

        public StringBuilder getActions() {
            return actions;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public String getEdgeColor() {
            return edgeColor;
        }

        public String getFillColor() {
            return fillColor;
        }
    }
}
