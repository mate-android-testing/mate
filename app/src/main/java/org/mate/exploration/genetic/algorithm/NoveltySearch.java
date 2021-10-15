package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.NoveltyFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
    private List<ChromosomeNoveltyTuple> archive;

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
     * The novelty function.
     */
    private final NoveltyFitnessFunction<T> noveltyFunction;

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
        this.noveltyFunction = (NoveltyFitnessFunction<T>) fitnessFunctions.get(0);
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
            updateArchive(chromosome);
        }

        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Updates the archive with the given chromosome if:
     *
     * (1) The new chromosome has at least a novelty >= {@link #noveltyThreshold}.
     * (2) The archive is not full yet or the given chromosome is more diverse than any other
     *      chromosome in the archive. Replaces the chromosome in the latter case.
     *
     * @param chromosome The new chromosome.
     */
    private void updateArchive(IChromosome<T> chromosome) {

        double novelty = noveltyFunction.getFitness(chromosome, population,
                getChromosomesOfArchive(archive), nearestNeighbours);

        if (novelty >= noveltyThreshold) {
            if (archive.size() < archiveLimit) {
                archive.add(new ChromosomeNoveltyTuple(chromosome, novelty));
            } else {
                // replace with worst if better than worst
                ChromosomeNoveltyTuple worstChromosome = getWorstChromosomeOfArchive();
                if (novelty > worstChromosome.novelty) {
                    archive.remove(worstChromosome);
                    archive.add(new ChromosomeNoveltyTuple(chromosome, novelty));
                }
            }
        }
    }

    /**
     * Returns the worst chromosome, i.e. the one with the lowest novelty/diversity score, from
     * the archive.
     *
     * @return Returns the worst chromosome from the archive.
     */
    private ChromosomeNoveltyTuple getWorstChromosomeOfArchive() {

        if (archive.isEmpty()) {
            throw new IllegalStateException("Can't retrieve worst chromosome from empty archive!");
        }

        Collections.sort(archive, new Comparator<ChromosomeNoveltyTuple>() {
            @Override
            public int compare(ChromosomeNoveltyTuple o1, ChromosomeNoveltyTuple o2) {
                return Double.compare(o1.novelty, o2.novelty);
            }
        });

        // the worst chromosome comes first
        return archive.get(0);
    }

    /**
     * Represents the evolution process. In the context of Novelty Search, we stick here to
     * the procedure of a standard genetic algorithm. The only difference is that we update our
     * archive accordingly.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        while (newGeneration.size() < bigPopulationSize) {
            List<IChromosome<T>> parents = selectionFunction.select(population, fitnessFunctions);

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
                    updateArchive(chromosome);
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
     * Retrieves the chromosomes of the given archive.
     *
     * @param archive The given archive.
     * @return Returns the chromosomes of the given archive.
     */
    private List<IChromosome<T>> getChromosomesOfArchive(List<ChromosomeNoveltyTuple> archive) {
        List<IChromosome<T>> chromosomes = new LinkedList<>();
        for (ChromosomeNoveltyTuple chromosome : archive) {
            chromosomes.add(chromosome.chromosome);
        }
        return chromosomes;
    }

    /**
     * Pairs a chromosome with its novelty/diversity value.
     */
    private class ChromosomeNoveltyTuple {

        private final IChromosome<T> chromosome;
        private final double novelty;

        public ChromosomeNoveltyTuple(IChromosome<T> chromosome, double novelty) {
            this.chromosome = chromosome;
            this.novelty = novelty;
        }

        public IChromosome<T> getChromosome() {
            return chromosome;
        }

        public double getNovelty() {
            return novelty;
        }

        @Override
        public String toString() {

            int size = -1;

            if (chromosome.getValue() instanceof TestCase) {
                size = ((TestCase) chromosome.getValue()).getEventSequence().size();
            } else if (chromosome.getValue() instanceof TestSuite) {
                size = ((TestSuite) chromosome.getValue()).getTestCases().size();
            }

            return "ChromosomeNoveltyTuple{" +
                    "chromosome=" + chromosome +
                    ", novelty=" + novelty +
                    ", size=" + size +
                    '}';
        }
    }
}
