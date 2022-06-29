package org.mate.crash_reproduction.eda.univariate;

import android.util.Pair;

import org.mate.crash_reproduction.eda.representation.IModelRepresentation;
import org.mate.crash_reproduction.eda.representation.TestCaseModelIterator;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CGA extends RepresentationBasedModel {
    private final double lambda = 1D;
    private final IFitnessFunction<TestCase> fitnessFunction;

    public CGA(IModelRepresentation modelRepresentation, IFitnessFunction<TestCase> fitnessFunction) {
        super(modelRepresentation);
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

        TestCaseModelIterator bestTestCase = new TestCaseModelIterator(modelRepresentation.getIterator(), bestChromosome.getValue());
        TestCaseModelIterator worstTestCase = new TestCaseModelIterator(modelRepresentation.getIterator(), worstChromosome.getValue());

        Map<Map<Action, Double>, Pair<List<Action>, List<Action>>> probabilityMapToUpdates = new IdentityHashMap<>();

        while (bestTestCase.hasNext()) {
            TestCaseModelIterator.NodeWithPickedAction node = bestTestCase.next();
            probabilityMapToUpdates.computeIfAbsent(node.getActionProbabilities(), a -> Pair.create(new LinkedList<>(), new LinkedList<>()))
                    .first.add(node.action);
        }

        while (worstTestCase.hasNext()) {
            TestCaseModelIterator.NodeWithPickedAction node = worstTestCase.next();
            probabilityMapToUpdates.computeIfAbsent(node.getActionProbabilities(), a -> Pair.create(new LinkedList<>(), new LinkedList<>()))
                    .second.add(node.action);
        }

        for (Map.Entry<Map<Action, Double>, Pair<List<Action>, List<Action>>> entry : probabilityMapToUpdates.entrySet()) {
            Map<Action, Double> probabilities = entry.getKey();

            List<Action> bestActions = entry.getValue().first;
            List<Action> worstActions = entry.getValue().second;
            Set<Action> worstAndBestActions = intersection(entry.getValue().first, entry.getValue().second);
            entry.getValue().first.removeAll(worstAndBestActions);
            entry.getValue().second.removeAll(worstAndBestActions);

            for (Action bestAction : bestActions) {
                probabilities.put(bestAction, probabilities.get(bestAction) + lambda * bestFitness);
            }

            for (Action worstAction : worstActions) {
                probabilities.put(worstAction, probabilities.get(worstAction) - lambda * (1 - worstFitness));
            }

            for (Action worstAndBestAction : worstAndBestActions) {
                probabilities.put(worstAndBestAction, probabilities.get(worstAndBestAction) + lambda * (bestFitness - worstFitness));
            }
        }
    }

    private <T> Set<T> intersection(Collection<T> c1, Collection<T> c2) {
        Set<T> intersection = new HashSet<>();

        for (T elem : c1) {
            if (c2.contains(elem)) {
                intersection.add(elem);
            }
        }

        return intersection;
    }

    private double getMaximisingFitness(IChromosome<TestCase> chromosome) {
        return fitnessFunction.isMaximizing()
                ? fitnessFunction.getNormalizedFitness(chromosome)
                : 1 - fitnessFunction.getNormalizedFitness(chromosome);
    }
}
