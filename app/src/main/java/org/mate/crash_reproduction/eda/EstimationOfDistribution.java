package org.mate.crash_reproduction.eda;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.crash_reproduction.CrashReproduction;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EstimationOfDistribution extends CrashReproduction {
    private final List<IFitnessFunction<TestCase>> fitnessFunctions;
    private final ISelectionFunction<TestCase> selectionFunction;
    private final IChromosomeFactory<TestCase> chromosomeFactory;
    private final int populationSize;
    private final int maxNumEvents;
    private final double pRandomAction;
    private final IDistributionModel<UIAction> model;
    private final LocalDateTime start = LocalDateTime.now();

    public EstimationOfDistribution(List<IFitnessFunction<TestCase>> fitnessFunctions,
                                    ISelectionFunction<TestCase> selectionFunction,
                                    IChromosomeFactory<TestCase> chromosomeFactory,
                                    IDistributionModel<UIAction> model,
                                    int populationSize, int maxNumEvents, double pRandomAction) {
        super(fitnessFunctions);
        this.fitnessFunctions = fitnessFunctions;
        this.selectionFunction = selectionFunction;
        this.chromosomeFactory = chromosomeFactory;
        this.populationSize = populationSize;
        this.maxNumEvents = maxNumEvents;
        this.pRandomAction = pRandomAction;
        this.model = model;
    }

    @Override
    protected List<IChromosome<TestCase>> initialPopulation() {
        return Stream.generate(chromosomeFactory::createChromosome)
                .limit(populationSize)
                .collect(Collectors.toList());
    }

    @Override
    protected List<IChromosome<TestCase>> evolve(List<IChromosome<TestCase>> prevPopulation) {
        List<IChromosome<TestCase>> best = selectionFunction.select(prevPopulation, fitnessFunctions);
        model.update(new HashSet<>(best));
        String fileName = String.format("%s-gen-%d-model.dot", start.format(DateTimeFormatter.ISO_DATE_TIME), currentGenerationNumber);
        Registry.getEnvironmentManager().writeFile(fileName, model.toString());

        return Stream.generate(this::drawTestCase)
                .limit(prevPopulation.size())
                .collect(Collectors.toList());
    }

    private IChromosome<TestCase> drawTestCase() {
        return drawTestCase(this::selectAction, "");
    }

    private IChromosome<TestCase> drawTestCase(Function<UIAction, UIAction> actionSelector, String nickName) {
        Registry.getUiAbstractionLayer().resetApp();

        TestCase testCase = TestCase.newInitializedTestCase();
        testCase.setNickName(nickName);
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);
        UIAction prevAction = null;

        try {
            for (int actionsCount = 0; actionsCount < maxNumEvents; actionsCount++) {
                UIAction action = actionSelector.apply(prevAction);
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

    private UIAction selectAction(UIAction prevAction) {
        Optional<UIAction> nextAction = model.drawNextNode(prevAction);

        if (Randomness.getRnd().nextDouble() >= pRandomAction) {
            if (nextAction.isPresent()) {
                Optional<UIAction> action = filterApplicableActions(nextAction.get());

                if (action.isPresent()) {
                    return action.get();
                }
            } else {
                MATE.log("Would have taken action from model, but there was none");
            }
        }

        MATE.log("Taking random action");
        return pickRandomAction(prevAction);
    }

    private UIAction pickRandomAction(UIAction prevAction) {
        Set<UIAction> modelActions = model.getPossibleNodes(prevAction);
        List<UIAction> executableActions = Registry.getUiAbstractionLayer().getExecutableActions();
        List<UIAction> notExecutedActions = new LinkedList<>(executableActions);
        notExecutedActions.removeAll(modelActions);
        List<UIAction> promisingActions = promisingActions(notExecutedActions);
        for (UIAction action : promisingActions) {
            if (action instanceof WidgetAction) {
                WidgetAction widgetAction = (WidgetAction) action;
                MATE.log("RANDOM ACTION PICK: promising action " + action.toShortString() + " [" + widgetAction.getWidget().getTokens().collect(Collectors.joining(", ")) + "]");
            }
        }

        List<UIAction> drawSet = getFirstNonEmpty(promisingActions, notExecutedActions, executableActions);
        MATE.log(String.format("RANDOM ACTION PICK: (Model contains %d actions, There are %d possible actions, There are %d promising actions, We draw from %d actions)", modelActions.size(), executableActions.size(), promisingActions.size(), drawSet.size()));
        return Randomness.randomElement(drawSet);
    }

    @SafeVarargs
    private static <T> List<T> getFirstNonEmpty(List<T>... lists) {
        for (List<T> l : lists) {
            if (!l.isEmpty()) {
                return l;
            }
        }
        throw new IllegalArgumentException("All lists are empty...");
    }

    private List<UIAction> promisingActions(List<UIAction> actions) {
        return Registry.getUiAbstractionLayer().getPromisingActions().stream()
                .filter(actions::contains)
                .collect(Collectors.toList());
    }

    private Optional<UIAction> filterApplicableActions(UIAction action) {
        if (Registry.getUiAbstractionLayer().getExecutableActions().contains(action)) {
            MATE.log("Taking action from model");
            return Optional.of(action);
        } else {
            MATE.log("Would have taken action from model, but it was not applicable to current state!");
            return Optional.empty();
        }
    }
}
