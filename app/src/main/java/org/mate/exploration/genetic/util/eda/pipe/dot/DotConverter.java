package org.mate.exploration.genetic.util.eda.pipe.dot;

import org.mate.Registry;
import org.mate.exploration.genetic.util.eda.pipe.ppt.ApplicationStateTree;
import org.mate.exploration.genetic.util.eda.pipe.ppt.TreeNode;
import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;
import org.mate.utils.Tuple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts an {@link ApplicationStateTree} (PPT) to a DOT representation.
 */
public final class DotConverter {

    /**
     * The relative location where the screenshots of the gui states are stored.
     */
    private static final String SCREENSHOTS_DIR = "screenshots";

    /**
     * The relative location where the converted dots file are stored.
     */
    private static final String DOT_DIR = "ppt";

    private DotConverter() {
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * Retrieves the most likely path through the PPT, i.e the path with the highest action probabilities.
     *
     * @param ppt The given PPT.
     * @return Returns the most likely path through the PPT.
     */
    private static List<Tuple<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>>> getMostLikelyPath(final ApplicationStateTree ppt) {

        // TODO: This is not necessarily the real most likely path through the PPT, since there might
        //  multiple actions in a state having the same (highest) action probability.

        final List<Tuple<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>>> path = new LinkedList<>();

        TreeNode<ApplicationStateTree.ApplicationStateNode> prevNode = ppt.getRoot();
        Optional<TreeNode<ApplicationStateTree.ApplicationStateNode>> nextNode;

        do {
            final Action nextAction = prevNode.getContent().getActionWithBiggestProbability();

            final IScreenState nextState = prevNode.getContent().getActionToNextState().get(nextAction);
            nextNode = prevNode.getChild(node -> node.getState().equals(nextState));

            if (nextNode.isPresent()) {
                path.add(new Tuple<>(prevNode, nextNode.get()));
                prevNode = nextNode.get();
            }
        } while (nextNode.isPresent());

        return path;
    }

    /**
     * Converts the given PPT to a DOT representation.
     *
     * @param ppt The given PPT.
     * @param fileName The file to which the DOT converted representation of the PPT should be stored.
     */
    public static void toDot(final ApplicationStateTree ppt, final String fileName) {

        // Track which nodes represent the most likely path.
        final List<Tuple<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>>>
                mostLikelyPath = getMostLikelyPath(ppt);

        // Determines whether a node lies on the most likely path.
        final BiPredicate<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>> isOnMostLikelyPath
                = (source, target) -> mostLikelyPath.stream()
                .anyMatch(edge -> edge.getX() == source && edge.getY() == target);

        // Ignore showing actions that have a very low probability.
        final BiPredicate<ApplicationStateTree.ApplicationStateNode, Action> keepAction
                = (node, action) ->
                node.getActionWithBiggestProbability().equals(action)
                        || node.getActionProbabilities().getOrDefault(action, 0d) > 0.01;

        // Prints for the given action its action probability.
        final BiFunction<ApplicationStateTree.ApplicationStateNode, Action, String> printActionProb
                = (node, action) -> {

            final Double actionProbability = node.getActionProbabilities().get(action);

            String label = action.toShortString() + ": " + actionProbability;

            // label in bold if action with highest probability
            if (node.getActionWithBiggestProbability().equals(action)) {
                label = "<B>" + label + "</B>";
            }

            return label;
        };

        final StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("digraph D {");

        // Nodes are textually represented by the underlying screen state id.
        final Function<TreeNode<ApplicationStateTree.ApplicationStateNode>, String> nodeLabelFunction
                = node -> node.getContent().getState().getId();

        final Function<TreeNode<ApplicationStateTree.ApplicationStateNode>, String> nodeToEscapedLabelFunction
                = node -> '"' + nodeLabelFunction.apply(node) + " "
                + node.getParentList().map(nodeLabelFunction).collect(Collectors.joining(",")) + '"';

        // Defines the node attributes.
        final Function<TreeNode<ApplicationStateTree.ApplicationStateNode>, Map<String, String>>
                attributesFunction = node -> new HashMap<String, String>() {{
            put("image", "\"../" + SCREENSHOTS_DIR + "/" + node.getContent().getState().getId() + ".png\"");
            put("imagescale", "true");
            put("imagepos", "tc");
            put("labelloc", "b");
            put("height", "6");
            put("fixedsize", "true");
            put("shape", "square");
            // Show next to each node the action probabilities.
            put("xlabel", "<" + node.getContent().getActionProbabilities().keySet().stream()
                    .filter(action -> !node.getContent().getActionToNextState().containsKey(action))
                    .filter(action -> keepAction.test(node.getContent(), action))
                    .map(action -> printActionProb.apply(node.getContent(), action))
                    .collect(Collectors.joining("<BR/>")) + ">"
            );
        }};

        final BiFunction<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>, Map<String, String>>
                edgeAttributeFunction = (source, target) -> new HashMap<String, String>() {{
            put("label", "<" + source.getContent().getActionToNextState().entrySet().stream()
                    .filter(e -> e.getValue().equals(target.getContent().getState()))
                    .filter(e -> keepAction.test(source.getContent(), e.getKey()))
                    .map(e -> printActionProb.apply(source.getContent(), e.getKey()))
                    .collect(Collectors.joining("<BR/>")) + ">");

            if (isOnMostLikelyPath.test(source, target)) {
                put("color", "red");
            }
        }};

        final Function<Map<String, String>, String> attributesToString
                = attributes -> (attributes == null || attributes.isEmpty()) ? ""
                : " [" + attributes.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(", ")) + "]";

        final Queue<TreeNode<ApplicationStateTree.ApplicationStateNode>> nodes = new LinkedList<>();
        nodes.add(ppt.getRoot());

        while (!nodes.isEmpty()) {

            final TreeNode<ApplicationStateTree.ApplicationStateNode> node = nodes.poll();

            final Map<String, String> attributes = attributesFunction.apply(node);

            if (!attributes.containsKey("label")) {
                attributes.put("label", '"' + nodeLabelFunction.apply(node) + '"');
            }

            stringJoiner.add(nodeToEscapedLabelFunction.apply(node) + attributesToString.apply(attributes));

            for (final TreeNode<ApplicationStateTree.ApplicationStateNode> child : node.getChildren()) {
                nodes.add(child);
                stringJoiner.add(nodeToEscapedLabelFunction.apply(node) + " -> "
                        + nodeToEscapedLabelFunction.apply(child)
                        + attributesToString.apply(edgeAttributeFunction.apply(node, child)));
            }
        }

        stringJoiner.add("}");

        final String dotContent = stringJoiner.toString();
        Registry.getEnvironmentManager().writeFile(DOT_DIR + "/" + fileName, dotContent);
    }
}
