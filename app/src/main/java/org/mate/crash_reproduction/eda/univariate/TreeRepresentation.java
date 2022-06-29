package org.mate.crash_reproduction.eda.univariate;

import android.util.Pair;

import org.mate.Registry;
import org.mate.crash_reproduction.eda.util.DotGraphUtil;
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

public class TreeRepresentation implements ModelRepresentation {
    private final BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializeNode;
    private final Tree<TreeNodeContent> probabilityTree;

    public TreeRepresentation(BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializeNode) {
        this.initializeNode = initializeNode;
        this.probabilityTree = new Tree<>(initializeNode(Collections.emptyList(), Registry.getUiAbstractionLayer().getLastScreenState()));
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

        return probabilityTree.toDot(
                p -> p.state.getId(),
                p -> DotGraphUtil.toDotNodeAttributeLookup(p.state, p.actionProbabilities),
                (source, target) -> DotGraphUtil.toDotEdgeAttributeLookup(source.getContent().actionProbabilities, target.getContent().state, isOnMostLikelyPath.test(source, target))
        );
    }
}
