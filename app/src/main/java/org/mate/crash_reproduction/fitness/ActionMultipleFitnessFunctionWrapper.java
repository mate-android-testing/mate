package org.mate.crash_reproduction.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.manual.AskUserExploration;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.ChromosomeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionMultipleFitnessFunctionWrapper extends ActionFitnessFunctionWrapper implements IMultipleFitnessFunctions<TestCase> {

    private final IMultipleFitnessFunctions<TestCase> multipleFitnessFunctions;
    private final Map<IFitnessFunction<TestCase>, Map<String, Double>> actionFitnessValuesPerFunction = new HashMap<>();

    public ActionMultipleFitnessFunctionWrapper(IFitnessFunction<TestCase> fitnessFunction) {
        super(fitnessFunction);
        this.multipleFitnessFunctions = IMultipleFitnessFunctions.wrapIfNecessary(fitnessFunction);
    }

    @Override
    public void recordCurrentActionFitness(IChromosome<TestCase> chromosome) {
        super.recordCurrentActionFitness(chromosome);
        for (IFitnessFunction<TestCase> fitnessFunction : multipleFitnessFunctions.getInnerFitnessFunction()) {
            actionFitnessValuesPerFunction.computeIfAbsent(fitnessFunction, f -> new HashMap<>())
                    .put(ChromosomeUtils.getActionEntityId(chromosome), fitnessFunction.getNormalizedFitness(chromosome));
        }
    }

    @Override
    protected AskUserExploration.ExplorationStep getExplorationStep(IChromosome<TestCase> chromosome, int actions) {

        final String entityId = ChromosomeUtils.getActionEntityId(chromosome, actions);
        final IScreenState state = Registry.getUiAbstractionLayer().getGuiModel().getScreenStateById(entityId);

        return new AskUserExploration.ExplorationStep(
                state,
                "action_" + actions,
                getFitnessAfterXActions(chromosome, actions),
                actionFitnessValuesPerFunction.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getClass().getSimpleName(), e -> Objects.requireNonNull(e.getValue().get(entityId))))
        );
    }

    @Override
    public Set<IFitnessFunction<TestCase>> getInnerFitnessFunction() {
        return multipleFitnessFunctions.getInnerFitnessFunction();
    }
}
