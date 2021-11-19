package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.NoveltyFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.selection.NoveltyRankSelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A novelty search implementation following the paper 'A Novelty Search Approach for Automatic Test
 * Data Generation', see https://hal.archives-ouvertes.fr/hal-01121228/document. This is basically
 * a standard genetic algorithm extended by an archive where the selection and fitness function are
 * replaced with functions that focus on novelty.
 *
 * @param <T> Refers to either a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class NoveltySearch<T> extends GeneticAlgorithm<T> {

    /**
     * The archive containing the most diverse chromosomes.
     */
    private List<IChromosome<T>> archive;

    /**
     * The maximal size of the archive, denoted as L.
     */
    private final int archiveLimit;

    /**
     * The novelty threshold T.
     */
    private final double noveltyThreshold;

    /**
     * The number of nearest neighbours that should be considered, denoted as k.
     */
    private final int nearestNeighbours;

    /**
     * The novelty fitness function.
     */
    private final NoveltyFitnessFunction<T> noveltyFitnessFunction;

    /**
     * The novelty selection function.
     */
    private final NoveltyRankSelectionFunction<T> noveltySelectionFunction;

    /**
     * A mapping of a chromosome to its novelty.
     */
    private final Map<IChromosome<T>, Double> noveltyScores = new HashMap<>();

    /**
     * Initializes the genetic algorithm with all the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param selectionFunction The used selection function.
     * @param crossOverFunction The used crossover function.
     * @param mutationFunction The used mutation function.
     * @param fitnessFunctions The used fitness/novelty function.
     * @param terminationCondition The used termination condition.
     * @param populationSize The population size.
     * @param bigPopulationSize The big population size.
     * @param pCrossover The probability for crossover.
     * @param pMutate The probability for mutation.
     * @param archiveLimit The maximal size (L) of the archive.
     * @param noveltyThreshold The novelty threshold T.
     * @param nearestNeighbours The number of nearest neighbours k.
     */
    public NoveltySearch(IChromosomeFactory<T> chromosomeFactory,
                         ISelectionFunction<T> selectionFunction,
                         ICrossOverFunction<T> crossOverFunction,
                         IMutationFunction<T> mutationFunction,
                         List<IFitnessFunction<T>> fitnessFunctions,
                         ITerminationCondition terminationCondition,
                         int populationSize,
                         int bigPopulationSize,
                         double pCrossover,
                         double pMutate,
                         int nearestNeighbours,
                         int archiveLimit,
                         double noveltyThreshold) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction,
                fitnessFunctions, terminationCondition, populationSize, bigPopulationSize,
                pCrossover, pMutate);
        this.archive = new LinkedList<>(); // the archive not bigger than limit L
        this.nearestNeighbours = nearestNeighbours; // the number of nearest neighbours k
        this.archiveLimit = archiveLimit; // the archive limit L
        this.noveltyThreshold = noveltyThreshold; // the novelty threshold T
        this.noveltyFitnessFunction = (NoveltyFitnessFunction<T>) fitnessFunctions.get(0);
        this.noveltySelectionFunction = (NoveltyRankSelectionFunction<T>) selectionFunction;
    }

    /**
     * Creates the initial population consisting of random chromosomes. Also updates the archive.
     */
    @Override
    public void createInitialPopulation() {

        MATE.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");

        for (int i = 0; i < populationSize; i++) {
            IChromosome<T> chromosome = chromosomeFactory.createChromosome();
            updateArchive(chromosome, population, archive);
            population.add(chromosome);
        }

        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Represents the evolution process. In the context of novelty search, we stick here to
     * the procedure of a standard genetic algorithm. The only difference is that we update our
     * archive accordingly and that we use a specialised selection as well as fitness function.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        /*
        * As an offspring may represent a duplicate of the population, because no crossover and
        * no mutation was applied, the novelty of this offspring would decrease, which in turn
        * would influence the next selection process. Thus, we should work with a copy of the saved
        * novelty scores.
         */
        Map<IChromosome<T>, Double> noveltyScoresCopy = Collections.unmodifiableMap(noveltyScores);

        while (newGeneration.size() < bigPopulationSize) {

            List<IChromosome<T>> parents = noveltySelectionFunction.select(population, noveltyScoresCopy);

            IChromosome<T> parent;

            if (Randomness.getRnd().nextDouble() < pCrossover) {
                parent = crossOverFunction.cross(parents);
            } else {
                parent = parents.get(0);
            }

            List<IChromosome<T>> offspring = new ArrayList<>();

            if (Randomness.getRnd().nextDouble() < pMutate) {
                offspring = mutationFunction.mutate(parent);
            } else {
                offspring.add(parent);
            }

            // TODO: if the offspring was not changed at all by crossover and mutation, we need to copy
            //  the fitness data

            for (IChromosome<T> chromosome : offspring) {
                if (newGeneration.size() == bigPopulationSize) {
                    break;
                } else {

                    if (population.contains(offspring)) {
                        MATE.log_acc("Offspring " + offspring + " represents duplicate!");
                    }

                    updateArchive(chromosome, newGeneration, archive);
                    newGeneration.add(chromosome);
                }
            }
        }

        population.clear();
        population.addAll(newGeneration);
        List<IChromosome<T>> survivors = getGenerationSurvivors();
        population.clear();
        population.addAll(survivors);
        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Logs the novelty of the chromosomes in the current population and the archive.
     */
    @Override
    protected void logCurrentFitness() {

        MATE.log_acc("Novelty of generation #" + (currentGenerationNumber + 1) + " :");

        MATE.log_acc("Novelty of chromosomes in population: ");
        for (IChromosome<T> chromosome : population) {
            MATE.log_acc("Chromosome " + chromosome + ": " + noveltyScores.get(chromosome));
        }

        MATE.log_acc("Novelty of chromosomes in archive: ");
        for (IChromosome<T> chromosome : archive) {
            MATE.log_acc("Chromosome " + chromosome + ": " + noveltyScores.get(chromosome));
        }

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
            MATE.log_acc("Combined coverage until now: "
                    + CoverageUtils.getCombinedCoverage(Properties.COVERAGE()));
            if (population.size() <= 10) {
                MATE.log_acc("Combined coverage of current population: "
                        + CoverageUtils.getCombinedCoverage(Properties.COVERAGE(), population));
            }
        }
    }

    /**
     * Updates the archive with the new chromosome if one of the following conditions hold:
     *
     * (1) If the archive is empty.
     * (2) The new chromosome has at least a novelty > {@link #noveltyThreshold} and the archive
     *      is not full yet.
     * (3) The new chromosome has at least a novelty > {@link #noveltyThreshold} and it is more
     *      diverse than at least a single other chromosome in the current archive. This will
     *      replace the new chromosome with the worst chromosome in the archive.
     *
     * @param chromosome The new chromosome (already contained in the current population!).
     * @param population The current population.
     * @param archive The current archive.
     */
    private void updateArchive(IChromosome<T> chromosome, List<IChromosome<T>> population,
                               List<IChromosome<T>> archive) {

        if (archive.isEmpty()) {
            // the first chromosome always goes into the archive
            noveltyScores.put(chromosome, 1.0);
            archive.add(chromosome);
        } else {

            double novelty = noveltyFitnessFunction.getFitness(chromosome, population, archive, nearestNeighbours);
            noveltyScores.put(chromosome, novelty);

            if (novelty > noveltyThreshold) {
                archive.add(chromosome);
            }

            // TODO: replace with worst chromosome in archive or replace with oldest chromosome
            //  this may requires a re-evaluation of the novelty scores
            if (archive.size() == archiveLimit) {
                MATE.log_acc("Pre-defined archive limit is reached!");
            }
        }
    }
}
