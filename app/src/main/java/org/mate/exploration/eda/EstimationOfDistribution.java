package org.mate.exploration.eda;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EstimationOfDistribution implements Algorithm {
    private final List<IFitnessFunction<TestCase>> fitnessFunctions;
    private final ISelectionFunction<TestCase> selectionFunction;
    private final IChromosomeFactory<TestCase> chromosomeFactory;
    private final ITerminationCondition terminationCondition;
    private final int populationSize;
    private final int maxNumEvents;
    private final double pRandomAction;
    private final IDistributionModel<Action> model;

    private int currentGenerationNumber = 0;

    public EstimationOfDistribution(List<IFitnessFunction<TestCase>> fitnessFunctions,
                                    ISelectionFunction<TestCase> selectionFunction,
                                    IChromosomeFactory<TestCase> chromosomeFactory,
                                    ITerminationCondition terminationCondition,
                                    IDistributionModel<Action> model,
                                    int populationSize, int maxNumEvents, double pRandomAction) {
        this.fitnessFunctions = fitnessFunctions;
        this.selectionFunction = selectionFunction;
        this.chromosomeFactory = chromosomeFactory;
        this.terminationCondition = terminationCondition;
        this.populationSize = populationSize;
        this.maxNumEvents = maxNumEvents;
        this.pRandomAction = pRandomAction;
        this.model = model;
    }

    @Override
    public void run() {
        List<IChromosome<TestCase>> population = createInitialPopulation();
        MATE.log("Finished initial population");

        while (!terminationCondition.isMet()) {
            List<IChromosome<TestCase>> best = selectionFunction.select(population, fitnessFunctions);
            model.update(best.stream().map(c -> c.getValue().getEventSequence()).collect(Collectors.toSet()));

            population = Stream.generate(this::drawTestCase)
                    .limit(population.size())
                    .collect(Collectors.toList());

            logPopulationFitness(population);
            currentGenerationNumber++;
        }
    }

    private void logPopulationFitness(List<IChromosome<TestCase>> population) {
        MATE.log_acc("Fitness of generation #" + (currentGenerationNumber + 1) + " :");
        for (int i = 0; i < Math.min(fitnessFunctions.size(), 5); i++) {
            MATE.log_acc("Fitness function " + (i + 1) + ":");
            IFitnessFunction<TestCase> fitnessFunction = fitnessFunctions.get(i);
            for (int j = 0; j < population.size(); j++) {
                IChromosome<TestCase> chromosome = population.get(j);
                MATE.log_acc("Chromosome " + (j + 1) + ": "
                        + fitnessFunction.getFitness(chromosome));
            }
        }
        if (fitnessFunctions.size() > 5) {
            MATE.log_acc("Omitted other fitness function because there are too many ("
                    + fitnessFunctions.size() + ")");
        }
    }

    private IChromosome<TestCase> drawTestCase() {
        Registry.getUiAbstractionLayer().resetApp();

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);
        Action prevAction = null;

        try {
            for (int actionsCount = 0; actionsCount < maxNumEvents; actionsCount++) {
                Action action = selectAction(prevAction);
                if (action instanceof WidgetAction
                        && !Registry.getUiAbstractionLayer().getExecutableActions().contains(action)) {
                    MATE.log("LOL wtf ");
                    MATE.log("Prev action: " + prevAction);
                    MATE.log("Action: " + action);
                    MATE.log("Available actions: " + Registry.getUiAbstractionLayer().getExecutableActions().stream().map(Object::toString).collect(Collectors.joining(", ")));
                }
                if (!testCase.updateTestCase(action, actionsCount)) {
                    return chromosome;
                }
                prevAction = action;
            }
        } finally {
            FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
            CoverageUtils.logChromosomeCoverage(chromosome);
            testCase.finish();
            MATE.log("Last action was " + prevAction);
        }

        return chromosome;
    }

    private Action selectAction(Action prevAction) {
        List<UIAction> executableActions = Registry.getUiAbstractionLayer().getExecutableActions();
        Optional<Action> nextAction = model.drawNextNode(prevAction);

        if (Randomness.getRnd().nextDouble() >= pRandomAction) {
            if (nextAction.isPresent()) {
                Action action = nextAction.get();

                if (action instanceof UIAction && executableActions.contains(action)) {
                    MATE.log("Taking action from model");
                    return action;
                } else {
                    MATE.log("Would have taken action from model, but it was not applicable to current state!");
                }
            } else {
                MATE.log("Would have taken action from model, but there was none");
            }
        }

        MATE.log("Taking random action");
        return Randomness.randomElement(executableActions);
    }

    private List<IChromosome<TestCase>> createInitialPopulation() {
        MATE.log("Creating initial population (1st generation)");

        currentGenerationNumber++;
        return Stream.generate(chromosomeFactory::createChromosome)
                .limit(populationSize)
                .collect(Collectors.toList());
    }
}
