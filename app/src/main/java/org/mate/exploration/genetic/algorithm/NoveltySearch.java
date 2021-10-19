package org.mate.exploration.genetic.algorithm;

import android.util.Pair;

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
import java.util.HashMap;
import java.util.Iterator;
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
            population.add(chromosome);
            updateArchive(chromosome, population, archive);
        }

        logCurrentFitness();
        currentGenerationNumber++;
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
            // the first chromosome also goes into the archive
            archive.add(chromosome);
        }

        List<Double> noveltyVector = noveltyFitnessFunction.getFitness(population, archive, nearestNeighbours);
        MATE.log_acc("Novelty of chromosomes: " + noveltyVector);

        Pair<List<Double>, List<Double>> noveltyVectorPair
                = getNoveltyScoresOfPopulationAndArchive(noveltyVector, population.size());

        Map<IChromosome<T>, Double> populationNoveltyScores = convertToMap(population, noveltyVectorPair.first);
        Map<IChromosome<T>, Double> archiveNoveltyScores = convertToMap(archive, noveltyVectorPair.second);

        double novelty = populationNoveltyScores.get(chromosome);

        if (novelty > noveltyThreshold) {
            if (archive.size() < archiveLimit) {
                // archive not full yet
                archive.add(chromosome);
            } else {
                // replace with worst if better than worst
                Map.Entry<IChromosome<T>, Double> worstChromosome = getWorstChromosomeOfArchive(archiveNoveltyScores);
                if (novelty > worstChromosome.getValue()) {
                    archive.remove(worstChromosome.getKey());
                    archive.add(chromosome);
                }
            }
        }
    }

    /**
     * Splits the novelty vector into the scores belonging to the population and the archive.
     *
     * @param noveltyVector The novelty vector.
     * @param populationSize The population size.
     * @return Returns a pair where the first entry refers to the novelty scores of the population
     *          and the second entry refers to the novelty scores of the archive.
     */
    private Pair<List<Double>, List<Double>> getNoveltyScoresOfPopulationAndArchive(
            List<Double> noveltyVector, int populationSize) {
        List<Double> populationNoveltyScores = noveltyVector.subList(0, populationSize);
        List<Double> archiveNoveltyScores = noveltyVector.subList(populationSize, noveltyVector.size());
        return new Pair<>(populationNoveltyScores, archiveNoveltyScores);
    }

    /**
     * Converts a key and value list into a corresponding map. This assumes that the number of
     * keys and values are identical.
     *
     * @param keys The list of keys.
     * @param values The list of values.
     * @return Returns a map with the respective key-value associations.
     */
    private Map<IChromosome<T>, Double> convertToMap(List<IChromosome<T>> keys, List<Double> values) {

        if (keys.size() != values.size()) {
            throw new IllegalStateException("Not the same number of keys and values!");
        }

        Map<IChromosome<T>, Double> map = new HashMap<>();
        Iterator<IChromosome<T>> i1 = keys.iterator();
        Iterator<Double> i2 = values.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            map.put(i1.next(), i2.next());
        }
        return map;
    }

    /**
     * Returns the worst chromosome, i.e. the one with the lowest novelty/diversity score, from
     * the archive.
     *
     * @param archiveNoveltyScores A mapping of chromosomes in the archive to its novelty score.
     *
     * @return Returns the worst chromosome from the archive.
     */
    private Map.Entry<IChromosome<T>, Double> getWorstChromosomeOfArchive(
            Map<IChromosome<T>, Double> archiveNoveltyScores) {

        if (archiveNoveltyScores.isEmpty()) {
            throw new IllegalStateException("Can't retrieve worst chromosome from empty archive!");
        }

        Map.Entry<IChromosome<T>, Double> worstChromosome = null;

        for (Map.Entry<IChromosome<T>, Double> chromosome : archiveNoveltyScores.entrySet()) {
            if (worstChromosome == null) {
                worstChromosome = chromosome;
            } else if (chromosome.getValue() < worstChromosome.getValue()) {
                // a lower novelty score is worse
                worstChromosome = chromosome;
            }
        }

        return worstChromosome;
    }

    /**
     * Represents the evolution process. In the context of Novelty Search, we stick here to
     * the procedure of a standard genetic algorithm. The only difference is that we update our
     * archive accordingly and that we use a specialised selection as well as fitness function.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        while (newGeneration.size() < bigPopulationSize) {

            List<IChromosome<T>> parents = noveltySelectionFunction.select(population, archive,
                    nearestNeighbours, fitnessFunctions);

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

            for (IChromosome<T> chromosome : offspring) {
                if (newGeneration.size() == bigPopulationSize) {
                    break;
                } else {
                    newGeneration.add(chromosome);
                    updateArchive(chromosome, newGeneration, archive);
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

        List<Double> noveltyVector = noveltyFitnessFunction.getFitness(population, archive, nearestNeighbours);
        Pair<List<Double>, List<Double>> noveltyVectorPair
                = getNoveltyScoresOfPopulationAndArchive(noveltyVector, population.size());

        Map<IChromosome<T>, Double> populationNoveltyScores = convertToMap(population, noveltyVectorPair.first);
        Map<IChromosome<T>, Double> archiveNoveltyScores = convertToMap(archive, noveltyVectorPair.second);

        MATE.log_acc("Novelty of chromosomes in population: ");

        for (Map.Entry<IChromosome<T>, Double> chromosome : populationNoveltyScores.entrySet()) {
            MATE.log_acc("Chromosome " + chromosome.getKey() + ": " + chromosome.getValue());
        }

        MATE.log_acc("Novelty of chromosomes in archive: ");

        for (Map.Entry<IChromosome<T>, Double> chromosome : archiveNoveltyScores.entrySet()) {
            MATE.log_acc("Chromosome " + chromosome.getKey() + ": " + chromosome.getValue());
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

}
