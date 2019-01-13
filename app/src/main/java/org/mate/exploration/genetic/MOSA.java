package org.mate.exploration.genetic;

import org.mate.model.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mate.exploration.genetic.GAUtils.getParetoFront;
import static org.mate.exploration.genetic.GAUtils.updateCrowdingDistance;

/**
 * Implementation of the Many-Objective Sorting Algorithm (MOSA) based on the paper:
 * <a href="https://ieeexplore.ieee.org/abstract/document/7102604">Reformulating Branch Coverage as a Many-Objective Optimization Problem</a>
 * <p>
 * In contrast to the proposed algorithms in the paper which minimises each testing target, this implementation
 * <emp>maximises</emp> each fitness function to ease integration with other genetic algorithm implementations.
 * <p>
 * In the paper, a fitness function is fulfilled once it yields 0 (e.g. for branch distance: covers that branch).
 * In this implementation, a fitness function is fulfilled when it yields 1. That means, the fitness function values
 * have to be between {@code [0.0, 1.0]}.
 *
 * @param <T> Type wrapped by the chromosome implementation. Has to be a {@link TestCase} or sub class.
 */
public class MOSA<T extends TestCase> extends GeneticAlgorithm<T> {
    public static final String ALGORITHM_NAME = "MOSA";

    /**
     * <em>"MOSA uses a second population, called archive, to keep track of the best test cases that cover branches
     * (here: fulfills a fitness function best) of the program under test."</em>
     * <p>
     * Stores a mapping from fitness functions to the chromosome, which fulfills the fitness functions best.
     */
    private Map<IFitnessFunction<T>, IChromosome<T>> archive = new HashMap<>();
    /**
     * Stores all fitness functions which have already been covered and which criteria have been met, i.e. result
     * of the fitness function is 0 for at least one chromosome.
     */
    private List<IFitnessFunction> coveredFitnessFunctions = new ArrayList<>();

    public MOSA(IChromosomeFactory<T> chromosomeFactory, ISelectionFunction<T> selectionFunction, ICrossOverFunction<T> crossOverFunction, IMutationFunction<T> mutationFunction, List<IFitnessFunction<T>> fitnessFunctions, ITerminationCondition terminationCondition, int populationSize, int generationSurvivorCount, double pCrossover, double pMutate) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction, fitnessFunctions, terminationCondition, populationSize, generationSurvivorCount, pCrossover, pMutate);
    }

    @Override
    public void createInitialPopulation() {
        super.createInitialPopulation();
        updateArchive(population);
    }

    @Override
    public List<IChromosome<T>> getGenerationSurvivors() {
        List<IChromosome<T>> survivors = new ArrayList<>(population);

        List<IChromosome<T>> nextSurvivors = preferenceSorting(survivors).subList(0, generationSurvivorCount);

        updateArchive(nextSurvivors);

        return nextSurvivors;
    }

    /**
     * Sorting the current population based on <a href="https://ieeexplore.ieee.org/abstract/document/7102604">MOSA Algorithm 2</a>.
     * <p>
     * The returned sorted list contains the so far best chromosomes for all fitness functions.
     *
     * @param population a list of chromosomes to be sorted by preference.
     *
     * @return a sorted updated population matching all fitness functions best.
     */
    private List<IChromosome<T>> preferenceSorting(List<IChromosome<T>> population) {
        if (population.isEmpty()) {
            return population;
        }
        final Map<IChromosome<T>, Integer> rankMap = new HashMap<>();
        final Map<IChromosome<T>, Double> crowdingDistanceMap = new HashMap<>();

        final List<IChromosome<T>> remaining = new ArrayList<>(population);

        final Set<IChromosome<T>> firstNonDominatedFront = new HashSet<>();
        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {

            // only look at fitness functions which have not been covered yet
            if (!coveredFitnessFunctions.contains(fitnessFunction)) {
                IChromosome<T> best = population.get(0);
                double bestFitness = fitnessFunction.getFitness(best);
                for (IChromosome<T> chrom : population) {
                    final double chromFitness = fitnessFunction.getFitness(chrom);
                    if (chromFitness > bestFitness) {
                        best = chrom;
                        bestFitness = chromFitness;
                    }
                }

                // fitness function is covered
                if (bestFitness == 1) {
                    coveredFitnessFunctions.add(fitnessFunction);
                }
                // Rank 0
                rankMap.put(best, 0);

                firstNonDominatedFront.add(best);
            }
        }

        remaining.removeAll(firstNonDominatedFront);

        if (!remaining.isEmpty()) {
            int rank = 1;

            // apply rank and crowding distance values
            while (!remaining.isEmpty()) {
                List<IChromosome<T>> paretoFront = getParetoFront(remaining, fitnessFunctions);

                for (IChromosome<T> chromosome : paretoFront) {
                    remaining.remove(chromosome);
                    rankMap.put(chromosome, rank);
                }

                updateCrowdingDistance(paretoFront, fitnessFunctions, crowdingDistanceMap);
                rank++;
            }
        }

        // Sort all by rank and if rank is equal by crowding distance
        Collections.sort(population, new NSGAII.RankComparator<>(rankMap, crowdingDistanceMap));

        return population;
    }

    /**
     * Updates the archive based on <a href="https://ieeexplore.ieee.org/abstract/document/7102604">MOSA Algorithm 3</a>.
     *
     * @param possibleAdditions a list of possible additions to the archive.
     */
    private void updateArchive(List<IChromosome<T>> possibleAdditions) {
        possibleAdditions.addAll(archive.values());

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
            double bestLength = Double.POSITIVE_INFINITY;
            IChromosome<T> best = null;

            for (IChromosome<T> survivor : possibleAdditions) {
                final double score = fitnessFunction.getFitness(survivor);
                final double length = survivor.getValue().getEventSequence().size();

                if (score == 1 && length <= bestLength) {
                    best = survivor;
                    bestLength = length;
                }
            }
            if (best != null) {
                archive.put(fitnessFunction, best);
            }
        }
    }
}
