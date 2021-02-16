package org.mate.exploration.genetic.algorithm;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mate.exploration.genetic.core.GAUtils.getParetoFront;
import static org.mate.exploration.genetic.core.GAUtils.updateCrowdingDistance;
import static org.mate.utils.MathUtils.isEpsEq;

public class NSGAII<T> extends GeneticAlgorithm<T> {

    public NSGAII(IChromosomeFactory<T> chromosomeFactory, ISelectionFunction<T> selectionFunction, ICrossOverFunction<T> crossOverFunction, IMutationFunction<T> mutationFunction, List<IFitnessFunction<T>> list, ITerminationCondition terminationCondition, int populationSize, int bigPopulationSize, double pCrossover, double pMutate) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction, list, terminationCondition, populationSize, bigPopulationSize, pCrossover, pMutate);
    }

    @Override
    public List<IChromosome<T>> getGenerationSurvivors() {
        List<IChromosome<T>> survivors = new ArrayList<>(population);
        final Map<IChromosome<T>, Integer> rankMap = new HashMap<>();
        final Map<IChromosome<T>, Double> crowdingDistanceMap = new HashMap<>();

        List<IChromosome<T>> remaining = new ArrayList<>(population);

        int rank = 0;

        while (!remaining.isEmpty()) {
            List<IChromosome<T>> paretoFront = getParetoFront(remaining, fitnessFunctions);

            for (IChromosome<T> chromosome : paretoFront) {
                remaining.remove(chromosome);
                rankMap.put(chromosome, rank);
            }

            updateCrowdingDistance(paretoFront, fitnessFunctions, crowdingDistanceMap);
            rank++;
        }
        Collections.sort(survivors, new RankComparator<>(rankMap, crowdingDistanceMap));
        return survivors.subList(0, populationSize);
    }

    static class RankComparator<T> implements Comparator<IChromosome<T>> {
        private final Map<IChromosome<T>, Integer> rankMap;
        private final Map<IChromosome<T>, Double> crowdingDistanceMap;

        RankComparator(Map<IChromosome<T>, Integer> rankMap, Map<IChromosome<T>, Double> crowdingDistanceMap) {
            this.rankMap = rankMap;
            this.crowdingDistanceMap = crowdingDistanceMap;
        }

        @Override
        public int compare(IChromosome<T> o1, IChromosome<T> o2) {
            final Integer o1Rank = rankMap.get(o1);
            if (o1Rank == null) {
                throw new IllegalStateException("Rank value not in rank map");
            }
            final Integer o2Rank = rankMap.get(o2);
            if (o2Rank == null) {
                throw new IllegalStateException("Rank value not in rank map");
            }

            int c = o1Rank.compareTo(o2Rank);
            if (c == 0) {
                final Double o1CrowdDistance = crowdingDistanceMap.get(o1);
                if (o1CrowdDistance == null) {
                    throw new IllegalStateException("Crowding distance not in crowding distance map");
                }
                final Double o2CrowdDistance = crowdingDistanceMap.get(o2);
                if (o2CrowdDistance == null) {
                    throw new IllegalStateException("Crowding distance not in crowding distance map");
                }

                if (isEpsEq(o1CrowdDistance, o2CrowdDistance)) {
                    return 0;
                }
                return 0 - o1CrowdDistance.compareTo(o2CrowdDistance);
            }
            return c;
        }
    }
}
