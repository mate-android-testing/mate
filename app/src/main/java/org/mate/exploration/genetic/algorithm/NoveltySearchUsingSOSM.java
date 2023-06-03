package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.crossover.ISOSMCrossOverFunction;
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
import org.mate.utils.Randomness;
import org.mate.utils.Tuple;
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
    private final List<ChromosomeNoveltyTrace> population;

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
     * The SOSM-based crossover function.
     */
    private final ISOSMCrossOverFunction noveltyCrossOverFunction;

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

        sosmModel = (SOSMModel) Registry.getUiAbstractionLayer().getGuiModel();

        this.noveltyFitnessFunction = (ISOSMNoveltyFitnessFunction) fitnessFunctions.get(0);
        this.noveltySelectionFunction = (SOSMNoveltyRankSelection) selectionFunction;
        this.noveltyMutationFunction = (ISOSMMutationFunction) mutationFunction;
        this.noveltyCrossOverFunction = (ISOSMCrossOverFunction) crossOverFunction;
    }

    /**
     * Creates the initial population consisting of random chromosomes. Updates the SOSM afterwards.
     */
    @Override
    public void createInitialPopulation() {

        MATE.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");

        for (int i = 0; i < populationSize; i++) {

            // Record the transitions, i.e. a trace, that are taken by the test case.
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

    /**
     * Updates the SOSM with new traces.
     */
    private void updateSOSM() {
        sosmModel.updateSOSM(traces);
    }

    /**
     * Prints the SOSM to logcat.
     */
    private void printSOSM() {
        /*
         * The logcat buffer can store only a limited number of logs. If the number of log messages
         * is too large in a particular time frame, some messages get dropped silently.
         * We wait a bit before flooding the logcat with DOT logs, such that the logcat can handle
         * all the logs.
         */
        Utils.sleep(200);
        MATE.log_debug("SOSM print graph start");
        for (final String line : sosmModel.convertToDOT())
            MATE.log_debug(line);
        MATE.log_debug("SOSM print graph stop");
        Utils.sleep(200);
    }

    /**
     * Generates a new population following the procedure of a standard genetic algorithm. Updates
     * the SOSM after each generation.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));

        traces.clear();

        List<ChromosomeNoveltyTrace> newGeneration = new ArrayList<>(population);

        while (newGeneration.size() < bigPopulationSize) {

            List<ChromosomeNoveltyTrace> parents = noveltySelectionFunction.select(population);

            List<ChromosomeNoveltyTrace> offsprings = new ArrayList<>();

            if (Randomness.getRnd().nextDouble() < pCrossover) {

                List<Tuple<IChromosome<TestCase>, Trace>> offspringTuples
                        = noveltyCrossOverFunction.crossover(parents);

                for (Tuple<IChromosome<TestCase>, Trace> offspringTuple : offspringTuples) {
                    final IChromosome<TestCase> crossedChromosome = offspringTuple.getX();
                    final Trace trace = offspringTuple.getY();
                    traces.add(trace);
                    final double novelty
                            = noveltyFitnessFunction.getNovelty(crossedChromosome, trace);
                    offsprings.add(new ChromosomeNoveltyTrace(crossedChromosome, novelty, trace));
                }
            } else {
                offsprings = parents;
            }

            for (ChromosomeNoveltyTrace offspring : offsprings) {

                if (Randomness.getRnd().nextDouble() < pMutate) {

                    final Tuple<IChromosome<TestCase>, Trace> mutatedChromosomeAndTrace
                            = noveltyMutationFunction.mutate(offspring.getChromosome(), offspring.getTrace());

                    final IChromosome<TestCase> mutatedChromosome = mutatedChromosomeAndTrace.getX();
                    final Trace trace = mutatedChromosomeAndTrace.getY();

                    traces.add(trace);
                    final double novelty = noveltyFitnessFunction.getNovelty(mutatedChromosome, trace);
                    offspring = new ChromosomeNoveltyTrace(mutatedChromosome, novelty, trace);
                }

                if (newGeneration.size() < bigPopulationSize) {
                    newGeneration.add(offspring);
                } else {
                    // big population size reached -> early abort
                    break;
                }
            }
        }

        population.clear();
        population.addAll(newGeneration);
        List<ChromosomeNoveltyTrace> survivors = getSurvivors();
        population.clear();
        population.addAll(survivors);

        updateSOSM();
        printSOSM();
        logCurrentFitness();
        ++currentGenerationNumber;
    }

    /**
     * Returns the generation survivors, i.e. the lastly added chromosomes.
     *
     * @return Returns the generation survivors.
     */
    public List<ChromosomeNoveltyTrace> getSurvivors() {
        return new ArrayList<>(population.subList(population.size() - populationSize, population.size()));
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

