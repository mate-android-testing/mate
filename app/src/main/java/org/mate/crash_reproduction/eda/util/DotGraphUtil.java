package org.mate.crash_reproduction.eda.util;

import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class DotGraphUtil {
    public static Map<String, String> toDotEdgeAttributeLookup(Map<Action, Double> actionProbabilities, Set<Map.Entry<Action, Double>> relevantActions, boolean mostLikelyEdge) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("label", toLabelString(relevantActions, actionProbabilities));
        if (mostLikelyEdge) {
            attributes.put("color", "red");
        }

        return attributes;
    }

    public static Map<String, String> toDotNodeAttributeLookup(IScreenState state, Set<Map.Entry<Action, Double>> relevantActions, Map<Action, Double> actionProbabilities) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("image", "\"results/pictures/" + Registry.getPackageName() + "/" + state.getId() + ".png\"");
        attributes.put("imagescale", "true");
        attributes.put("imagepos", "tc");
        attributes.put("labelloc", "b");
        attributes.put("height", "6");
        attributes.put("fixedsize", "true");
        attributes.put("shape", "square");
        attributes.put("xlabel", toLabelString(relevantActions, actionProbabilities));

        return attributes;
    }

    public static String toLabelString(Set<Map.Entry<Action, Double>> relevantActions, Map<Action, Double> actionProbabilities) {
        Action maxAction = actionProbabilities.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(NoSuchElementException::new);

        return "<" + relevantActions.stream()
                .filter(a -> a.getValue() > 0.01)
                .map(action -> {
                    String label = action.getKey().toShortString() + ": " + action.getValue();

                    if (action.getKey() == maxAction) {
                        label = "<B>" + label + "</B>";
                    }
                    return label;
                })
                .collect(Collectors.joining("<BR/>")) + ">";
    }

    public static String getAttributeString(Map<String, String> attributes) {
        if (attributes == null) {
            return "";
        } else {
            return attributes.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", "));
        }
    }
}
