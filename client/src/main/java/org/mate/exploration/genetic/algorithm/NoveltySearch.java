package org.mate.exploration.genetic.algorithm;

import android.util.Pair;

import org.mate.Properties;
import org.mate.commons.utils.MATELog;
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
import org.mate.commons.utils.Randomness;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
     * The archive contains the most novel chromosomes over the entire run.
     */
    private final List<IChromosome<T>> archive = new LinkedList<>();

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
     * Saves pairs where a pair associates a chromosome with its novelty value.
     */
    private final List<Pair<IChromosome<T>, Double>> noveltyPairs = new ArrayList<>();

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

        MATELog.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");

        for (int i = 0; i < populationSize; i++) {
            IChromosome<T> chromosome = chromosomeFactory.createChromosome();

            double novelty = noveltyFitnessFunction
                    .getFitness(chromosome, population, archive, nearestNeighbours);
            noveltyPairs.add(new Pair<>(chromosome, novelty));

            population.add(chromosome);
            updateArchive(chromosome, archive);
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

        MATELog.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        /*
        * The selection process should not be influenced by changes on the underlying novelty pairs.
        * In particular, we don't want to negatively influence the selection by introducing an
        * offspring that neither went through crossover or mutation, thus leading a low novelty value.
        * Thus, we work on a copy of the novelty pairs for the selection process.
         */
        List<Pair<IChromosome<T>, Double>> noveltyPairsCopy = Collections.unmodifiableList(noveltyPairs);

        while (newGeneration.size() < bigPopulationSize) {

            List<IChromosome<T>> parents = noveltySelectionFunction.select(noveltyPairsCopy);

            IChromosome<T> parent;

            if (Randomness.getRnd().nextDouble() < pCrossover) {
                parent = crossOverFunction.cross(parents).get(0);
            } else {
                parent = parents.get(0);
            }

            IChromosome<T> offspring;

            if (Randomness.getRnd().nextDouble() < pMutate) {
                offspring = mutationFunction.mutate(parent);
            } else {
                offspring = parent;
            }

            /*
            * TODO: Should we exclude in the new generation the current population? At least this
            *  would be consistent with the invocation of getFitness() in createInitialPopulation().
             */
            double novelty = noveltyFitnessFunction
                    .getFitness(offspring, newGeneration, archive, nearestNeighbours);
            noveltyPairs.add(new Pair<>(offspring, novelty));

            newGeneration.add(offspring);
            updateArchive(offspring, archive);
        }

        population.clear();
        population.addAll(newGeneration);
        List<IChromosome<T>> survivors = getGenerationSurvivors();
        population.clear();
        population.addAll(survivors);

        // TODO: This may remove the wrong pairs since there can be duplicates!
        List<Pair<IChromosome<T>, Double>> toBeRemoved = noveltyPairs.stream()
                .filter(pair -> !survivors.contains(pair.first))
                .collect(Collectors.toList());
        noveltyPairs.removeAll(toBeRemoved);

        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Logs the novelty of the chromosomes in the current population.
     */
    @Override
    protected void logCurrentFitness() {

        MATELog.log_acc("Novelty of generation #" + (currentGenerationNumber + 1) + " :");

        MATELog.log_acc("Novelty of chromosomes in population: ");
        for (Pair<IChromosome<T>, Double> chromosome : noveltyPairs) {
            MATELog.log_acc("Chromosome " + chromosome.first + ": " + chromosome.second);
        }

        MATELog.log_acc("Novelty of chromosomes in archive: ");
        List<Double> noveltyScores = noveltyFitnessFunction.getFitness(archive, nearestNeighbours);
        for (int i = 0; i < archive.size(); i++) {
            MATELog.log_acc("Chromosome " + archive.get(i) + ": " + noveltyScores.get(0));
        }

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
            MATELog.log_acc("Combined coverage until now: "
                    + CoverageUtils.getCombinedCoverage(Properties.COVERAGE()));
            if (population.size() <= 10) {
                MATELog.log_acc("Combined coverage of current population: "
                        + CoverageUtils.getCombinedCoverage(Properties.COVERAGE(), population));
            }
        }
    }

    /**
     * Updates the archive with the new chromosome if one of the following conditions hold:
     *
     * (1) If the archive is empty.
     * (2) The new chromosome has at least a novelty > {@link #noveltyThreshold}.
     *
     * @param chromosome The new chromosome that may get inserted into the archive.
     * @param archive The current archive.
     */
    private void updateArchive(IChromosome<T> chromosome, List<IChromosome<T>> archive) {

        if (archive.isEmpty()) {
            // the first chromosome always goes into the archive
            archive.add(chromosome);
        } else {

            /*
             * Here we compute the novelty again, but only compare the chromosome with the
             * chromosomes from the archive and don't include the current population.
             */
            double novelty = noveltyFitnessFunction
                    .getFitness(chromosome, Collections.emptyList(), archive, nearestNeighbours);

            if (novelty > noveltyThreshold) {

                if (archive.size() < archiveLimit) {
                    archive.add(chromosome);
                } else {
                    // replace 'worst' chromosome in the archive
                    List<Double> noveltyScores = noveltyFitnessFunction.getFitness(archive, nearestNeighbours);
                    double worstNovelty = Collections.min(noveltyScores);
                    IChromosome<T> worst = archive.get(noveltyScores.indexOf(worstNovelty));

                    if (novelty > worstNovelty) {
                        // only replace if better than worst chromosome in the archive
                        MATELog.log_acc("Replacing chromosome " + worst + " with chromosome "
                                + chromosome + " in archive!");
                        archive.remove(worst);
                        archive.add(chromosome);
                    }
                }
            }
        }
    }
}
