package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.MIOEDAChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.ActionFitnessFunctionWrapper;
import org.mate.exploration.genetic.fitness.IActionFitnessFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;
import org.mate.exploration.genetic.util.eda.pipe.PIPE;
import org.mate.model.TestCase;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MIOEDA<T> extends GeneticAlgorithm<T> {

    /**
     * The archive maintains for each target k a population T_k of size up to n.
     */
    private final Map<ActionFitnessFunctionWrapper, ArchiveContainer> archive;

    /**
     * Represents the current probability P_r for sampling a random chromosome.
     */
    private final double pSampleRandom;

    /**
     * Wraps the underlying fitness functions such that we can store and retrieve the fitness after
     * individual actions.
     */
    private final List<ActionFitnessFunctionWrapper> fitnessFunctions = new ArrayList<>();

    // represents the variable c_k for each target k
    private final Map<ActionFitnessFunctionWrapper, Integer> samplingCounters;

    // tracks the start point of the search to measure when the focused search should start
    private long startTime;

    /**
     * The chromosome factory to sample new chromosomes.
     */
    private final IChromosomeFactory<T> randomChromosomeFactory;

    /**
     * Initializes MIOEDA with the relevant attributes.
     *
     * @param chromosomeFactory    The used chromosome factory, see {@link IChromosomeFactory}.
     * @param fitnessFunctions     The used fitness functions, see {@link IFitnessFunction}.
     * @param terminationCondition The used termination condition, see {@link ITerminationCondition}.
     * @param populationSize       The population size n.
     * @param pSampleRandom        The sampling probability P_r.
     */
    public MIOEDA(IChromosomeFactory<T> chromosomeFactory,
                  List<IFitnessFunction<T>> fitnessFunctions,
                  ITerminationCondition terminationCondition,
                  int populationSize,
                  double pSampleRandom) {

        super(chromosomeFactory,
                null,
                null,
                null,
                fitnessFunctions,
                terminationCondition,
                populationSize,
                populationSize,
                0,
                0);

        // Enhance the fitness functions to record and retrieve the fitness after individual actions.
        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
            this.fitnessFunctions.add(new ActionFitnessFunctionWrapper(
                    (IActionFitnessFunction<TestCase>) fitnessFunction));
        }

        // Initialize the random probabilistic model.
        IProbabilisticModel<T> randomProbabilisticModel
                = (IProbabilisticModel<T>) new PIPE(this.fitnessFunctions.get(0),
                Properties.PIPE_LEARNING_RATE(),
                Properties.PIPE_NEGATIVE_LEARNING_RATE(),
                Properties.PIPE_EPSILON(),
                Properties.PIPE_CLR(),
                Properties.PIPE_PROB_ELITIST_LEARNING(),
                Properties.PIPE_PROB_MUTATION(),
                Properties.PIPE_MUTATION_RATE());

        randomChromosomeFactory
                = (IChromosomeFactory<T>) new MIOEDAChromosomeFactory(
                        Properties.MAX_NUMBER_EVENTS(), randomProbabilisticModel);


        this.archive = new HashMap<>(); // (k -> T_k)
        this.samplingCounters = new HashMap<>(); // (k -> c_k)
        this.pSampleRandom = pSampleRandom; // P_r
        this.populationSize = populationSize; // n

        // Provide for each target a dedicated probabilistic model.
        for (ActionFitnessFunctionWrapper fitnessFunction : this.fitnessFunctions) {
            IProbabilisticModel<T> probabilisticModel = (IProbabilisticModel<T>) new PIPE(fitnessFunction,
                    Properties.PIPE_LEARNING_RATE(),
                    Properties.PIPE_NEGATIVE_LEARNING_RATE(),
                    Properties.PIPE_EPSILON(),
                    Properties.PIPE_CLR(),
                    Properties.PIPE_PROB_ELITIST_LEARNING(),
                    Properties.PIPE_PROB_MUTATION(),
                    Properties.PIPE_MUTATION_RATE());

            ArchiveContainer archiveContainer = new ArchiveContainer(probabilisticModel, fitnessFunction);
            archive.put(fitnessFunction, archiveContainer);

            // initially the sampling counter c_k for each testing target k is zero
            samplingCounters.put(fitnessFunction, 0);
        }

        //we have to add all archive containers to our chromosome factories since they need to update all the models.
        MIOEDAChromosomeFactory cf = (MIOEDAChromosomeFactory) chromosomeFactory;
        MIOEDAChromosomeFactory cf2 = (MIOEDAChromosomeFactory) randomChromosomeFactory;

        List<MIOEDA.ArchiveContainer> archiveContainers = new ArrayList<>(archive.values());
        archiveContainers.add(new ArchiveContainer(randomProbabilisticModel, this.fitnessFunctions.get(0)));

        cf.setArchiveContainers(archiveContainers);
        cf2.setArchiveContainers(archiveContainers);

        MATE.log_acc("We have " + fitnessFunctions.size() + " fitness functions");
    }

    /**
     * Creates the initial population consisting of a single random chromosome and then updates the
     * probabilistic models accordingly.
     */
    @Override
    public void createInitialPopulation() {
        this.startTime = System.currentTimeMillis();

        MATE.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");

        IChromosome<T> chromosome = randomChromosomeFactory.createChromosome();
        population.add(chromosome);
        evaluatePopulation(population);

        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Forms a new generation consisting of a single chromosome that is either sampled randomly with
     * a probability P_r or otherwise samples the chromosome from the archive, more precisely from
     * one of the probabilistic models.
     */
    @Override
    public void evolve() {
        MATE.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");
        population.clear();

        if (Randomness.getRnd().nextDouble() < pSampleRandom) {
            // sample random chromosome with probability P_r
            IChromosome<T> chromosome = randomChromosomeFactory.createChromosome();
            population.add(chromosome);
            MATE.log_acc("Sampled random chromosome " + chromosome + "!");
        } else {
            /*
             * Sample a chromosome from the archive with probability (1 - P_r). Pick a target k with
             * the lowest sampling counter c_k. Then select randomly a chromosome from the
             * population T_k in the archive.
             */
            ActionFitnessFunctionWrapper target = getBestTarget().getFitnessFunction();
            MATE.log_acc("Sampled target " + target + " from archive!");

            // increase sampling counter c_k, see section 3.3
            samplingCounters.put(target, samplingCounters.get(target) + 1);

            MATE.log_acc("Sampling new chromosome");
            MIOEDAChromosomeFactory cf = (MIOEDAChromosomeFactory) chromosomeFactory;
            cf.setProbabilisticModel((IProbabilisticModel<TestCase>) archive.get(target).getProbabilisticModel());
            // TODO: 04.12.2023 Maybe change to using more Chromosome factories with a probabilistic model each
            IChromosome<T> chromosome = (IChromosome<T>) cf.createChromosome();
            MATE.log_acc("sampled " + chromosome);
            population.add(chromosome);
        }

        MATE.log_acc("Updating Archive...");

        // evaluate fitness and update archive
        evaluatePopulation(population);
        logCurrentFitness();
    }

    private void evaluatePopulation(final List<IChromosome<T>> population) {
        for (IChromosome<T> chromosome : population) {
            for (ActionFitnessFunctionWrapper target : this.fitnessFunctions) {
                ArchiveContainer archiveContainer = archive.get(target);
                if (archiveContainer == null || archiveContainer.isCovered()) continue;

                double fitness = target.getNormalizedFitness((IChromosome<TestCase>) chromosome);
                // TODO: Consider maximising and minimising fitness function values here.
                if (fitness != 0.0) archiveContainer.getProbabilisticModel().update(population);
                archiveContainer.updateFitness(fitness);
            }
        }
    }

    private ArchiveContainer getBestTarget() {
        List<ArchiveContainer> possibleTargets = new ArrayList<>();

        for (ActionFitnessFunctionWrapper target : fitnessFunctions) {
            ArchiveContainer archiveContainer = archive.get(target);
            if (archiveContainer != null && !archiveContainer.isCovered() && archiveContainer.getBestFitness() < 1.0)
                possibleTargets.add(archiveContainer);
        }

        MATE.log_acc("We have " + possibleTargets.size() + " possible targets with a fitness value lower than 1.0");

        if (possibleTargets.isEmpty()) {
            MATE.log_warn("No uncovered targets found where fitness is better than 1.0");
            for (ActionFitnessFunctionWrapper target : fitnessFunctions) {
                ArchiveContainer archiveContainer = archive.get(target);
                possibleTargets.add(archiveContainer);
            }
        }

        //sorted by fitness
//        Collections.sort(possibleTargets, (o1, o2) -> {
//            double diff = o1.getBestFitness() - o2.getBestFitness();
//            if (diff < 0) return -1;
//            else if (diff > 0) return 1;
//            else return 0;
//        });

        return possibleTargets.get(Randomness.getRandom(0, possibleTargets.size()));

//        Collections.sort(possibleTargets, (Comparator.comparingInt((ArchiveContainer o) -> samplingCounters.get(o.getFitnessFunction()))));
//        MATE.log_acc("First sample count: " + samplingCounters.get(possibleTargets.get(0).getFitnessFunction()) + " last sample count: " + samplingCounters.get(possibleTargets.get(possibleTargets
//        .size() - 1).getFitnessFunction()));
//        return possibleTargets.get(0);
    }

    public class ArchiveContainer {
        private final IProbabilisticModel<T> probabilisticModel;
        private boolean covered = false;
        private double bestFitness = 1;
        private final ActionFitnessFunctionWrapper fitnessFunction;


        public ArchiveContainer(IProbabilisticModel<T> probabilisticModel, ActionFitnessFunctionWrapper fitnessFunction) {
            this.probabilisticModel = probabilisticModel;
            this.fitnessFunction = fitnessFunction;
        }

        public double getBestFitness() {
            return bestFitness;
        }

        public void updateFitness(double fitness) {
            if(fitness < this.bestFitness){
                MATE.log_acc("Fitness decreased from " + this.bestFitness + " to " + fitness);
                if (fitness <= 0d) covered = true;
                this.bestFitness = fitness;
            }
        }

        public IProbabilisticModel<T> getProbabilisticModel() {
            return probabilisticModel;
        }

        public boolean isCovered() {
            return covered;
        }

        public ActionFitnessFunctionWrapper getFitnessFunction() {
            return fitnessFunction;
        }
    }
}
