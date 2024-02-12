package org.mate.exploration.genetic.util.eda.pipe.dot;

import org.mate.Registry;
import org.mate.exploration.genetic.fitness.ActionFitnessFunctionWrapper;
import org.mate.exploration.genetic.util.eda.pipe.ppt.ApplicationStateTree;
import org.mate.exploration.genetic.util.eda.pipe.ppt.TreeNode;
import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;
import org.mate.utils.Tuple;

import java.util.Comparator;
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
     * Retrieves the action with the highest action probability.
     *
     * @return Returns the action with the highest assigned probability if possible, otherwise
     *          {@code null} is returned.
     */
    private static Action getActionWithBiggestProbability(final Map<Action, Float> actionProbabilities) {
        return actionProbabilities.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                // States that don't belong to the AUT do not have any outgoing actions.
                .orElse(null);
    }

    /**
     * Retrieves the most likely (highest possible rewarded) path through the PPT, i.e., the path
     * through the nodes with the highest action probabilities. If the action with the highest probability
     * in the current node have not been taken so far the path ends at this location. In summary, this
     * is not the path through the actions with the highest probabilities that has been actually
     * traversed. In fact, this path might not even exist in the current PPT.
     *
     * @param ppt The given PPT.
     * @param fitnessFunction The currently active fitness function (target).
     * @return Returns the most likely path through the PPT.
     */
    private static List<Tuple<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>>> getMostLikelyPath(
                        final ApplicationStateTree ppt,
                        final ActionFitnessFunctionWrapper fitnessFunction) {

        // TODO: This is not necessarily the real most likely path through the PPT, since there might
        //  multiple actions in a state having the same (highest) action probability.

        final List<Tuple<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>>> path = new LinkedList<>();

        // NOTE: The virtual root node might be connected to multiple real root nodes. The current
        // implementation picks the transition to the lastly inserted real root node.
        TreeNode<ApplicationStateTree.ApplicationStateNode> prevNode = ppt.getRoot();
        Optional<TreeNode<ApplicationStateTree.ApplicationStateNode>> nextNode;

        do {
            final Action nextAction = getActionWithBiggestProbability(
                    prevNode.getContent().getActionProbabilities().get(fitnessFunction.getIndex()));

            if (nextAction == null) {
                // We reached a state that doesn't belong to the AUT and thus doesn't have any
                // outgoing actions.
                break;
            }

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
     * @param fitnessFunction The currently active target.
     * @param fileName The file to which the DOT converted representation of the PPT should be stored.
     */
    public static void toDot(final ApplicationStateTree ppt,
                             final ActionFitnessFunctionWrapper fitnessFunction,
                             final String fileName) {

        // Track which nodes represent the most likely path.
        final List<Tuple<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>>>
                mostLikelyPath = getMostLikelyPath(ppt, fitnessFunction);

        // Determines whether a node lies on the most likely path.
        final BiPredicate<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>> isOnMostLikelyPath
                = (source, target) -> mostLikelyPath.stream()
                .anyMatch(edge -> edge.getX().equals(source) && edge.getY().equals(target));

        // Ignore showing actions that have a very low probability.
        final BiPredicate<ApplicationStateTree.ApplicationStateNode, Action> keepAction
                = (node, action) -> {
            final Map<Action, Float> actionProbabilities
                    = node.getActionProbabilities().get(fitnessFunction.getIndex());
            final Action mostLikelyAction = getActionWithBiggestProbability(actionProbabilities);
            return action.equals(mostLikelyAction)
                    || actionProbabilities.getOrDefault(action, 0f) > 0.01f;
        };

        // Prints for the given action its action probability.
        final BiFunction<ApplicationStateTree.ApplicationStateNode, Action, String> printActionProb
                = (node, action) -> {

            final Map<Action, Float> actionProbabilities
                    = node.getActionProbabilities().get(fitnessFunction.getIndex());
            final Action mostLikelyAction = getActionWithBiggestProbability(actionProbabilities);
            final double actionProbability = actionProbabilities.get(action);
            String label = action.toShortString() + ": " + String.format("%.3f", actionProbability);

            // label in bold if action with highest probability
            if (action.equals(mostLikelyAction)) {
                label = "<B>" + label + "</B>";
            }

            return label;
        };

        final StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("digraph D {");
        // Shows all labels.
        stringJoiner.add("forcelabels=true;");
        // Controls how much horizontal space should be between two nodes.
        stringJoiner.add("nodesep=10.0;");

        // Nodes are textually represented by the underlying screen state id.
        final Function<TreeNode<ApplicationStateTree.ApplicationStateNode>, String> nodeLabelFunction
                = node -> node.getContent().getState().getId();

        final Function<TreeNode<ApplicationStateTree.ApplicationStateNode>, String> nodeToEscapedLabelFunction
                = node -> '"' + nodeLabelFunction.apply(node) + " "
                + node.getParentList().map(nodeLabelFunction).collect(Collectors.joining(",")) + '"';

        // Defines the node attributes.
        final Function<TreeNode<ApplicationStateTree.ApplicationStateNode>, Map<String, String>>
                attributesFunction = node -> new HashMap<String, String>() {{
            // TODO: There is no image for the virtual root node.
            put("image", "\"../" + SCREENSHOTS_DIR + "/" + node.getContent().getState().getId() + ".png\"");
            put("imagescale", "true");
            put("imagepos", "tc");
            // NOTE: The label is unfortunately placed inside the image. Since the menu bar at the
            // bottom is black, the label wouldn't be visible at all. Since there is no easy option to
            // place it outside the image, we highlight the label in red and place it in the center.
            put("labelloc", "c");
            put("label", "<<font color=\"red\"><font point-size=\"40\"><b>"
                    + nodeLabelFunction.apply(node) + "</b></font></font>>");
            put("width", "8");
            put("height", "6");
            put("fixedsize", "true");
            put("shape", "square");
            // Show next to each node the action probabilities.
            put("xlabel", "<" + node.getContent().getActionProbabilities().get(fitnessFunction.getIndex())
                    .keySet().stream()
                    // Only show not yet triggered actions.
                    .filter(action -> !node.getContent().getActionToNextState().containsKey(action))
                    // TODO: Display only the best k actions since the label is getting somewhat too big.
                    // Skip actions with a very low action probability.
                    .filter(action -> keepAction.test(node.getContent(), action))
                    .map(action -> printActionProb.apply(node.getContent(), action))
                    .collect(Collectors.joining("<BR/>")) + ">"
            );
        }};

        // Defines the edge attributes.
        final BiFunction<TreeNode<ApplicationStateTree.ApplicationStateNode>,
                TreeNode<ApplicationStateTree.ApplicationStateNode>, Map<String, String>>
                edgeAttributeFunction = (source, target) -> new HashMap<String, String>() {{
            put("label", "<" + source.getContent().getActionToNextState().entrySet().stream()
                    // Ensure that there is actually an edge from the source to the target node.
                    .filter(entry -> entry.getValue().equals(target.getContent().getState()))
                    // Omit actions with a very low probability.
                    .filter(entry -> keepAction.test(source.getContent(), entry.getKey()))
                    .map(entry -> printActionProb.apply(source.getContent(), entry.getKey()))
                    .collect(Collectors.joining("<BR/>")) + ">");
            // Controls the minimal length of an edge. Actually this should be dependent on the
            // number of outgoing actions rather than a fixed value. This allows us to better separate
            // nodes from each other vertically.
            put("minlen", "10");

            if (isOnMostLikelyPath.test(source, target)) {
                put("color", "red");
            }
        }};

        final Function<Map<String, String>, String> attributesToString
                = attributes -> (attributes == null || attributes.isEmpty()) ? ""
                : " [" + attributes.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(", ")) + "]";

        final Queue<TreeNode<ApplicationStateTree.ApplicationStateNode>> nodes = new LinkedList<>();
        nodes.add(ppt.getRoot());

        while (!nodes.isEmpty()) {

            final TreeNode<ApplicationStateTree.ApplicationStateNode> node = nodes.poll();
            final Map<String, String> attributes = attributesFunction.apply(node);
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
