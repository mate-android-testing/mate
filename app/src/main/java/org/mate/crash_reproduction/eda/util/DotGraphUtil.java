package org.mate.crash_reproduction.eda.util;

import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.model.Edge;
import org.mate.state.IScreenState;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DotGraphUtil {
    public static Map<String, String> toDotEdgeAttributeLookup(Map<Action, Double> actionProbabilities, IScreenState target, boolean mostLikelyEdge) {
        Set<Action> actionsLeadingToTarget = actionProbabilities.keySet().stream()
                .filter(action -> {
                    Set<Edge> edges = Registry.getUiAbstractionLayer().getGuiModel().getEdges(action);

                    return edges.size() > 0 && edges.stream().anyMatch(e -> e.getTarget().equals(target));
                })
                .collect(Collectors.toSet());

        Map<String, String> attributes = new HashMap<>();
        attributes.put("label", toLabelString(actionsLeadingToTarget, actionProbabilities));
        if (mostLikelyEdge) {
            attributes.put("color", "red");
        }

        return attributes;
    }

    public static Map<String, String> toDotNodeAttributeLookup(IScreenState state, Map<Action, Double> actionProbabilities) {
        Set<Action> actionsWithUnknownTarget = actionProbabilities.keySet().stream()
                .filter(action -> Registry.getUiAbstractionLayer().getGuiModel().getEdges(action).isEmpty())
                .collect(Collectors.toSet());

        Map<String, String> attributes = new HashMap<>();
        attributes.put("image", "\"results/pictures/" + Registry.getPackageName() + "/" + state.getId() + ".png\"");
        attributes.put("imagescale", "true");
        attributes.put("imagepos", "tc");
        attributes.put("labelloc", "b");
        attributes.put("height", "6");
        attributes.put("fixedsize", "true");
        attributes.put("shape", "square");
        attributes.put("xlabel", toLabelString(actionsWithUnknownTarget, actionProbabilities));

        return attributes;
    }

    private static String toLabelString(Set<Action> relevantActions, Map<Action, Double> actionProbabilities) {
        Action maxAction = actionProbabilities.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(NoSuchElementException::new);

        return "<" + relevantActions.stream()
                .filter(a -> actionProbabilities.get(a) > 0.01)
                .map(action -> {
                    String label = action.toShortString() + ": " + actionProbabilities.get(action);

                    if (action == maxAction) {
                        label = "<B>" + label + "</B>";
                    }
                    return label;
                })
                .collect(Collectors.joining("<BR/>")) + ">";
    }

    public static String toDotGraph(Map<String, Set<String>> edges,
                                    Map<String, Map<String, String>> nodeAttributeLookup,
                                    Map<String, Map<String, Map<String, String>>> edgeAttributeLookup) {
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("digraph D {");

        Stream.concat(edges.keySet().stream(), edges.values().stream().flatMap(Collection::stream)).distinct().forEach(node -> {
            stringJoiner.add(String.format("\"%s\" [%s]", node, getAttributeString(nodeAttributeLookup.get(node))));
        });

        for (Map.Entry<String, Set<String>> nodeEdges : edges.entrySet()) {
            String source = nodeEdges.getKey();

            for (String target : nodeEdges.getValue()) {
                Map<String, String> edgeAttributes = edgeAttributeLookup.getOrDefault(source, Collections.emptyMap()).get(target);
                stringJoiner.add(String.format("\"%s\" -> \"%s\" [%s]", source, target, getAttributeString(edgeAttributes)));
            }
        }
        stringJoiner.add("}");

        return stringJoiner.toString();
    }

    private static String getAttributeString(Map<String, String> attributes) {
        if (attributes == null) {
            return "";
        } else {
            return attributes.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", "));
        }
    }
}
