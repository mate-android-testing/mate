package org.mate.exploration.manual;

import android.support.annotation.NonNull;

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
import org.mate.utils.Either;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.CoverageUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
            Chromosome<TestCase> template = createTemplateChromosome();
            List<ExplorationStep> explorationSteps = getExplorationSteps(template);
            Registry.getEnvironmentManager().writeFile("exp_" + chromosomeCounter + ".dot", toDot(explorationSteps, template.getValue().getEventSequence()));
            Registry.getEnvironmentManager().writeFile("exp_" + chromosomeCounter + ".py", toMatPlotLibCode(explorationSteps));
            Registry.getEnvironmentManager().writeTraceDiffToFile("exp_" + chromosomeCounter + "_traces_diff.txt", explorationSteps.stream().map(e -> e.chromosome).collect(Collectors.toList()));
            chromosomeCounter++;
        }
    }

    private List<ExplorationStep> getExplorationSteps(Chromosome<TestCase> template) {
        List<ExplorationStep> explorationSteps = new LinkedList<>();
        List<Action> currentActions = new LinkedList<>();

        IChromosome<TestCase> chromosome = retryCreatingChromosome(currentActions, 3);
        explorationSteps.add(createStep(chromosome));

        for (Action action : template.getValue().getEventSequence()) {
            currentActions.add(action);
            chromosome = retryCreatingChromosome(currentActions, 3);
            explorationSteps.add(createStep(chromosome));
        }

        return explorationSteps;
    }

    private IChromosome<TestCase> retryCreatingChromosome(List<Action> actions, int maxTries) {
        Optional<IChromosome<TestCase>> chromosome;
        int tries = 0;

        do {
            tries++;
            chromosome = createChromosome(actions);
        } while (!chromosome.isPresent() && tries < maxTries);

        return chromosome
                .orElseThrow(() -> new IllegalStateException("Was not able to create chromosome after " + maxTries + " tries"));
    }

    private Optional<IChromosome<TestCase>> createChromosome(List<Action> actions) {
        Registry.getUiAbstractionLayer().resetApp();

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        for (int i = 0; i < actions.size(); i++) {
            Optional<Action> closestAction = getClosestCurrentAction(actions.get(i));

            if (!closestAction.isPresent()) {
                return Optional.empty(); // Reached wrong state
            } else if (!testCase.updateTestCase(closestAction.get(), i) && (i+1) < actions.size()) {
                throw new IllegalStateException("App crashed, but there this were " + (actions.size() - i) + " actions left!");
            }
        }

        FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
        CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
        testCase.finish();

        return Optional.of(chromosome);
    }

    private Chromosome<TestCase> createTemplateChromosome() {
        Optional<Chromosome<TestCase>> template;
        do {
            template = createTemplateChromosomeUnsafe();
        } while (!template.isPresent());
        return template.get();
    }

    private Optional<Chromosome<TestCase>> createTemplateChromosomeUnsafe() {
        Registry.getUiAbstractionLayer().resetApp();

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (int actionsCount = 0; actionsCount < Properties.MAX_NUMBER_EVENTS(); actionsCount++) {
                Either<Action, ExplorationCommand> actionOrCommand = askUserToPickAction();
                boolean stop;

                if (actionOrCommand.hasLeft()) {
                    stop = !testCase.updateTestCase(actionOrCommand.getLeft(), actionsCount);
                } else {
                    switch (actionOrCommand.getRight()) {
                        case STOP: stop = true; break;
                        case RESET: return Optional.empty();
                        default: throw new IllegalArgumentException("Unknown command: " + actionOrCommand.getRight().description);
                    }
                }

                if (stop) {
                    return Optional.of(chromosome);
                }
            }
        } finally {
            FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
            testCase.finish();
        }
        return Optional.of(chromosome);
    }

    private Either<Action, ExplorationCommand> askUserToPickAction() {
        List<Action> availableActions = new LinkedList<>(Registry.getUiAbstractionLayer().getExecutableActions());
        availableActions.add(0, new ExplorationCommandAction(ExplorationCommand.STOP));
        availableActions.add(1, new ExplorationCommandAction(ExplorationCommand.RESET));

        Action pickedAction = Registry.getEnvironmentManager().askUserToPick(availableActions, this::prettyPrintAction);

        if (pickedAction instanceof ExplorationCommandAction) {
            return Either.right(((ExplorationCommandAction) pickedAction).command);
        } else {
            return Either.left(pickedAction);
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

    private Optional<Action> getClosestCurrentAction(Action expectedAction) {
        if (Registry.getUiAbstractionLayer().getExecutableActions().contains(expectedAction)) {
            return Optional.of(expectedAction);
        } else if (expectedAction instanceof WidgetAction) {
            // Exact match not possible -> look for closest action
            WidgetAction expectedWidgetAction = (WidgetAction) expectedAction;
            List<Action> matching = Registry.getUiAbstractionLayer().getExecutableActions().stream()
                    .filter(availableAction -> availableAction instanceof WidgetAction)
                    .map(a -> (WidgetAction) a)
                    .filter(availableAction -> availableAction.getActionType().equals(expectedWidgetAction.getActionType())
                            && availableAction.getWidget().getId().equals(expectedWidgetAction.getWidget().getId()))
                    .collect(Collectors.toList());

            if (matching.size() == 1) {
                return Optional.of(matching.get(0));
            }
        }

        return Optional.empty();
    }

    private String toDot(List<ExplorationStep> steps, List<Action> actions) {
        StringJoiner graph = new StringJoiner("\n");

        graph.add("digraph D {");

        Iterator<Action> actionIterator = actions.iterator();
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

    private String toMatPlotLibCode(List<ExplorationStep> steps) {
        String states = "[" + steps.stream().map(s -> '"' + s.nodeId + '"').collect(Collectors.joining(",")) + "]";
        String fitnessValues = "[" + steps.stream().map(s -> String.valueOf(s.fitness)).collect(Collectors.joining(",")) + "]";
        String imageLabelLookup = "{" + steps.stream()
                .map(s -> '"' + s.nodeId + "\":\"\"\"" + s.state.getId() + " Fitness: " + String.format("%,.2f", s.fitness) + "\n" + s.fitnessVector.entrySet().stream().map(e -> e.getKey() + ": " + String.format("%,.2f", e.getValue())).collect(Collectors.joining("\n")) + "\"\"\"")
                .collect(Collectors.joining(", ")) + "}";

        return "import matplotlib.pyplot as plt\n" +
                "from matplotlib.offsetbox import OffsetImage, AnnotationBbox, TextArea\n" +
                "\n" +
                "lookup = " + imageLabelLookup + "\n" +
                "def estimate_height(label):\n" +
                "    num_lines = label.count('\\n')\n" +
                "    return num_lines * 20\n" +
                "\n" +
                "def offset_image(x, ax):\n" +
                "    img = plt.imread(f'pictures/" + Registry.getPackageName() + "/{x}.png')\n" +
                "    im = OffsetImage(img, zoom=0.3)\n" +
                "    im.label = lookup[x]\n" +
                "    im.image.axes = ax\n" +
                "    ab = AnnotationBbox(im, (x, 0), xybox=(0, -320), frameon=False,\n" +
                "                        xycoords='data', boxcoords=\"offset points\", pad=0)\n" +
                "    ax.add_artist(ab)\n" +
                "    ab = AnnotationBbox(TextArea(lookup[x], textprops=dict(size=22)), (x, 0), xybox=(0, -600 - estimate_height(lookup[x])), frameon=False,\n" +
                "                        xycoords='data', boxcoords=\"offset points\", pad=0)\n" +
                "    ax.add_artist(ab)\n" +
                "\n" +
                "x_values = " + states + "\n" +
                "y_values = " + fitnessValues + "\n" +
                "\n" +
                "plt.plot(x_values, y_values, linewidth=4)\n" +
                "plt.gca().set_ylim([0, 1])\n" +
                "plt.gca().tick_params(axis='y', labelsize=20)\n" +
                "\n" +
                "for i, (x, y) in enumerate(zip(x_values, y_values)):\n" +
                "    offset_image(x, ax=plt.gca())\n" +
                "\n" +
                "fig = plt.gcf()\n" +
                "max_label_height = max(map(estimate_height, lookup.values()))\n" +
                "fig.set_size_inches(len(x_values) * 5, 6 + max_label_height / 100)\n" +
                "plt.tight_layout()\n" +
                "plt.savefig('exp_" + chromosomeCounter + "_graph" + ".png', bbox_inches=\"tight\")\n";
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
        double fitness = fitnessFunction.getNormalizedFitness(chromosome);

        String nodeId = "exp_" + chromosomeCounter + "_step_" + chromosome.getValue().getEventSequence().size();
        Registry.getEnvironmentManager().takeScreenshot(Registry.getPackageName(), nodeId);
        Registry.getEnvironmentManager().drawCallTree(nodeId + "_graph", chromosome);
        List<Widget> widgets = Registry.getUiAbstractionLayer().getPromisingActions(Registry.getUiAbstractionLayer().getLastScreenState()).stream().map(WidgetAction::getWidget).collect(Collectors.toList());

        if (!widgets.isEmpty()) {
            Registry.getEnvironmentManager().markOnImage(widgets, nodeId);
        }

        return new ExplorationStep(Registry.getUiAbstractionLayer().getLastScreenState(), nodeId, fitness,
                fitnessFunction.getWeightedFitnessFunctions().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getClass().getSimpleName(), e -> e.getKey().getNormalizedFitness(chromosome))), chromosome);
    }

    private static class ExplorationStep {
        private final IScreenState state;
        private final String nodeId;
        private final double fitness;
        private final Map<String, Double> fitnessVector;
        private final IChromosome<TestCase> chromosome;

        private ExplorationStep(IScreenState state, String nodeId, double fitness, Map<String, Double> fitnessVector, IChromosome<TestCase> chromosome) {
            this.state = state;
            this.nodeId = nodeId;
            this.fitness = fitness;
            this.fitnessVector = fitnessVector;
            this.chromosome = chromosome;
        }
    }

    private enum ExplorationCommand {
        STOP("Stop exploration"),
        RESET("Reset exploration");

        private final String description;

        ExplorationCommand(String description) {
            this.description = description;
        }
    }

    private static class ExplorationCommandAction extends Action {
        private final ExplorationCommand command;

        private ExplorationCommandAction(ExplorationCommand command) {
            this.command = command;
        }

        @NonNull
        @Override
        public String toString() {
            return command.description;
        }

        @NonNull
        @Override
        public String toShortString() {
            return toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExplorationCommandAction that = (ExplorationCommandAction) o;
            return command == that.command;
        }

        @Override
        public int hashCode() {
            return Objects.hash(command);
        }
    }
}
