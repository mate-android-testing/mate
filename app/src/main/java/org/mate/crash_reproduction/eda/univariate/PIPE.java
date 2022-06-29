package org.mate.crash_reproduction.eda.univariate;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.crash_reproduction.eda.IDistributionModel;
import org.mate.crash_reproduction.eda.representation.IModelRepresentation;
import org.mate.crash_reproduction.eda.representation.ModelRepresentationIterator;
import org.mate.crash_reproduction.eda.representation.TestCaseModelIterator;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Organizes variables in tree and updates similar to PBIL
 */
public class PIPE extends RepresentationBasedModel {
    private final IFitnessFunction<TestCase> fitnessFunction;
    private final double learningRate;
    private final double epsilon;
    private final double clr;
    private final double pEl;
    private final double pMutation;
    private final double mutationRate;

    private IChromosome<TestCase> elitist;

    public PIPE(IModelRepresentation modelRepresentation, IFitnessFunction<TestCase> fitnessFunction, double learningRate, double epsilon, double clr, double pEl, double pMutation, double mutationRate) {
        super(modelRepresentation);
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
        TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(modelRepresentation.getIterator(), testCase);

        while (testCaseModelIterator.hasNext()) {
            TestCaseModelIterator.NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

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
            TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(modelRepresentation.getIterator(), bestTestcase.getValue());

            while (testCaseModelIterator.hasNext()) {
                TestCaseModelIterator.NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

                double probBefore = nodeWithPickedAction.getProbabilityOfAction();
                nodeWithPickedAction.putProbabilityOfAction(probBefore + clr * learningRate * (1 - probBefore));
            }
        }
    }

    private void decreaseOfWorst(IChromosome<TestCase> worstTestcase, IChromosome<TestCase> best) {
        double pTarget = worseTargetProbability(worstTestcase);

        while (probability(worstTestcase.getValue()) > pTarget) {
            TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(modelRepresentation.getIterator(), worstTestcase.getValue());
            TestCaseModelIterator bestTestCaseModelIterator = new TestCaseModelIterator(modelRepresentation.getIterator(), best.getValue());

            while (testCaseModelIterator.hasNext()) {
                TestCaseModelIterator.NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

                // ignore common prefix
                if (!bestTestCaseModelIterator.hasNext() || !bestTestCaseModelIterator.next().equals(nodeWithPickedAction)) {
                    double probBefore = nodeWithPickedAction.getProbabilityOfAction();
                    nodeWithPickedAction.putProbabilityOfAction(probBefore - clr * learningRate * probBefore);
                }
            }
        }
    }

    private void renormalizeOthers(TestCase bestTestcase) {
        TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(modelRepresentation.getIterator(), bestTestcase);

        while (testCaseModelIterator.hasNext()) {
            TestCaseModelIterator.NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();
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

            TestCaseModelIterator testCaseModelIterator = new TestCaseModelIterator(modelRepresentation.getIterator(), bestTestcase.getValue());

            while (testCaseModelIterator.hasNext()) {
                TestCaseModelIterator.NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

                for (Map.Entry<Action, Double> entry : nodeWithPickedAction.getActionProbabilities().entrySet()) {
                    if (Randomness.getRnd().nextDouble() < pMP) {
                        double probBefore = entry.getValue();
                        // PIPE paper 4.5
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
    public String toString() {
        return modelRepresentation.toString();
    }
}
