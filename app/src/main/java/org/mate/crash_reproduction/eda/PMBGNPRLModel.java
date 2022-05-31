package org.mate.crash_reproduction.eda;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.Randomness;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PMBGNPRLModel<Node extends UIAction> implements IDistributionModel<Node> {
    private final QLearning<IScreenState, Node> qLearning = new QLearning<>(0.9, 0.1);

    @Override
    public void update(Set<IChromosome<TestCase>> node) {
        for (IChromosome<TestCase> testCaseIChromosome : node) {
            TestCase testCase = testCaseIChromosome.getValue();
            for (int i = 0; i < testCase.getEventSequence().size() - 1 && i < testCase.getStateSequence().size() - 1; i++) {
                Node actionT = (Node) testCase.getEventSequence().get(i);
                IScreenState stateT = testCase.getStateSequence().get(i);
                IScreenState stateTp1 = testCase.getStateSequence().get(i + 1);
                Node actionTp1 = (Node) testCase.getEventSequence().get(i + 1);

                double reward = reward(testCase, stateT, actionT, stateTp1, actionTp1);
                qLearning.update(stateT, actionT, stateTp1, actionTp1, reward);
            }
        }
    }

    private double reward(TestCase testCase, IScreenState stateT, Node actionT, IScreenState stateTp1, Node actionTp1) {
        double reward = 1;
        if (!stateT.equals(stateTp1)) {
            reward++;
        }
        if (!actionT.equals(actionTp1)) {
            reward++;
        }
        return reward;
    }

    @Override
    public Optional<Node> drawNextNode(Node startNode) {
        Map<Node, Double> probabilities = getProbabilities();
        if (probabilities.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(Randomness.randomIndexWithProbabilities(probabilities));
        }
    }

    @Override
    public Optional<Node> getNextBestNode(Node startNode) {
        return getProbabilities().entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(edge -> (Node) edge.getKey());
    }

    @Override
    public Set<Node> getPossibleNodes(Node startNode) {
        return qLearning.getPossibleAction(Registry.getUiAbstractionLayer().getLastScreenState());
    }

    private Map<Node, Double> getProbabilities() {
        return qLearning.getDistribution(Registry.getUiAbstractionLayer().getLastScreenState());
    }
}
