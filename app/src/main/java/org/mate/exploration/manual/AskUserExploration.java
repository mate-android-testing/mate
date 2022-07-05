package org.mate.exploration.manual;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.crash_reproduction.eda.util.DotGraphUtil;
import org.mate.crash_reproduction.fitness.CrashDistance;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.CoverageUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AskUserExploration implements Algorithm {
    private final CrashDistance fitnessFunction = new CrashDistance();
    private int chromosomeCounter = 0;

    @Override
    public void run() {
        while (true) {
            createChromosome();
            chromosomeCounter++;
        }
    }

    private Chromosome<TestCase> createChromosome() {
        Registry.getUiAbstractionLayer().resetApp();

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);
        List<ExplorationStep> explorationSteps = new LinkedList<>();
        explorationSteps.add(createStep(chromosome));

        try {
            for (int actionsCount = 0; actionsCount < Properties.MAX_NUMBER_EVENTS(); actionsCount++) {
                Optional<Action> action = askUserToPickAction();
                boolean stop;

                if (action.isPresent()) {
                    stop = !testCase.updateTestCase(action.get(), actionsCount);
                    explorationSteps.add(createStep(chromosome));
                } else {
                    stop = true;
                }

                if (stop) {
                    return chromosome;
                }
            }
        } finally {
            testCase.finish();
            Registry.getEnvironmentManager().writeFile("exp_" + chromosomeCounter + ".dot", toDot(explorationSteps, chromosome));
        }
        return chromosome;
    }

    private Optional<Action> askUserToPickAction() {
        List<Action> availableActions = new LinkedList<>(Registry.getUiAbstractionLayer().getExecutableActions());
        Action stopExplorationAction = new Action() {
            @NonNull
            @Override
            public String toString() {
                return "Stop exploration";
            }

            @NonNull
            @Override
            public String toShortString() {
                return toString();
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public boolean equals(@Nullable Object o) {
                return o == this;
            }
        };
        availableActions.add(0, stopExplorationAction);

        Action pickedAction = Registry.getEnvironmentManager().askUserToPick(availableActions, this::prettyPrintAction);

        if (pickedAction == stopExplorationAction) {
            return Optional.empty();
        } else {
            return Optional.of(pickedAction);
        }
    }

    private String prettyPrintAction(Action a) {
        return a.toShortString() + " (" + tokens(a).collect(Collectors.joining(",")) + ")";
    }

    private Stream<String> tokens(Action action) {
        if (action instanceof MotifAction) {
            return ((MotifAction) action).getUIActions().stream().flatMap(this::tokens);
        } else if (action instanceof WidgetAction) {
            return ((WidgetAction) action).getWidget().getTokens();
        } else {
            return Stream.empty();
        }
    }

    private String toDot(List<ExplorationStep> steps, IChromosome<TestCase> chromosome) {
        StringJoiner graph = new StringJoiner("\n");

        graph.add("digraph D {");

        Iterator<Action> actionIterator = chromosome.getValue().getEventSequence().iterator();
        ExplorationStep prevStep = null;
        double prevFitness = fitnessFunction.isMaximizing() ? 0 : 1;
        Map<String, Double> prevFitnessVector = null;
        for (ExplorationStep step : steps) {
            // Print step
            Map<String, String> attributes = new HashMap<>();
            attributes.put("image", "\"results/pictures/" + Registry.getPackageName() + "/" + step.nodeId + ".png\"");
            attributes.put("imagescale", "true");
            attributes.put("imagepos", "tc");
            attributes.put("labelloc", "b");
            attributes.put("height", "6");
            attributes.put("fixedsize", "true");
            attributes.put("shape", "square");
            Map<String, Double> finalPrevFitnessVector = prevFitnessVector;
            attributes.put("xlabel", "<" + step.state.getId() + " on " + step.state.getActivityName()
                    + "<BR/><B>Fitness: " + step.fitness + " " + describeFitnessChange(prevFitness, step.fitness) + "</B><BR/>"
                    + step.fitnessVector.entrySet().stream().map(e -> {
                        String suffix = finalPrevFitnessVector == null ? "" : describeFitnessChange(finalPrevFitnessVector.get(e.getKey()), e.getValue());
                        return e.getKey() + ": " + e.getValue() + " " + suffix;
                    }).collect(Collectors.joining("<BR/>"))
                    + ">");
            graph.add(String.format("%s [%s]", step.nodeId, DotGraphUtil.getAttributeString(attributes)));

            // Print edge
            if (prevStep != null) {
                graph.add(String.format("%s -> %s [label=\"%s\"]", prevStep.nodeId, step.nodeId, printAction(actionIterator.next())));
            }

            prevStep = step;
            prevFitness = step.fitness;
            prevFitnessVector = step.fitnessVector;
        }

        graph.add("}");

        return graph.toString();
    }

    private String printAction(Action action) {
        return action.toString();
    }

    private String describeFitnessChange(double prevFitness, double curFitness) {
        if (curFitness < prevFitness) {
            return "\uD83E\uDC56"; // lower
        } else if (curFitness > prevFitness) {
            return "\uD83E\uDC55"; // higher
        } else {
            return ""; // same
        }
    }

    private ExplorationStep createStep(IChromosome<TestCase> chromosome) {
        FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
        CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
        double fitness = fitnessFunction.getNormalizedFitness(chromosome);

        String nodeId = "exp_" + chromosomeCounter + "_step_" + chromosome.getValue().getEventSequence().size();
        Registry.getEnvironmentManager().takeScreenshot(Registry.getPackageName(), nodeId);
        List<Widget> widgets = Registry.getUiAbstractionLayer().getPromisingActions(Registry.getUiAbstractionLayer().getLastScreenState()).stream().map(WidgetAction::getWidget).collect(Collectors.toList());

        if (!widgets.isEmpty()) {
            Registry.getEnvironmentManager().markOnImage(widgets, nodeId);
        }

        return new ExplorationStep(Registry.getUiAbstractionLayer().getLastScreenState(), nodeId, fitness,
                fitnessFunction.getWeightedFitnessFunctions().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getClass().getSimpleName(), e -> e.getKey().getNormalizedFitness(chromosome))));
    }

    private static class ExplorationStep {
        private final IScreenState state;
        private final String nodeId;
        private final double fitness;
        private final Map<String, Double> fitnessVector;

        private ExplorationStep(IScreenState state, String nodeId, double fitness, Map<String, Double> fitnessVector) {
            this.state = state;
            this.nodeId = nodeId;
            this.fitness = fitness;
            this.fitnessVector = fitnessVector;
        }
    }
}
