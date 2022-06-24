package org.mate.crash_reproduction.eda.univariate;

import android.util.Pair;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.crash_reproduction.eda.IDistributionModel;
import org.mate.crash_reproduction.eda.util.Tree;
import org.mate.crash_reproduction.eda.util.TreeNode;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Organizes variables in tree and updates similar to PBIL
 */
public class PIPE implements IDistributionModel {
    private final IFitnessFunction<TestCase> fitnessFunction;
    private final double pPromisingAction;
    private final double learningRate;
    private final double epsilon;
    private final double clr;
    private final double pEl;
    private final double pMutation;
    private final double mutationRate;
    private final Tree<PIPETreeNodeContent> probabilityTree = new Tree<>(initializeNode(Registry.getUiAbstractionLayer().getLastScreenState()));

    private IChromosome<TestCase> elitist;

    public PIPE(IFitnessFunction<TestCase> fitnessFunction, double pPromisingAction, double learningRate, double epsilon, double clr, double pEl, double pMutation, double mutationRate) {
        this.pPromisingAction = pPromisingAction;
        if (fitnessFunction.isMaximizing()) {
            throw new IllegalArgumentException("PIPE needs a minimizing fitness function!");
        }
        this.fitnessFunction = fitnessFunction;
        this.learningRate = learningRate;
        this.epsilon = epsilon;
        this.clr = clr;
        this.pEl = pEl;
        this.pMutation = pMutation;
        this.mutationRate = mutationRate;
    }

    @Override
    public void update(Collection<IChromosome<TestCase>> population) {
        List<IChromosome<TestCase>> sortedPopulation = population.stream()
                .sorted(Comparator.comparingDouble(fitnessFunction::getNormalizedFitness))
                .collect(Collectors.toList());

        IChromosome<TestCase> best = sortedPopulation.get(0);
        IChromosome<TestCase> worst = sortedPopulation.get(sortedPopulation.size() - 1);

        best.getValue().setNickName("Best");
        if (elitist == null || fitnessFunction.getFitness(best) < fitnessFunction.getFitness(elitist)) {
            elitist = best;
            elitist.getValue().setNickName("Elitist");
        }

        // Elitist learning does not lead to a new population so we repeatedly apply it
        while (Randomness.getRnd().nextDouble() < pEl) {
            MATE.log("Running elitist learning");
            adaptPPTTowards(elitist);
            pptPruning();
        }

        // Generation-based learning
        adaptPPTTowards(best);
//        adaptPPTAwayFrom(worst, best); // Added by me
        pptMutation(best);
        pptPruning();
    }

    private PIPETreeNodeContent initializeNode(IScreenState state) {
        Map<Action, Double> probabilities = new HashMap<>();
        Set<Action> promisingActions = new HashSet<>(Registry.getUiAbstractionLayer().getPromisingActions(state));

        // The PIPE paper differentiates between terminals and functions, which is why they define
        // a probability P_T for picking a terminal.
        // We differentiate between promising actions and normal ones
        double l = state.getActions().size();

        // P_j(I) = P_T / l, where I elem promising actions
        // P_j(I) = (1 - P_T) / l, where I not elem promising actions
        for (Action action : state.getActions()) {
            probabilities.put(action, promisingActions.contains(action) ? pPromisingAction / l : (1 - pPromisingAction) / l);
        }

        return new PIPETreeNodeContent(state, probabilities);
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        Registry.getUiAbstractionLayer().resetApp();

        TestCase testCase = TestCase.newInitializedTestCase();
        testCase.setNickName("PIPE");
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            TreeNode<PIPETreeNodeContent> state = probabilityTree.getRoot();

            for (int actionsCount = 0; actionsCount < Properties.MAX_NUMBER_EVENTS(); actionsCount++) {
                Action action = Randomness.randomIndexWithProbabilities(state.getContent().actionProbabilities);

                if (!testCase.updateTestCase(action, actionsCount)) {
                    return chromosome;
                }

                IScreenState currentScreenState = Registry.getUiAbstractionLayer().getLastScreenState();
                state.getContent().updateActionToNextState(action, currentScreenState);
                TreeNode<PIPETreeNodeContent> finalState = state;
                state = state.getChild(s -> s.state.equals(currentScreenState))
                        .orElseGet(() -> finalState.addChild(initializeNode(currentScreenState)));
            }
        } finally {
            FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
            CoverageUtils.logChromosomeCoverage(chromosome);
            testCase.finish();
        }
        return chromosome;
    }

    // ================== (3) Learning from Population =========================
    private void adaptPPTTowards(IChromosome<TestCase> best) {
        increaseOfBest(best);
        renormalizeOthers(best.getValue());
    }

    private void adaptPPTAwayFrom(IChromosome<TestCase> worst, IChromosome<TestCase> best) {
        decreaseOfWorst(worst, best);
        renormalizeOthers(worst.getValue());
    }

    private double probability(TestCase testCase) {
        // PIPE paper 4.1
        double probProduct = 1;
        TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(testCase);

        while (testCaseModelIterator.hasNext()) {
            NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

            probProduct *= nodeWithPickedAction.getProbabilityOfAction();
        }

        return probProduct;
    }

    private double targetProbability(IChromosome<TestCase> bestTestcase) {
        // PIPE paper 4.2
        double probBestTestcase = probability(bestTestcase.getValue());
        double fitElitist = fitnessFunction.getFitness(elitist);
        double fitBest = fitnessFunction.getFitness(bestTestcase);

        return probBestTestcase + (1 - probBestTestcase) * learningRate * ((epsilon + fitElitist) / (epsilon + fitBest));
    }

    private double worseTargetProbability(IChromosome<TestCase> worstTestcase) {
        double probWorstTestCase = probability(worstTestcase.getValue());
        double fitElitist = fitnessFunction.getFitness(elitist);
        double fitWorst = fitnessFunction.getFitness(worstTestcase);

        return probWorstTestCase - probWorstTestCase * learningRate * ((epsilon + fitWorst) / (epsilon + fitElitist));
    }

    private void increaseOfBest(IChromosome<TestCase> bestTestcase) {
        // PIPE paper 4.3
        double pTarget = targetProbability(bestTestcase);

        while (probability(bestTestcase.getValue()) < pTarget) {
            TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(bestTestcase.getValue());

            while (testCaseModelIterator.hasNext()) {
                NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

                double probBefore = nodeWithPickedAction.getProbabilityOfAction();
                nodeWithPickedAction.putProbabilityOfAction(probBefore + clr * learningRate * (1 - probBefore));
            }
        }
    }

    private void decreaseOfWorst(IChromosome<TestCase> worstTestcase, IChromosome<TestCase> best) {
        double pTarget = worseTargetProbability(worstTestcase);

        while (probability(worstTestcase.getValue()) > pTarget) {
            TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(worstTestcase.getValue());
            TestCaseModelIterator bestTestCaseModelIterator = new TestCaseModelIterator(best.getValue());

            while (testCaseModelIterator.hasNext()) {
                NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

                // ignore common prefix
                if (!bestTestCaseModelIterator.hasNext() || !bestTestCaseModelIterator.next().equals(nodeWithPickedAction)) {
                    double probBefore = nodeWithPickedAction.getProbabilityOfAction();
                    nodeWithPickedAction.putProbabilityOfAction(probBefore - clr * learningRate * probBefore);
                }
            }
        }
    }

    private void renormalizeOthers(TestCase bestTestcase) {
        TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(bestTestcase);

        while (testCaseModelIterator.hasNext()) {
            NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();
            double probBest = nodeWithPickedAction.getProbabilityOfAction();
            double sum = nodeWithPickedAction.node.getContent().actionProbabilities.values().stream().mapToDouble(d -> d).sum();

            for (Map.Entry<Action, Double> entry : nodeWithPickedAction.node.getContent().actionProbabilities.entrySet()) {
                if (!entry.getKey().equals(nodeWithPickedAction.action)) {
                    double probBefore = entry.getValue();
                    entry.setValue(probBefore * (1 - (1 - sum) / (probBest - sum)));
                }
            }
        }
    }

    // ===================== (4) Mutation of Prototype Tree =======================
    private void pptMutation(IChromosome<TestCase> bestTestcase) {

        if (Randomness.getRnd().nextDouble() < pMutation) {
            // TODO How to adapt formula 4.4 for the P_MP?, for now we simply use pMutation

            int z = 1;
            double pMP = pMutation / (z * Math.sqrt(bestTestcase.getValue().getEventSequence().size()));

            TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(bestTestcase.getValue());

            while (testCaseModelIterator.hasNext()) {
                NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

                for (Map.Entry<Action, Double> entry : nodeWithPickedAction.node.getContent().actionProbabilities.entrySet()) {
                    if (Randomness.getRnd().nextDouble() < pMP) {
                        double probBefore = entry.getValue();
                        // PIPE paper 4.5
                        entry.setValue(probBefore + mutationRate * (1 - probBefore));
                    }
                }

                // Normalization
                double sum = nodeWithPickedAction.node.getContent().actionProbabilities.values().stream().mapToDouble(d -> d).sum();
                for (Map.Entry<Action, Double> entry : nodeWithPickedAction.node.getContent().actionProbabilities.entrySet()) {
                    entry.setValue(entry.getValue() / sum);
                }
            }
        }
    }

    // ======================= (5) Prototype Tree Pruning =========================
    private void pptPruning() {
        // TODO Remove subtrees which are very unlikely to be reached, ignoring for now, since this is only a performance/memory optimization
    }

    // ============================================================================

    /**
     * Walks through the model tree like the testcase has before
     */
    private class TestCaseModelIterator implements Iterator<NodeWithPickedAction> {
        private TreeNode<PIPETreeNodeContent> currentNode = probabilityTree.getRoot();
        private final Iterator<Action> actionIterator;
        private final Iterator<IScreenState> stateIterator;

        private TestCaseModelIterator(TestCase testCase) {
            this.actionIterator = testCase.getEventSequence().iterator();
            this.stateIterator = testCase.getStateSequence().iterator();

            // This skips the root node
            if (!stateIterator.next().equals(probabilityTree.getRoot().getContent().state)) {
                throw new IllegalStateException("Testcase does not start at root...");
            }
        }

        @Override
        public boolean hasNext() {
            return actionIterator.hasNext();
        }

        @Override
        public NodeWithPickedAction next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Forgot to call hasNext?");
            }

            Action currentAction = actionIterator.next();
            NodeWithPickedAction nodeWithPickedAction = new NodeWithPickedAction(currentNode, currentAction);

            if (stateIterator.hasNext()) {
                IScreenState nextState = stateIterator.next();
                currentNode = currentNode.getChild(s -> s.state.equals(nextState)).orElseThrow(() -> new IllegalStateException());
            } else if (actionIterator.hasNext()) {
                throw new IllegalStateException("Number of actions should at most be off by one");
            }

            return nodeWithPickedAction;
        }
    }

    private List<Pair<TreeNode<PIPETreeNodeContent>, TreeNode<PIPETreeNodeContent>>> getMostLikelyPath() {
        List<Pair<TreeNode<PIPETreeNodeContent>, TreeNode<PIPETreeNodeContent>>> path = new LinkedList<>();
        TreeNode<PIPETreeNodeContent> prevNode = probabilityTree.getRoot();
        Optional<TreeNode<PIPETreeNodeContent>> nextNode;

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

    private static class NodeWithPickedAction {
        private final TreeNode<PIPETreeNodeContent> node;
        private final Action action;

        private NodeWithPickedAction(TreeNode<PIPETreeNodeContent> node, Action action) {
            this.node = node;
            this.action = action;
        }

        public double getProbabilityOfAction() {
            return node.getContent().actionProbabilities.get(action);
        }

        public void putProbabilityOfAction(double newProb) {
            node.getContent().actionProbabilities.put(action, newProb);
        }
    }

    @Override
    public String toString() {
        List<Pair<TreeNode<PIPETreeNodeContent>, TreeNode<PIPETreeNodeContent>>> mostLikelyPath = getMostLikelyPath();
        BiPredicate<TreeNode<PIPETreeNodeContent>, TreeNode<PIPETreeNodeContent>> isOnMostLikelyPath = (source, target) -> mostLikelyPath.stream().anyMatch(edge -> edge.first == source && edge.second == target);

        BiFunction<PIPETreeNodeContent, Action, String> printActionProb = (node, action) -> {
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
                            .map(a -> printActionProb.apply(p, a))
                            .collect(Collectors.joining("<BR/>")) + ">"
                    );
                }},
                (source, target) -> new HashMap<String, String>() {{
                    put("label", "<" + source.getContent().actionToNextState.entrySet().stream()
                            .filter(e -> e.getValue().equals(target.getContent().state))
                            .map(e -> printActionProb.apply(source.getContent(), e.getKey()))
                            .collect(Collectors.joining("<BR/>")) + ">");

                    if (isOnMostLikelyPath.test(source, target)) {
                        put("color", "red");
                    }
                }}
        );
    }

    private static class PIPETreeNodeContent {
        private final IScreenState state;
        private final Map<Action, Double> actionProbabilities;
        private final Map<Action, IScreenState> actionToNextState = new HashMap<>();

        private PIPETreeNodeContent(IScreenState state, Map<Action, Double> actionProbabilities) {
            this.state = state;
            this.actionProbabilities = actionProbabilities;
        }

        public void updateActionToNextState(Action action, IScreenState nextState) {
            actionToNextState.put(action, nextState);
        }

        public Action getActionWithBiggestProb() {
            return actionProbabilities.entrySet().stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElseThrow(IllegalStateException::new);
        }
    }
}
