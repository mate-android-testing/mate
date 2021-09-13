package org.mate.exploration.genetic.core;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.mate.utils.MathUtils.isEpsEq;

public class GAUtils {

    public static <T> void updateCrowdingDistance(List<IChromosome<T>> paretoFront,
                                                  List<IFitnessFunction<T>> fitnessFunctions,
                                                  Map<IChromosome<T>, Double> crowdingDistanceMap) {

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
                        double compared = fitnessFunction.getNormalizedFitness(o2)
                                - fitnessFunction.getNormalizedFitness(o1);
                        return fitnessFunction.isMaximizing()
                                ? compareFunctions(compared) : compareFunctions(compared) * (-1);
                    }

                    private int compareFunctions(double compared) {
                        if (isEpsEq(compared)) {
                            return 0;
                        } else if (compared < 0) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });

                double minObjective = fitnessFunction.getNormalizedFitness(paretoFront.get(0));
                double maxObjective = fitnessFunction.getNormalizedFitness(paretoFront.get(n - 1));

                if (!isEpsEq(minObjective, maxObjective)) {
                    crowdingDistanceMap.put(paretoFront.get(0), Double.POSITIVE_INFINITY);
                    crowdingDistanceMap.put(paretoFront.get(n - 1), Double.POSITIVE_INFINITY);

                    for (int i = 1; i < n - 1; i++) {
                        IChromosome<T> paretoElement = paretoFront.get(i);
                        double distance = crowdingDistanceMap.get(paretoElement);
                        distance += (fitnessFunction.getNormalizedFitness(paretoFront.get(i + 1)) -
                                fitnessFunction.getNormalizedFitness(paretoFront.get(i - 1)))
                                / (maxObjective - minObjective);
                        crowdingDistanceMap.put(paretoElement, distance);
                    }
                }

            }
        }
    }

    public static <T> double calculateDistance(IChromosome<T> c1, IChromosome<T> c2,
                                               List<IFitnessFunction<T>> fitnessFunctions) {
        double distance = 0.0;

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
            distance += Math.pow(fitnessFunction.getNormalizedFitness(c1)
                    - fitnessFunction.getNormalizedFitness(c2), 2.0);
        }
        return Math.sqrt(distance);
    }

    public static <T> List<IChromosome<T>> getParetoFront(List<IChromosome<T>> chromosomes,
                                                          List<IFitnessFunction<T>> fitnessFunctions) {
        List<IChromosome<T>> paretoFront = new ArrayList<>();

        for (IChromosome<T> chromosome : chromosomes) {
            boolean isDominated = false;
            Iterator<IChromosome<T>> iterator = paretoFront.iterator();
            while (iterator.hasNext()) {
                IChromosome<T> frontElement = iterator.next();
                boolean worseInOne = false;
                boolean betterInOne = false;
                for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
                    double compared = fitnessFunction.getNormalizedFitness(chromosome)
                            - fitnessFunction.getNormalizedFitness(frontElement);
                    if (isEpsEq(compared)) {
                        continue;
                    }
                    if (compared > 0) {
                        betterInOne = true;
                        if (worseInOne) {
                            break;
                        }
                    } else if (compared < 0) {
                        worseInOne = true;
                        if (betterInOne) {
                            break;
                        }
                    }
                }

                if (worseInOne && !betterInOne) {
                    isDominated = true;
                    break;
                }
                if (betterInOne && !worseInOne) {
                    iterator.remove();
                }
            }

            if (!isDominated) {
                paretoFront.add(chromosome);
            }
        }
        return paretoFront;
    }
}
