package org.mate.exploration.manual;

import android.support.annotation.NonNull;

import org.mate.Properties;
import org.mate.Registry;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AskUserExploration implements Algorithm {
    private final CrashDistance fitnessFunction = new CrashDistance();
    private int chromosomeCounter = 0;

    @Override
    public void run() {
        while (true) {
            List<ExplorationStep> explorationSteps = getExplorationSteps();
            Registry.getEnvironmentManager().writeFile("exp_" + chromosomeCounter + ".py", toMatPlotLibCode("exp_" + chromosomeCounter + "_graph.png", explorationSteps, true));
            chromosomeCounter++;
        }
    }

    private List<ExplorationStep> getExplorationSteps() {
        Optional<List<ExplorationStep>> steps;
        do {
            steps = getExplorationStepsUnsafe();
        } while (!steps.isPresent());
        return steps.get();
    }

    private Optional<List<ExplorationStep>> getExplorationStepsUnsafe() {
        List<ExplorationStep> explorationSteps = new LinkedList<>();
        Registry.getUiAbstractionLayer().resetApp();

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);
        explorationSteps.add(createStep(chromosome));

        try {
            for (int actionsCount = 0; actionsCount < Properties.MAX_NUMBER_EVENTS(); actionsCount++) {
                Either<Action, ExplorationCommand> actionOrCommand = askUserToPickAction();
                boolean stop;

                if (actionOrCommand.hasLeft()) {
                    stop = !testCase.updateTestCase(actionOrCommand.getLeft(), actionsCount);
                    explorationSteps.add(createStep(chromosome));
                } else {
                    switch (actionOrCommand.getRight()) {
                        case STOP: stop = true; break;
                        case RESET: return Optional.empty();
                        default: throw new IllegalArgumentException("Unknown command: " + actionOrCommand.getRight().description);
                    }
                }

                if (stop) {
                    return Optional.of(explorationSteps);
                }
            }
        } finally {
            testCase.finish();
        }

        return Optional.of(explorationSteps);
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

    public static String toMatPlotLibCode(String fileName, List<ExplorationStep> steps, boolean useNodeIdForImage) {
        String states = "[" + steps.stream().map(s -> '"' + s.nodeId + '"').collect(Collectors.joining(",")) + "]";
        String fitnessValues = "[" + steps.stream().map(s -> String.valueOf(s.fitness)).collect(Collectors.joining(",")) + "]";
        String imageLookup = "{" + steps.stream()
                .map(s -> '"' + s.nodeId + "\":\"" + (useNodeIdForImage ? s.nodeId : s.state.getId()) + '"')
                .collect(Collectors.joining(", "))
                + "}";
        String imageLabelLookup = "{" + steps.stream()
                .map(s -> '"' + s.nodeId + "\":\"\"\"" + s.state.getId() + " Fitness: " + String.format("%,.2f", s.fitness) + "\n" + s.fitnessVector.entrySet().stream().map(e -> e.getKey() + ": " + String.format("%,.2f", e.getValue())).collect(Collectors.joining("\n")) + "\"\"\"")
                .collect(Collectors.joining(", ")) + "}";

        return "import matplotlib.pyplot as plt\n" +
                "from matplotlib.offsetbox import OffsetImage, AnnotationBbox, TextArea\n" +
                "\n" +
                "lookup = " + imageLabelLookup + "\n" +
                "lookupImage = " + imageLookup + "\n" +
                "def estimate_height(label):\n" +
                "    num_lines = label.count('\\n')\n" +
                "    return num_lines * 20\n" +
                "\n" +
                "def offset_image(x, ax):\n" +
                "    img = plt.imread(f'pictures/" + Registry.getPackageName() + "/{lookupImage[x]}.png')\n" +
                "    im = OffsetImage(img, zoom=0.3)\n" +
                "    im.label = lookup[x]\n" +
                "    im.image.axes = ax\n" +
                "    ab = AnnotationBbox(im, (x, 0), xybox=(0, -320), frameon=False,\n" +
                "                        xycoords='data', boxcoords=\"offset points\", pad=0)\n" +
                "    ax.add_artist(ab)\n" +
                "    ab = AnnotationBbox(TextArea(lookup[x], textprops=dict(size=22)), (x, 0), xybox=(0, -650 - estimate_height(lookup[x])), frameon=False,\n" +
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
                "plt.savefig('" + fileName + "', bbox_inches=\"tight\")\n";
    }

    private ExplorationStep createStep(IChromosome<TestCase> chromosome) {
        Registry.getEnvironmentManager().storeActionFitnessData(chromosome);
        double fitness = fitnessFunction.getNormalizedFitness(chromosome);

        String nodeId = "exp_" + chromosomeCounter + "_step_" + chromosome.getValue().getEventSequence().size();
        Registry.getEnvironmentManager().takeScreenshot(Registry.getPackageName(), nodeId);
        Registry.getEnvironmentManager().drawCallGraph(nodeId + "_graph", chromosome);
        List<Widget> widgets = Registry.getUiAbstractionLayer().getPromisingActions(Registry.getUiAbstractionLayer().getLastScreenState()).stream().map(WidgetAction::getWidget).collect(Collectors.toList());

        if (!widgets.isEmpty()) {
            Registry.getEnvironmentManager().markOnImage(widgets, nodeId);
        }

        return new ExplorationStep(Registry.getUiAbstractionLayer().getLastScreenState(), nodeId, fitness,
                fitnessFunction.getWeightedFitnessFunctions().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getClass().getSimpleName(), e -> e.getKey().getNormalizedFitness(chromosome))));
    }

    public static class ExplorationStep {
        private final IScreenState state;
        private final String nodeId;
        private final double fitness;
        private final Map<String, Double> fitnessVector;

        public ExplorationStep(IScreenState state, String nodeId, double fitness, Map<String, Double> fitnessVector) {
            this.state = state;
            this.nodeId = nodeId;
            this.fitness = fitness;
            this.fitnessVector = fitnessVector;
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
