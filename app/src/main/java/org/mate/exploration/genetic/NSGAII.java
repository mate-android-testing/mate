package org.mate.exploration.genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NSGAII<T> extends GeneticAlgorithm<T> {
    public static final String ALGORITHM_NAME = "NSGA-II";
    public static final double EPS = 1e-10;

    public NSGAII(IChromosomeFactory chromosomeFactory, ISelectionFunction selectionFunction, ICrossOverFunction crossOverFunction, IMutationFunction mutationFunction, List list, ITerminationCondition terminationCondition, int populationSize, int generationSurvivorCount, float pCrossover, float pMutate) {
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
                    return -1 * crowdingDistanceMap.get(o1).compareTo(crowdingDistanceMap.get(o2));
                }
                return c;
            }
        });
        return survivors.subList(0, generationSurvivorCount);
    }

    private void updateCrowdingDistance(List<IChromosome<T>> paretoFront, List<IFitnessFunction<T>> fitnessFunctions, Map<IChromosome<T>, Double> crowdingDistanceMap) {
        for (IChromosome<T> chromosome : paretoFront) {
            crowdingDistanceMap.put(chromosome, 0.0);
        }

        List<IChromosome<T>> uniqueFront = new ArrayList<>();

        for (IChromosome<T> c1 : paretoFront) {
            boolean isDuplicate = false;

            for (IChromosome<T> c2 : uniqueFront) {
                if (isEpsEq(calculateDistance(c1, c2, fitnessFunctions))) {
                    isDuplicate = true;
                    break;
                }
            }

            if (!isDuplicate) {
                uniqueFront.add(c1);
            }
        }

        paretoFront = uniqueFront;

        // then compute the crowding distance for the unique solutions
        int n = paretoFront.size();

        if (n < 3) {
            for (IChromosome<T> chromosome : paretoFront) {
                crowdingDistanceMap.put(chromosome, Double.POSITIVE_INFINITY);
            }
        } else {
            for (final IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
                Collections.sort(paretoFront, new Comparator<IChromosome<T>>() {
                    @Override
                    public int compare(IChromosome<T> o1, IChromosome<T> o2) {
                        double compared = fitnessFunction.getFitness(o2) - fitnessFunction.getFitness(o1);
                        if (isEpsEq(compared)) {
                            return 0;
                        } else if (compared < 0) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });

                double minObjective = fitnessFunction.getFitness(paretoFront.get(0));
                double maxObjective = fitnessFunction.getFitness(paretoFront.get(n -1));

                if (!isEpsEq(minObjective, maxObjective)) {
                    crowdingDistanceMap.put(paretoFront.get(0), Double.POSITIVE_INFINITY);
                    crowdingDistanceMap.put(paretoFront.get(n - 1), Double.POSITIVE_INFINITY);

                    for (int i = 1; i < n - 1; i++) {
                        IChromosome<T> paretoElement = paretoFront.get(i);
                        double distance = crowdingDistanceMap.get(paretoElement);
                        distance += (fitnessFunction.getFitness(paretoFront.get(i + 1)) -
                                fitnessFunction.getFitness(paretoFront.get(i - 1)))
                                / (maxObjective - minObjective);
                        crowdingDistanceMap.put(paretoElement, distance);
                    }
                }

            }
        }
    }

    private double calculateDistance(IChromosome<T> c1, IChromosome<T> c2, List<IFitnessFunction<T>> fitnessFunctions) {
        double distance = 0.0;

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
            distance += Math.pow(fitnessFunction.getFitness(c1) - fitnessFunction.getFitness(c2), 2.0);
        }
        return Math.sqrt(distance);
    }

    private List<IChromosome<T>> getParetoFront(List<IChromosome<T>> remaining, List<IFitnessFunction<T>> fitnessFunctions) {
        List<IChromosome<T>> paretoFront = new ArrayList<>();

        for (IChromosome<T> chromosome : remaining) {
            boolean isDominated = false;
            Iterator<IChromosome<T>> iterator = paretoFront.iterator();
            while (iterator.hasNext()) {
                IChromosome<T> frontElement = iterator.next();
                boolean worseInOne = false;
                boolean isBetterInOne = false;
                for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
                    double compared = fitnessFunction.getFitness(chromosome) - fitnessFunction.getFitness(frontElement);
                    if (isEpsEq(compared)) {
                        continue;
                    }
                    if (compared > 0) {
                        isBetterInOne = true;
                    } else if (compared < 0) {
                        worseInOne = true;
                    }
                }

                if (worseInOne && !isBetterInOne) {
                    isDominated = true;
                    break;
                }
                if (isBetterInOne && !worseInOne) {
                    iterator.remove();
                }
            }

            if (!isDominated) {
                paretoFront.add(chromosome);
            }
        }
        return paretoFront;
    }

    private boolean isEpsEq(double a, double b) {
        return Math.abs(a - b) < EPS;
    }

    private boolean isEpsEq(double a) {
        return Math.abs(a) < EPS;
    }
}
