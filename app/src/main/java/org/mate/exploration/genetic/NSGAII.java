package org.mate.exploration.genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mate.exploration.genetic.GAUtils.getParetoFront;
import static org.mate.exploration.genetic.GAUtils.updateCrowdingDistance;
import static org.mate.utils.MathUtils.isEpsEq;

public class NSGAII<T> extends GeneticAlgorithm<T> {
    public static final String ALGORITHM_NAME = "NSGA-II";

    public NSGAII(IChromosomeFactory chromosomeFactory, ISelectionFunction selectionFunction, ICrossOverFunction crossOverFunction, IMutationFunction mutationFunction, List list, ITerminationCondition terminationCondition, int populationSize, int generationSurvivorCount, double pCrossover, double pMutate) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction, list, terminationCondition, populationSize, generationSurvivorCount, pCrossover, pMutate);
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

        Collections.sort(survivors, new Comparator<IChromosome<T>>() {
            @Override
            public int compare(IChromosome<T> o1, IChromosome<T> o2) {
                int c = rankMap.get(o1).compareTo(rankMap.get(o2));
                if (c == 0) {
                    double cd = crowdingDistanceMap.get(o1).compareTo(crowdingDistanceMap.get(o2));
                    if (isEpsEq(cd)) {
                        return 0;
                    }
                    if (cd < 0) {
                        return 1;
                    }
                    return -1;
                }
                return c;
            }
        });
        return survivors.subList(0, generationSurvivorCount);
    }

}
