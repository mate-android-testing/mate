package org.mate.crash_reproduction.eda.univariate;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.crash_reproduction.eda.representation.IModelRepresentation;
import org.mate.crash_reproduction.eda.representation.NodeWithPickedAction;
import org.mate.crash_reproduction.fitness.ActionFitnessFunctionWrapper;
import org.mate.crash_reproduction.fitness.ActionMultipleFitnessFunctionWrapper;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.utils.Randomness;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Organizes variables in tree and updates similar to PBIL
 */
public class PIPE extends RepresentationBasedModel {
    private final ActionFitnessFunctionWrapper fitnessFunction;
    private final double learningRate;
    private final double negativeLearningRate;
    private final double epsilon;
    private final double clr;
    private final double pEl;
    private final double pMutation;
    private final double mutationRate;

    private IChromosome<TestCase> elitist;

    public PIPE(IModelRepresentation modelRepresentation, IFitnessFunction<TestCase> fitnessFunction, double learningRate, double negativeLearningRate, double epsilon, double clr, double pEl, double pMutation, double mutationRate) {
        super(modelRepresentation);
        if (fitnessFunction.isMaximizing()) {
            throw new IllegalArgumentException("PIPE needs a minimizing fitness function!");
        }
        this.fitnessFunction = new ActionMultipleFitnessFunctionWrapper(fitnessFunction);
        this.learningRate = learningRate;
        this.negativeLearningRate = negativeLearningRate;
        this.epsilon = epsilon;
        this.clr = clr;
        this.pEl = pEl;
        this.pMutation = pMutation;
        this.mutationRate = mutationRate;

        MATE.log(String.format(Locale.getDefault(),
                "Using PIPE with {learningRate: %f, epsilon: %f, clr: %f, pEL: %f, pMutation: %f, mutationRate: %f}",
                learningRate, epsilon, clr, pEl, pMutation, mutationRate
                ));
    }

    @Override
    public void update(Collection<IChromosome<TestCase>> population) {
        List<IChromosome<TestCase>> sortedPopulation = population.stream()
                .sorted(Comparator.comparingDouble(fitnessFunction::getNormalizedFitness))
                .collect(Collectors.toList());

        IChromosome<TestCase> best = sortedPopulation.get(0);
        MATE.log("Best testcase from update is: " + best.getValue().getId());

        if (elitist == null || fitnessFunction.getFitness(best) < fitnessFunction.getFitness(elitist)) {
            elitist = best;
        }

        // Elitist learning does not lead to a new population so we repeatedly apply it
        while (Randomness.getRnd().nextDouble() < pEl) {
            MATE.log("Running elitist learning");
            adaptPPTTowards(elitist);
            pptPruning();
        }

        // Generation-based learning
        adaptPPTTowards(best);
        pptMutation(best);
        pptPruning();
    }

    // ================== (3) Learning from Population =========================
    private void adaptPPTTowards(IChromosome<TestCase> best) {
        increaseOfBest(best);
        renormalizeOthers(best.getValue());
    }

    private double probability(List<NodeWithPickedAction> nodes) {
        // PIPE paper 4.1
        double probProduct = 1;

        for (NodeWithPickedAction nodeWithPickedAction : nodes) {
            probProduct *= nodeWithPickedAction.getProbabilityOfAction();
        }

        return probProduct;
    }

    private double betterTargetProbability(double probBestTestcase, double fitBest) {
        // PIPE paper 4.2
        double fitElitist = fitnessFunction.getFitness(elitist);

        return probBestTestcase + (1 - probBestTestcase) * learningRate * ((epsilon + fitElitist) / (epsilon + fitBest));
    }

    private double worseTargetProbability(double probTestcase, double fitness) {
        double fitElitist = fitnessFunction.getFitness(elitist);

        return probTestcase - probTestcase * negativeLearningRate * ((epsilon + fitness) / (epsilon + fitElitist));
    }

    private void increaseOfBest(IChromosome<TestCase> bestTestcase) {
        // PIPE paper 4.3
        SplitTestCase splitTestCase = cutOffAfterLastFitnessEnhancement(bestTestcase);
        double fitness = fitnessFunction.getFitness(bestTestcase);

        if (!splitTestCase.goodActions.isEmpty()) {
            double pTarget = betterTargetProbability(probability(splitTestCase.goodActions), fitness);

            // Increase probability of "good" actions (i.e. actions that decrease fitness)
            int iterations = 0;
            while (probability(splitTestCase.goodActions) < pTarget) {
                for (NodeWithPickedAction nodeWithPickedAction : splitTestCase.goodActions) {
                    double probBefore = nodeWithPickedAction.getProbabilityOfAction();
                    nodeWithPickedAction.putProbabilityOfAction(probBefore + clr * learningRate * (1 - probBefore));
                }
                iterations++;
            }
            MATE.log("PIPE increaseOfBest good iterations: " + iterations);
        }

        if (!splitTestCase.badActions.isEmpty()) {
            double pTarget = worseTargetProbability(probability(splitTestCase.badActions), fitness);

            int iterations = 0;
            while (probability(splitTestCase.badActions) > pTarget) {
                // Decrease probability of "bad" actions
                for (NodeWithPickedAction nodeWithPickedAction : splitTestCase.badActions) {
                    double probBefore = nodeWithPickedAction.getProbabilityOfAction();
                    nodeWithPickedAction.putProbabilityOfAction(probBefore - clr * negativeLearningRate * probBefore);
                }
                iterations++;
            }
            MATE.log("PIPE increaseOfBest bad iterations: " + iterations);
        }
    }

    private SplitTestCase cutOffAfterLastFitnessEnhancement(IChromosome<TestCase> chromosome) {
        List<NodeWithPickedAction> nodes = new LinkedList<NodeWithPickedAction>() {{
            modelRepresentation.getTestcaseIterator(chromosome.getValue()).forEachRemaining(this::add);
        }};

        int indexOfLastFitnessDecrease = -1;
        double prevFitness = fitnessFunction.getFitnessAfterXActions(chromosome, 0);

        for (int i = 0; i < nodes.size(); i++) {
            NodeWithPickedAction nodeWithPickedAction = nodes.get(i);
            double fitness = fitnessFunction.getFitnessAfterXActions(chromosome, nodeWithPickedAction.actionIndex + 1);

            if (prevFitness - fitness > 0.002) {
                indexOfLastFitnessDecrease = i;
            }
            prevFitness = fitness;
        }

        return new SplitTestCase(nodes.subList(0, indexOfLastFitnessDecrease + 1), nodes.subList(indexOfLastFitnessDecrease + 1, nodes.size()));
    }

    private void renormalizeOthers(TestCase bestTestcase) {
        Iterator<NodeWithPickedAction> testCaseModelIterator = modelRepresentation.getTestcaseIterator(bestTestcase);

        while (testCaseModelIterator.hasNext()) {
            NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();
            double probBest = nodeWithPickedAction.getProbabilityOfAction();
            double sum = nodeWithPickedAction.getActionProbabilities().values().stream().mapToDouble(d -> d).sum();

            for (Map.Entry<Action, Double> entry : nodeWithPickedAction.getActionProbabilities().entrySet()) {
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

            Iterator<NodeWithPickedAction> testCaseModelIterator = modelRepresentation.getTestcaseIterator(bestTestcase.getValue());

            while (testCaseModelIterator.hasNext()) {
                NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

                for (Map.Entry<Action, Double> entry : nodeWithPickedAction.getActionProbabilities().entrySet()) {
                    if (Randomness.getRnd().nextDouble() < pMP) {
                        double probBefore = entry.getValue();
                        // PIPE paper 4.5
                        MATE.log("Applying mutation");
                        entry.setValue(probBefore + mutationRate * (1 - probBefore));
                    }
                }

                // Normalization
                double sum = nodeWithPickedAction.getActionProbabilities().values().stream().mapToDouble(d -> d).sum();
                for (Map.Entry<Action, Double> entry : nodeWithPickedAction.getActionProbabilities().entrySet()) {
                    entry.setValue(entry.getValue() / sum);
                }
            }
        }
    }

    // ======================= (5) Prototype Tree Pruning =========================
    private void pptPruning() {
        // TODO Remove subtrees which are very unlikely to be reached, ignoring for now, since this is only a performance/memory optimization
    }
    
    @Override
    protected void afterChromosomeChanged(Chromosome<TestCase> chromosome) {
        Registry.getEnvironmentManager().storeActionFitnessData(chromosome);
        fitnessFunction.recordCurrentActionFitness(chromosome);
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        IChromosome<TestCase> newChromosome = super.createChromosome();
        fitnessFunction.writeExplorationStepsToFile(newChromosome);
        return newChromosome;
    }

    @Override
    public String toString() {
        return modelRepresentation.toString();
    }

    private static class SplitTestCase {
        private final List<NodeWithPickedAction> goodActions;
        private final List<NodeWithPickedAction> badActions;

        private SplitTestCase(List<NodeWithPickedAction> goodActions, List<NodeWithPickedAction> badActions) {
            this.goodActions = goodActions;
            this.badActions = badActions;
        }
    }
}
