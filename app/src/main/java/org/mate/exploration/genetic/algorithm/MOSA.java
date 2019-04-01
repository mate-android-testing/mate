package org.mate.exploration.genetic.algorithm;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mate.exploration.genetic.core.GAUtils.getParetoFront;
import static org.mate.exploration.genetic.core.GAUtils.updateCrowdingDistance;

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
 * <p>
 * The result of MOSA are all test cases which are stored in the {@link #archive}, i.e. the shortest test cases
 * which fulfill provided test targets (here fitness functions).
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
     * Stores all fitness functions which have <emp>not yet</emp> been fulfilled and which criteria has not been met,
     * i.e. result of the fitness function is smaller than 1 for all chromosomes.
     */
    private List<IFitnessFunction<T>> uncoveredFitnessFunctions = new ArrayList<>();

    public MOSA(IChromosomeFactory<T> chromosomeFactory, ISelectionFunction<T> selectionFunction, ICrossOverFunction<T> crossOverFunction, IMutationFunction<T> mutationFunction, List<IFitnessFunction<T>> fitnessFunctions, ITerminationCondition terminationCondition, int populationSize, int bigPopulationSize, double pCrossover, double pMutate) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction, fitnessFunctions, terminationCondition, populationSize, bigPopulationSize, pCrossover, pMutate);

        uncoveredFitnessFunctions.addAll(fitnessFunctions);
    }

    @Override
    public void createInitialPopulation() {
        super.createInitialPopulation();
        updateArchive(population);
    }

    @Override
    public void evolve() {
        super.evolve();
        updateArchive(population);

        // Todo: remove. Memory issue dirty quick fix
        List<Object> activeChromosomes = new ArrayList<>();
        activeChromosomes.addAll(population);
        activeChromosomes.addAll(archive.values());
        LineCoveredPercentageFitnessFunction.cleanCache(activeChromosomes);
    }

    @Override
    public List<IChromosome<T>> getGenerationSurvivors() {
        List<IChromosome<T>> population = new ArrayList<>(this.population);

        final Map<IChromosome<T>, Integer> rankMap = new HashMap<>();
        final Map<IChromosome<T>, Double> crowdingDistanceMap = new HashMap<>();

        // List of chromosomes that fulfill a certain testing target best.
        final List<IChromosome<T>> preferredChromosomes = extractPreferred(population);
        for (IChromosome<T> chromosome : preferredChromosomes) {
            // MOSA best possible rank
            rankMap.put(chromosome, 0);
        }
        updateCrowdingDistance(preferredChromosomes, uncoveredFitnessFunctions, crowdingDistanceMap);

        // The following represents Algorithm 2 (lines 7-12) and algorithm 1 (lines 10-17):
        // Apply rank and crowding distance values for all non-preference sorted chromosomes
        final List<IChromosome<T>> remaining = new ArrayList<>(population);
        remaining.removeAll(preferredChromosomes);

        // Start at best possible rank in NSGA-II
        int rank = 1;

        while (!remaining.isEmpty()) {
            List<IChromosome<T>> paretoFront = getParetoFront(remaining, uncoveredFitnessFunctions);

            for (IChromosome<T> chromosome : paretoFront) {
                remaining.remove(chromosome);
                rankMap.put(chromosome, rank);
            }

            updateCrowdingDistance(paretoFront, uncoveredFitnessFunctions, crowdingDistanceMap);
            rank++;
        }

        // Sort all by rank and if rank is equal by crowding distance
        Collections.sort(population, new NSGAII.RankComparator<>(rankMap, crowdingDistanceMap));

        return population.subList(0, populationSize);
    }

    /**
     * Extract the best chromosomes for uncovered fitness functions for given population
     * based on <a href="https://ieeexplore.ieee.org/abstract/document/7102604">MOSA Algorithm 2 lines 1 - 6</a>.
     * <p>
     * If one chromosome fulfills one fitness function, the fitness function is removed from the
     * {@link #uncoveredFitnessFunctions uncovered fitness functions}.
     *
     * @return a list of current chromosomes that fit a uncovered fitness functions best.
     */
    private List<IChromosome<T>> extractPreferred(List<IChromosome<T>> population) {
        if (population.isEmpty()) {
            return new ArrayList<>();
        }
        final Set<IChromosome<T>> firstNonDominatedFront = new HashSet<>();

        // only look at fitness functions which have not been covered yet
        List<IFitnessFunction<T>> toRemove = new ArrayList<>();
        for (IFitnessFunction<T> fitnessFunction : uncoveredFitnessFunctions) {
            IChromosome<T> best = population.get(0);
            double bestFitness = fitnessFunction.getFitness(best);
            for (IChromosome<T> chrom : population) {
                final double chromFitness = fitnessFunction.getFitness(chrom);
                if (chromFitness > bestFitness) {
                    best = chrom;
                    bestFitness = chromFitness;
                }
            }

            // fitness function is now covered
            if (bestFitness == 1) {
                toRemove.add(fitnessFunction);
            }

            firstNonDominatedFront.add(best);
        }

        uncoveredFitnessFunctions.removeAll(toRemove);

        return new ArrayList<>(firstNonDominatedFront);
    }

    /**
     * Updates the archive based on <a href="https://ieeexplore.ieee.org/abstract/document/7102604">MOSA Algorithm 3</a>.
     *
     * @param possibleAdditions a list of possible additions to the archive.
     */
    private void updateArchive(List<IChromosome<T>> possibleAdditions) {
        final List<IChromosome<T>> allChromosomes = new ArrayList<>();
        allChromosomes.addAll(archive.values());
        allChromosomes.addAll(possibleAdditions);

        // Look at all fitness functions, even covered ones.
        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
            double bestLength = Double.POSITIVE_INFINITY;
            IChromosome<T> best = null;

            for (IChromosome<T> survivor : allChromosomes) {
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
