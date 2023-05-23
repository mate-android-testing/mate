package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.ISOSMNoveltyFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.mutation.ISOSMMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.selection.SOSMNoveltyRankSelection;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.util.novelty.ChromosomeNoveltyTrace;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.utils.Utils;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This algorithm combines novelty search with subjective logic or also called second order uncertainty,
 * see https://ieeexplore.ieee.org/document/10057480.
 */
public class NoveltySearchUsingSOSM extends GeneticAlgorithm<TestCase> {

    /**
     * The current population.
     */
    private List<ChromosomeNoveltyTrace> population;

    /**
     * The new (next) generation.
     */
    private List<ChromosomeNoveltyTrace> newGeneration;

    /**
     * Stores traces about the current population.
     */
    private final List<Trace> traces = new ArrayList<>();

    /**
     * The subjective opinion state machine.
     */
    private final SOSMModel sosmModel;

    /**
     * The SOSM-based fitness function.
     */
    private final ISOSMNoveltyFitnessFunction noveltyFitnessFunction;

    /**
     * The SOSM-based mutation function.
     */
    private final ISOSMMutationFunction noveltyMutationFunction;

    /**
     * The SOSM-based selection function.
     */
    private final SOSMNoveltyRankSelection noveltySelectionFunction;

    /**
     * Initializes the genetic algorithm with all the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param selectionFunction The used selection function.
     * @param crossOverFunctions The used crossover functions.
     * @param mutationFunctions The used mutation functions.
     * @param fitnessFunctions The used fitness/novelty function.
     * @param terminationCondition The used termination condition.
     * @param populationSize The population size.
     * @param bigPopulationSize The big population size.
     * @param pCrossover The probability for crossover.
     * @param pMutate The probability for mutation.
     */
    public NoveltySearchUsingSOSM(IChromosomeFactory<TestCase> chromosomeFactory,
                                  ISelectionFunction<TestCase> selectionFunction,
                                  List<ICrossOverFunction<TestCase>> crossOverFunctions,
                                  List<IMutationFunction<TestCase>> mutationFunctions,
                                  List<IFitnessFunction<TestCase>> fitnessFunctions,
                                  ITerminationCondition terminationCondition,
                                  int populationSize,
                                  int bigPopulationSize,
                                  double pCrossover,
                                  double pMutate) {

        super(chromosomeFactory, selectionFunction, crossOverFunctions, mutationFunctions,
                fitnessFunctions, terminationCondition, populationSize, bigPopulationSize,
                pCrossover, pMutate);

        this.population = new ArrayList<>(populationSize);
        this.newGeneration = new ArrayList<>(populationSize);

        // TODO: Provide this argument as regular parameter.
        sosmModel = (SOSMModel) Registry.getUiAbstractionLayer().getGuiModel();

        this.noveltyFitnessFunction = (ISOSMNoveltyFitnessFunction) fitnessFunctions.get(0);
        this.noveltySelectionFunction = (SOSMNoveltyRankSelection) selectionFunction;
        this.noveltyMutationFunction = (ISOSMMutationFunction) mutationFunction;

    }

    /**
     * Creates the initial population consisting of random chromosomes. Also updates the archive.
     */
    @Override
    public void createInitialPopulation() {

        MATE.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");

        for (int i = 0; i < populationSize; i++) {
            sosmModel.resetRecordedTransitions();
            final IChromosome<TestCase> chromosome = chromosomeFactory.createChromosome();
            final Trace trace = new Trace(sosmModel.getRecordedTransitions());

            final ChromosomeNoveltyTrace cnt = new ChromosomeNoveltyTrace(chromosome, 0.0, trace);
            traces.add(trace);
            population.add(cnt);
        }

        updateSOSM();
        printSOSM();
        logCurrentFitness();
        currentGenerationNumber++;
    }

    private void updateSOSM() {
        sosmModel.updateSOSM(traces);
    }

    private IChromosome<TestCase> mutate(IChromosome<TestCase> parent, Trace trace) {
        return noveltyMutationFunction.mutate(parent, trace);
    }

    private ChromosomeNoveltyTrace newOffspring() {
        final ChromosomeNoveltyTrace parent = noveltySelectionFunction.select(population).get(0);
        sosmModel.resetRecordedTransitions();
        final IChromosome<TestCase> offspring = mutate(parent.getChromosome(), parent.getTrace());
        final Trace trace = new Trace(sosmModel.getRecordedTransitions());

        traces.add(trace);
        final double novelty = noveltyFitnessFunction.getNovelty(offspring, trace);
        return new ChromosomeNoveltyTrace(offspring, novelty, trace);
    }

    private void printSOSM() {
        /*
         * The logcat has a limited number of log, that is able to handle at once.
         * If the amount of message is too large, message will get silently dropped.
         * We wait a bit before flooding the logcat with DOT logs, such that the logcat can handle
         * all the logs.
         */
        Utils.sleep(200);
        MATE.log_debug("SOSM print graph start");
        for (final String line : sosmModel.toDOT())
            MATE.log_debug(line);
        MATE.log_debug("SOSM print graph stop");
        Utils.sleep(200);
    }

    private void swapPopulationAndNewGeneration() {
        final List<ChromosomeNoveltyTrace>  tmp = population;
        population = newGeneration;
        newGeneration = tmp;
    }

    /**
     * // TODO: Adjust comment.
     * Represents the evolution process. In the context of novelty search, we stick here to
     * the procedure of a standard genetic algorithm. The only difference is that we update our
     * archive accordingly and that we use a specialised selection as well as fitness function.
     */
    @Override
    public void evolve() {

        // TODO: Implement like regular genetic algorithm including crossover, etc.

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));

        traces.clear();

        for (int i = 0; i < populationSize; ++i) {
            final ChromosomeNoveltyTrace cnt = newOffspring();
            newGeneration.add(cnt);
        }

        population.clear();
        swapPopulationAndNewGeneration();

        updateSOSM();
        printSOSM();
        logCurrentFitness();
        ++currentGenerationNumber;
    }

    /**
     * Determines the survivors of the current generation. By default, the newly created offsprings
     * constitute the survivors.
     *
     * @return Returns a population of size {@link #populationSize} that is used in the next generation.
     */
    @Override
    public List<IChromosome<TestCase>> getGenerationSurvivors() {
        throw new UnsupportedOperationException("Unsupported");
    }

    /**
     * Logs the novelty of the chromosomes in the current population.
     */
    @Override
    protected void logCurrentFitness() {

        MATE.log_acc("Novelty of generation #" + (currentGenerationNumber + 1) + " :");
        MATE.log_acc("Novelty of chromosomes in population: ");

        for (final ChromosomeNoveltyTrace cnt : population) {
            MATE.log_acc("Chromosome " + cnt.getChromosome() + ": " + cnt.getNovelty());
        }

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
            MATE.log_acc("Combined coverage until now: "
                    + CoverageUtils.getCombinedCoverage(Properties.COVERAGE()));
        }
    }
}

