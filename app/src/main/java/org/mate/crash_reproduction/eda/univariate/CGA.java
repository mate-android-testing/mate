package org.mate.crash_reproduction.eda.univariate;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CGA extends VectorBasedDistributionModel<Double> {
    private final double lambda = 1D;
    private final IFitnessFunction<TestCase> fitnessFunction;

    public CGA(IFitnessFunction<TestCase> fitnessFunction) {
        super((state, action) -> Registry.getUiAbstractionLayer().getPromisingActions(state).contains(action) ? 1D : 1D, 0.000001);
        this.fitnessFunction = fitnessFunction;
    }

    @Override
    public void update(Collection<IChromosome<TestCase>> population) {
        List<IChromosome<TestCase>> sortedPopulation = population.stream()
                .sorted(Comparator.comparingDouble(fitnessFunction::getNormalizedFitness))
                .collect(Collectors.toList());

        IChromosome<TestCase> bestChromosome = sortedPopulation.get(0);
        IChromosome<TestCase> worstChromosome = sortedPopulation.get(sortedPopulation.size() - 1);

        double bestFitness = getMaximisingFitness(bestChromosome);
        double worstFitness = getMaximisingFitness(worstChromosome);

        List<Action> bestActions = bestChromosome.getValue().getEventSequence();
        List<Action> worstActions = worstChromosome.getValue().getEventSequence();

        for (int i = 0; i < Properties.MAX_NUMBER_EVENTS(); i++) {
            if (i < bestActions.size() && i < worstActions.size() && bestActions.get(i).equals(worstActions.get(i))) {
                // Action is both in worst and best sequence
                stateActionTree.updateWeightOfAction(i, bestActions.get(i), weight -> weight + lambda * (bestFitness - worstFitness));
            } else {
                if (i < bestActions.size()) {
                    stateActionTree.updateWeightOfAction(i, bestActions.get(i), weight -> weight + lambda * bestFitness);
                }

                if (i < worstActions.size()) {
                    stateActionTree.updateWeightOfAction(i, worstActions.get(i), weight -> weight - lambda * (1 - worstFitness));
                }
            }
        }
    }

    private double getMaximisingFitness(IChromosome<TestCase> chromosome) {
        return fitnessFunction.isMaximizing()
                ? fitnessFunction.getNormalizedFitness(chromosome)
                : 1 - fitnessFunction.getNormalizedFitness(chromosome);
    }
}
