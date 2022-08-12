package org.mate.crash_reproduction.fitness;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.manual.AskUserExploration;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ActionFitnessFunctionWrapper implements IFitnessFunction<TestCase> {
    private final Map<String, Double> actionFitnessValues = new HashMap<>();
    private final IFitnessFunction<TestCase> fitnessFunction;

    public ActionFitnessFunctionWrapper(IFitnessFunction<TestCase> fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return fitnessFunction.isMaximizing();
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return getFitnessAfterXActions(chromosome, chromosome.getValue().getEventSequence().size());
    }

    public void recordCurrentActionFitness(IChromosome<TestCase> chromosome) {
        double fitness = fitnessFunction.getNormalizedFitness(chromosome);
        actionFitnessValues.put(Registry.getEnvironmentManager().getActionEntityId(chromosome), fitness);

        MATE.log("Testcase fitness after " + chromosome.getValue().getEventSequence().size() + " actions is: " + fitness);
    }

    public void writeExplorationStepsToFile(IChromosome<TestCase> chromosome) {
        String code = AskUserExploration.toMatPlotLibCode("exp_" + chromosome.getValue().getId() + ".png", IntStream.range(0, chromosome.getValue().getStateSequence().size())
                .mapToObj(actions -> getExplorationStep(chromosome, actions))
                .collect(Collectors.toList()));

        Registry.getEnvironmentManager().writeFile("exp_" + chromosome.getValue().getId() + ".py", code);
    }

    private AskUserExploration.ExplorationStep getExplorationStep(IChromosome<TestCase> chromosome, int actions) {
        IScreenState state = chromosome.getValue().getStateSequence().get(actions);
        return new AskUserExploration.ExplorationStep(
                state,
                state.getId(),
                getFitnessAfterXActions(chromosome, actions),
                Collections.emptyMap()
        );
    }

    public double getFitnessAfterXActions(IChromosome<TestCase> testCase, int actions) {
        return Objects.requireNonNull(actionFitnessValues.get(Registry.getEnvironmentManager().getActionEntityId(testCase, actions)));
    }
}
