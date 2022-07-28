package org.mate.crash_reproduction.eda.representation;

import android.util.Pair;

import org.mate.Registry;
import org.mate.crash_reproduction.eda.util.Tree;
import org.mate.crash_reproduction.eda.util.TreeNode;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class TreeRepresentation implements IModelRepresentation {
    private final BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializeNode;
    private Tree<TreeNodeContent> probabilityTree;

    public TreeRepresentation(BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializeNode) {
        this.initializeNode = initializeNode;
        this.probabilityTree = new Tree<>(initializeNode(Collections.emptyList(), Registry.getUiAbstractionLayer().getLastScreenState()));
    }

    @Override
    public void resetProbabilities() {
        probabilityTree = new Tree<>(probabilityTree.getRoot().getContent());
    }

    @Override
    public ModelRepresentationIterator getIterator() {
        return new RepresentationIterator();
    }

    private static class TreeNodeContent {
        private final IScreenState state;
        private final Map<Action, Double> actionProbabilities;
        private final Map<Action, IScreenState> actionToNextState = new HashMap<>();

        private TreeNodeContent(IScreenState state, Map<Action, Double> actionProbabilities) {
            this.state = state;
            this.actionProbabilities = actionProbabilities;
        }

        private void updateActionToNextState(Action action, IScreenState nextState) {
            actionToNextState.put(action, nextState);
        }

        private Action getActionWithBiggestProb() {
            return actionProbabilities.entrySet().stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElseThrow(IllegalStateException::new);
        }
    }

    public class RepresentationIterator implements ModelRepresentationIterator {
        private TreeNode<TreeNodeContent> state = probabilityTree.getRoot();

        @Override
        public Map<Action, Double> getActionProbabilities() {
            return state.getContent().actionProbabilities;
        }

        @Override
        public IScreenState getState() {
            return state.getContent().state;
        }

        @Override
        public void updatePosition(TestCase testCase, Action action, IScreenState currentScreenState) {
            state.getContent().updateActionToNextState(action, currentScreenState);
            state = state.getChild(s -> s.state.equals(currentScreenState))
                    .orElseGet(() -> state.addChild(initializeNode(testCase.getEventSequence(), currentScreenState)));
        }

        @Override
        public void updatePositionImmutable(IScreenState currentScreenState) {
            state = state.getChild(s -> s.state.equals(currentScreenState)).orElseThrow(IllegalStateException::new);
        }
    }

    private TreeNodeContent initializeNode(List<Action> prevActions, IScreenState state) {
        return new TreeNodeContent(state, initializeNode.apply(prevActions, state));
    }

    private List<Pair<TreeNode<TreeNodeContent>, TreeNode<TreeNodeContent>>> getMostLikelyPath() {
        List<Pair<TreeNode<TreeNodeContent>, TreeNode<TreeNodeContent>>> path = new LinkedList<>();
        TreeNode<TreeNodeContent> prevNode = probabilityTree.getRoot();
        Optional<TreeNode<TreeNodeContent>> nextNode;

        do {
            Action nextAction = prevNode.getContent().getActionWithBiggestProb();

            IScreenState nextState = prevNode.getContent().actionToNextState.get(nextAction);
            nextNode = prevNode.getChild(n -> n.state.equals(nextState));

            if (nextNode.isPresent()) {
                path.add(Pair.create(prevNode, nextNode.get()));
                prevNode = nextNode.get();
            }
        } while (nextNode.isPresent());

        return path;
    }

    @Override
    public String toString() {
        List<Pair<TreeNode<TreeNodeContent>, TreeNode<TreeNodeContent>>> mostLikelyPath = getMostLikelyPath();
        BiPredicate<TreeNode<TreeNodeContent>, TreeNode<TreeNodeContent>> isOnMostLikelyPath = (source, target) -> mostLikelyPath.stream().anyMatch(edge -> edge.first == source && edge.second == target);

        BiPredicate<TreeNodeContent, Action> keepAction = (node, action) ->
                node.getActionWithBiggestProb().equals(action)
                || node.actionProbabilities.getOrDefault(action, 0D) > 0.01;

        BiFunction<TreeNodeContent, Action, String> printActionProb = (node, action) -> {
            Double prob = node.actionProbabilities.get(action);

            String label = action.toShortString() + ": " + prob;

            if (node.getActionWithBiggestProb().equals(action)) {
                label = "<B>" + label + "</B>";
            }

            return label;
        };

        return probabilityTree.toDot(
                p -> p.state.getId(),
                p -> new HashMap<String, String>() {{
                    put("image", "\"results/pictures/" + Registry.getPackageName() + "/" + p.state.getId() + ".png\"");
                    put("imagescale", "true");
                    put("imagepos", "tc");
                    put("labelloc", "b");
                    put("height", "6");
                    put("fixedsize", "true");
                    put("shape", "square");
                    put("xlabel", "<" + p.actionProbabilities.keySet().stream()
                            .filter(a -> !p.actionToNextState.containsKey(a))
                            .filter(a -> keepAction.test(p, a))
                            .map(a -> printActionProb.apply(p, a))
                            .collect(Collectors.joining("<BR/>")) + ">"
                    );
                }},
                (source, target) -> new HashMap<String, String>() {{
                    put("label", "<" + source.getContent().actionToNextState.entrySet().stream()
                            .filter(e -> e.getValue().equals(target.getContent().state))
                            .filter(e -> keepAction.test(source.getContent(), e.getKey()))
                            .map(e -> printActionProb.apply(source.getContent(), e.getKey()))
                            .collect(Collectors.joining("<BR/>")) + ">");

                    if (isOnMostLikelyPath.test(source, target)) {
                        put("color", "red");
                    }
                }}
        );
    }
}
