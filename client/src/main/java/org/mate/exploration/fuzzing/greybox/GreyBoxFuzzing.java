package org.mate.exploration.fuzzing.greybox;

import org.mate.commons.utils.MATELog;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an abstract implementation of a greybox fuzzing algorithm according to the paper
 * "Directed Greybox Fuzzing", see https://acmccs.github.io/papers/p2329-bohmeAemb.pdf.
 *
 * @param <T> Either a {@link TestCase} or a {@link TestSuite}.
 */
public abstract class GreyBoxFuzzing<T> implements Algorithm {

    /**
     * The used chromosome factory.
     */
    protected final IChromosomeFactory<T> chromosomeFactory;

    /**
     * The used mutation function.
     */
    protected final IMutationFunction<T> mutationFunction;

    /**
     * The used termination condition.
     */
    protected final ITerminationCondition terminationCondition;

    /**
     * The initial size of the seed corpus S.
     */
    protected final int corpusSize;

    /**
     * The maximal assignable energy p.
     */
    protected final int maxEnergy;

    /**
     * The list of chromosomes that produce a crash.
     */
    protected final List<IChromosome<T>> crashingInputs;

    /**
     * Initialises the greybox fuzzing algorithm with the relevant attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param mutationFunction The used mutation function.
     * @param terminationCondition The used termination condition.
     * @param corpusSize The initial size of the seed corpus S.
     * @param maxEnergy The maximal assignable energy p.
     */
    public GreyBoxFuzzing(IChromosomeFactory<T> chromosomeFactory,
                          IMutationFunction<T> mutationFunction,
                          ITerminationCondition terminationCondition,
                          int corpusSize,
                          int maxEnergy) {
        this.chromosomeFactory = chromosomeFactory;
        this.mutationFunction = mutationFunction;
        this.terminationCondition = terminationCondition;
        this.crashingInputs = new ArrayList<>();
        this.corpusSize = corpusSize;
        this.maxEnergy = maxEnergy;
    }

    /**
     * Returns the crashing inputs.
     *
     * @return Returns the list of crashing inputs.
     */
    public List<IChromosome<T>> getCrashingInputs() {
        return crashingInputs;
    }

    /**
     * Generates the initial seed corpus S.
     *
     * @return Returns the generated seed corpus S.
     */
    protected List<IChromosome<T>> generateSeedCorpus() {
        List<IChromosome<T>> seedCorpus = new ArrayList<>();
        for (int i = 0; i < corpusSize; i++) {
            seedCorpus.add(chromosomeFactory.createChromosome());
        }
        return seedCorpus;
    }

    /**
     * Determines which chromosome s should be picked next for energy assigning and mutation.
     *
     * @param seedCorpus The seed corpus S.
     * @return Returns the next chromosome from the seed corpus S.
     */
    public abstract IChromosome<T> chooseNext(List<IChromosome<T>> seedCorpus);

    /**
     * Assigns an energy to a chromosome s, which controls how often that chromosome should be
     * mutated.
     *
     * @param s The chromosome s.
     * @return Returns the assigned energy to the chromosome s.
     */
    public abstract int assignEnergy(IChromosome<T> s);

    /**
     * Determines whether a chromosome s is considered interesting or not.
     *
     * @param s The chromosome s.
     * @return Returns {@code true} if the chromosome is considered interesting, otherwise
     *          {@code false} is returned.
     */
    public abstract boolean isInteresting(IChromosome<T> s);

    /**
     * Checks whether the given chromosome s produced a crash or not.
     *
     * @param s The chromosome s.
     * @return Returns {@code true} if the chromosome produced a crash, otherwise
     *          {@code false} is returned.
     */
    public abstract boolean isCrashing(IChromosome<T> s);

    /**
     * The abstract procedure of a grey box fuzzing algorithm as described by the pseudo code
     * of the paper "Directed Greybox Fuzzing", see section 3.1.
     */
    @Override
    public void run() {

        MATELog.log_acc("Seed corpus size: " + corpusSize);
        MATELog.log_acc("Max assignable energy: " + maxEnergy);

        MATELog.log_acc("Generating seed corpus S...");
        List<IChromosome<T>> seedCorpus = generateSeedCorpus();

        MATELog.log_acc("Starting greybox fuzzing...");
        while (!terminationCondition.isMet()) {

            MATELog.log_acc("Choosing next chromosome from seed corpus S...");
            IChromosome<T> s = chooseNext(seedCorpus);
            int p = assignEnergy(s);

            for (int i = 0; i < p; i++) {
                IChromosome<T> sPrime = mutationFunction.mutate(s);
                if (isCrashing(sPrime)) {
                    MATELog.log_acc("Found crashing chromosome: " + sPrime);
                    crashingInputs.add(sPrime);
                } else if (isInteresting(sPrime)) {
                    MATELog.log_acc("Found interesting chromosome: " + sPrime);
                    seedCorpus.add(sPrime);
                }
            }

            MATELog.log_acc("Total number of crashes so far: " + crashingInputs.size());
        }
    }
}
