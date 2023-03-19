package org.mate.exploration.genetic.util.eda;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.util.eda.dot.DotConverter;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.Randomness;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An instance of EDA called Probabilistic Incremental Program Evolution (PIPE), see
 * https://dl.acm.org/doi/pdf/10.1162/evco.1997.5.2.123.
 */
public class PIPE implements IProbabilisticModel<TestCase> {

    /**
     * The fitness function wrapper used to store and retrieve fitness values after each action.
     */
    private final ActionFitnessFunctionWrapper fitnessFunction;

    /**
     * The learning rate used to update the probability of good nodes of the best test case.
     */
    private final double learningRate;

    /**
     * The negative learning rate used to update the probability of bad nodes of the best test case.
     */
    private final double negativeLearningRate;

    /**
     * A user defined epsilon used in updating the probability of good and bad nodes.
     */
    private final double epsilon;

    /**
     * A further constant learning rate that influences the number of iterations when updating
     * the probabilities along the path of good and bad nodes of the best test case. According to
     * the paper, a value of {@code 0.1} is deemed to be the best rate.
     */
    private final double clr;

    /**
     * The probability for elitist learning.
     */
    private final double pEl;

    /**
     * The probability for mutating probabilities in the PPT.
     */
    private final double pMutation;

    /**
     * The mutation rate (degree) used during mutating probabilities in the PPT.
     */
    private final double mutationRate;

    /**
     * Stores the best chromosome seen so far.
     */
    private IChromosome<TestCase> elitist;

    /**
     * The probabilistic prototype tree (PPT).
     */
    private final ApplicationStateTree ppt
            = new ApplicationStateTree(new ProbabilityInitialization(0.6));

    /**
     * Initialises the PIPE algorithm with the given properties.
     *
     * @param fitnessFunction The used fitness function.
     * @param learningRate The used learning rate for good nodes.
     * @param negativeLearningRate The used negative learning rate for bad nodes.
     * @param epsilon The used epsilon (small user defined constant).
     * @param clr The used constant learning rate (controls the number of iterations).
     * @param pEl The used probability for elitist learning.
     * @param pMutation The used probability for mutation.
     * @param mutationRate The used mutation rate (degree of mutation).
     */
    public PIPE(IFitnessFunction<TestCase> fitnessFunction, double learningRate,
                double negativeLearningRate, double epsilon, double clr,
                double pEl, double pMutation, double mutationRate) {

        this.fitnessFunction = new ActionFitnessFunctionWrapper(fitnessFunction);
        this.learningRate = learningRate;
        this.negativeLearningRate = negativeLearningRate;
        this.epsilon = epsilon;
        this.clr = clr;
        this.pEl = pEl;
        this.pMutation = pMutation;
        this.mutationRate = mutationRate;

        MATE.log(String.format(Locale.getDefault(),
                "Using PIPE with {learningRate: %f, epsilon: %f, clr: %f, pEL: %f, " +
                        "pMutation: %f, mutationRate: %f}",
                learningRate, epsilon, clr, pEl, pMutation, mutationRate
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePosition(final TestCase testCase, final Action action,
                               final IScreenState currentScreenState) {
        ppt.updatePosition(testCase, action, currentScreenState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Action, Double> getActionProbabilities() {
        return ppt.getActionProbabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IScreenState getState() {
        return ppt.getState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePositionImmutable(final IScreenState currentScreenState) {
        ppt.updatePositionImmutable(currentScreenState);
    }

    /**
     * Provides a textual representation of the PPT.
     *
     * @return Returns a textual representation of the PPT.
     */
    @Override
    public String toString() {
        return ppt.toString();
    }

    /**
     * Updates the probabilistic prototype tree (PPT) with information about the current population.
     *
     * @param population The current population.
     */
    @Override
    public void update(final Collection<IChromosome<TestCase>> population) {

        if (Properties.PIPE_RECORD_PPT()) {
            // Convert to dot before the model is refined.
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");
            DotConverter.toDot(ppt, LocalDateTime.now().format(formatter) + "-model-before-update.dot");
        }

        final List<IChromosome<TestCase>> sortedPopulation = population.stream()
                .sorted(Comparator.comparingDouble(fitnessFunction::getNormalizedFitness))
                .collect(Collectors.toList());

        final IChromosome<TestCase> best = sortedPopulation.get(0);

        // keep track of the best chromosome seen so far
        if (elitist == null || fitnessFunction.getFitness(best) < fitnessFunction.getFitness(elitist)) {
            elitist = best;
        }

        // elitist learning does not lead to a new population so we repeatedly apply it
        MATE.log_acc("Elitist learning...");
        while (Randomness.getRnd().nextDouble() < pEl) {
            adaptPPTTowards(elitist);
            pptPruning();
        }

        // generation-based learning
        MATE.log_acc("Generation-based learning...");
        adaptPPTTowards(best);
        pptMutation(best);
        pptPruning();

        if (Properties.PIPE_RECORD_PPT()) {
            // Convert to dot after the model was refined.
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");
            DotConverter.toDot(ppt, LocalDateTime.now().format(formatter) + "-model-after-update.dot");
        }
    }

    /**
     * Adapts the PPT towards the best chromosome, i.e. the action probabilities of the best test
     * are increased, see Algorithm 4 in the paper.
     *
     * @param best The best chromosome.
     */
    private void adaptPPTTowards(final IChromosome<TestCase> best) {
        increaseOfBest(best);
        reNormalizeOthers(best.getValue());
    }

    /**
     * Computes the probability for a path of nodes, i.e. the product of the individual probabilities.
     *
     * @param nodes A list of nodes denoting a path in the PPT.
     * @return Returns the path probability for the given nodes.
     */
    private double probability(final List<NodeWithPickedAction> nodes) {

        // PIPE paper 4.1
        double pathProbability = 1;

        // The path probability is the product over the individual probabilities of the path nodes.
        for (NodeWithPickedAction nodeWithPickedAction : nodes) {
            pathProbability *= nodeWithPickedAction.getProbabilityOfAction();
        }

        return pathProbability;
    }

    /**
     * Computes the new target/path probability for the good nodes of the best test case.
     *
     * @param probBestTestCase The current path probability for the good nodes of the best test case.
     * @param fitBestTestCase The fitness of the best test case.
     * @return Returns the new path probability for the good nodes of the best test case.
     */
    private double betterTargetProbability(final double probBestTestCase, final double fitBestTestCase) {
        // PIPE paper 4.2
        final double fitElitist = fitnessFunction.getFitness(elitist);
        return probBestTestCase + (1 - probBestTestCase) * learningRate
                * ((epsilon + fitElitist) / (epsilon + fitBestTestCase));
    }

    /**
     * Computes the new target/path probability for the bad nodes of the best test case.
     *
     * @param probBestTestCase The current path probability for the bad nodes of the best test case.
     * @param fitBestTestCase The fitness of the best test case.
     * @return Returns the new path probability for the bad nodes of the best test case.
     */
    private double worseTargetProbability(final double probBestTestCase, final double fitBestTestCase) {
        final double fitElitist = fitnessFunction.getFitness(elitist);
        return probBestTestCase - probBestTestCase * negativeLearningRate
                * ((epsilon + fitBestTestCase) / (epsilon + fitElitist));
    }

    /**
     * Increases the action probabilities of the best test case. To be more precise, we increase
     * the probabilities of the good actions, i.e. those that decrease fitness, while we decrease
     * the probabilities of the bad actions, i.e. those that increase fitness.
     *
     * @param bestTestCase The best test case.
     */
    private void increaseOfBest(final IChromosome<TestCase> bestTestCase) {

        // PIPE paper 4.3, we split the test case into good and bad actions.
        final SplitTestCase splitTestCase = cutOffAfterLastFitnessEnhancement(bestTestCase);
        final double fitness = fitnessFunction.getFitness(bestTestCase);

        if (!splitTestCase.goodActions.isEmpty()) {

            // Compute the new target/path probability for the good nodes.
            double pTarget = betterTargetProbability(probability(splitTestCase.goodActions), fitness);

            // Increase the probability of "good" actions (i.e. actions that decrease fitness) until
            // we reach the target probability.
            int iterations = 0;
            while (probability(splitTestCase.goodActions) < pTarget) {
                for (final NodeWithPickedAction nodeWithPickedAction : splitTestCase.goodActions) {
                    final double probBefore = nodeWithPickedAction.getProbabilityOfAction();
                    nodeWithPickedAction.setProbabilityOfAction(probBefore
                            + clr * learningRate * (1 - probBefore));
                }
                iterations++;
            }
            MATE.log_acc("PIPE increaseOfBest good iterations: " + iterations);
        }

        if (!splitTestCase.badActions.isEmpty()) {

            // Compute the new target/path probability for the bad nodes.
            double pTarget = worseTargetProbability(probability(splitTestCase.badActions), fitness);

            int iterations = 0;
            while (probability(splitTestCase.badActions) > pTarget) {
                // Decrease probability of "bad" actions (i.e. actions that increase fitness) until
                // we reach the target probability.
                for (final NodeWithPickedAction nodeWithPickedAction : splitTestCase.badActions) {
                    final double probBefore = nodeWithPickedAction.getProbabilityOfAction();
                    nodeWithPickedAction.setProbabilityOfAction(probBefore
                            - clr * negativeLearningRate * probBefore);
                }
                iterations++;
            }
            MATE.log_acc("PIPE increaseOfBest bad iterations: " + iterations);
        }
    }

    /**
     * Splits the best test case into good and bad nodes.
     *
     * @param bestTestCase The best test case.
     * @return Returns the split test case consisting of good and bad nodes.
     */
    private SplitTestCase cutOffAfterLastFitnessEnhancement(final IChromosome<TestCase> bestTestCase) {

        // Compute the list of nodes traversed by the best test case.
        final List<NodeWithPickedAction> nodes = new LinkedList<NodeWithPickedAction>() {{
            new TestCaseModelIterator(PIPE.this, bestTestCase.getValue())
                    .forEachRemaining(this::add);
        }};

        int indexOfLastFitnessDecrease = -1;
        double prevFitness = fitnessFunction.getFitnessAfterXActions(bestTestCase, 0);

        // Determine point in test case sequence when fitness doesn't get better (decrease) anymore.
        for (int i = 0; i < nodes.size(); i++) {

            final NodeWithPickedAction nodeWithPickedAction = nodes.get(i);
            final double fitness = fitnessFunction.getFitnessAfterXActions(bestTestCase,
                    nodeWithPickedAction.actionIndex + 1);

            // check if fitness is getting better (decreases)
            if (prevFitness - fitness > 0.002) { // TODO: Seems to be an epsilon, may use Double.compare()!
                indexOfLastFitnessDecrease = i;
            }

            prevFitness = fitness;
        }

        return new SplitTestCase(nodes.subList(0, indexOfLastFitnessDecrease + 1),
                nodes.subList(indexOfLastFitnessDecrease + 1, nodes.size()));
    }

    /**
     * Re-normalises the action probabilities of the best test case to form a valid probability
     * distribution, i.e. the sum over the action probabilities of a single node must be 1.
     *
     * @param bestTestCase The best test case.
     */
    private void reNormalizeOthers(final TestCase bestTestCase) {

        // Traverse over all nodes of the best test case.
        final Iterator<NodeWithPickedAction> testCaseModelIterator
                = new TestCaseModelIterator(this, bestTestCase);

        while (testCaseModelIterator.hasNext()) {

            final NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();
            final double probAction = nodeWithPickedAction.getProbabilityOfAction();
            final double sum = nodeWithPickedAction.getActionProbabilities().values().stream()
                    .mapToDouble(d -> d).sum();

            // Normalize the action probabilities of the traversed node.
            for (final Map.Entry<Action, Double> entry : nodeWithPickedAction.getActionProbabilities().entrySet()) {
                if (!entry.getKey().equals(nodeWithPickedAction.action)) {
                    double probBefore = entry.getValue();
                    entry.setValue(probBefore * (1 - (1 - sum) / (probAction - sum)));
                }
            }
        }
    }

    /**
     * Mutates the probabilities of the PPT.
     *
     * @param bestTestCase The best test case.
     */
    private void pptMutation(final IChromosome<TestCase> bestTestCase) {

        MATE.log_acc("Mutation of PPT...");

        if (Randomness.getRnd().nextDouble() < pMutation) {

            // TODO: How to adapt formula 4.4 for the variable P_MP as we don't distinct between terminals
            //  and non-terminals?
            int z = 1;
            double pMP = pMutation / (z * Math.sqrt(bestTestCase.getValue().getActionSequence().size()));

            final Iterator<NodeWithPickedAction> testCaseModelIterator
                    = new TestCaseModelIterator(this, bestTestCase.getValue());

            // Mutate the action probabilities of the best test case.
            while (testCaseModelIterator.hasNext()) {

                final NodeWithPickedAction nodeWithPickedAction = testCaseModelIterator.next();

                // TODO: Do we really mutate all action probabilities of every node that were traversed by
                //  the best test case or only the action probabilities of the best test case?
                for (final Map.Entry<Action, Double> entry : nodeWithPickedAction.getActionProbabilities().entrySet()) {
                    if (Randomness.getRnd().nextDouble() < pMP) {
                        final double probBefore = entry.getValue();
                        // PIPE paper 4.5
                        entry.setValue(probBefore + mutationRate * (1 - probBefore));
                    }
                }

                // Normalise to form a valid probability distribution at the given node.
                double sum = nodeWithPickedAction.getActionProbabilities().values().stream()
                        .mapToDouble(d -> d).sum();
                for (final Map.Entry<Action, Double> entry : nodeWithPickedAction.getActionProbabilities().entrySet()) {
                    entry.setValue(entry.getValue() / sum);
                }
            }
        }
    }

    /**
     * Performs the pruning of the PPT, i.e. it removes subtrees that became irrelevant over time.
     */
    private void pptPruning() {
        MATE.log_acc("Pruning of PPT...");
        // TODO Remove subtrees which are very unlikely to be reached, ignoring for now, since this
        //  is only a performance/memory optimization.
    }

    /**
     * Splits a test case into good and bad actions.
     */
    private static class SplitTestCase {

        /**
         * The list of good actions in a test case (those that improve the fitness).
         */
        private final List<NodeWithPickedAction> goodActions;

        /**
         * The list of bad actions in a test case (those that don't improve the fitness).
         */
        private final List<NodeWithPickedAction> badActions;

        private SplitTestCase(List<NodeWithPickedAction> goodActions, List<NodeWithPickedAction> badActions) {
            this.goodActions = goodActions;
            this.badActions = badActions;
        }
    }
}
