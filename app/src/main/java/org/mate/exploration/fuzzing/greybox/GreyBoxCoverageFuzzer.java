package org.mate.exploration.fuzzing.greybox;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.List;

/**
 * A generic coverage based greybox fuzzing algorithm.
 *
 * @param <T> Either a {@link TestCase} or a {@link TestSuite}.
 */
public class GreyBoxCoverageFuzzer<T> extends GreyBoxFuzzing<T> {

    /**
     * We need to maintain the total coverage to check whether a mutated chromosome increased it.
     * That way, we can decide whether a chromosome {@link #isInteresting(IChromosome)}.
     */
    private double totalCoverage = 0.0;

    /**
     * Whether the last considered chromosome s' fulfills {@link #isInteresting(IChromosome)}.
     */
    private boolean isInteresting = false;

    /**
     * The selected coverage criterion, i.e. the coverage type that should steer the exploration.
     */
    private final Coverage coverageCriterion;

    /**
     * Initialises the greybox fuzzer.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param mutationFunction The used mutation function.
     * @param terminationCondition The used termination condition.
     * @param coverageCriterion The used coverage criterion.
     * @param corpusSize The initial size of the seed corpus S.
     * @param maxEnergy The maximal assignable energy p.
     */
    public GreyBoxCoverageFuzzer(IChromosomeFactory<T> chromosomeFactory,
                                 IMutationFunction<T> mutationFunction,
                                 ITerminationCondition terminationCondition,
                                 Coverage coverageCriterion,
                                 int corpusSize,
                                 int maxEnergy) {
        super(chromosomeFactory, mutationFunction, terminationCondition, corpusSize, maxEnergy);
        this.coverageCriterion = coverageCriterion;
    }

    /**
     * Chooses the next chromosome from the seed corpus S. We pick the chromosome with the
     * highest coverage.
     *
     * @param seedCorpus The seed corpus S.
     * @return Returns the next chromosome from the seed corpus S.
     */
    @Override
    public IChromosome<T> chooseNext(List<IChromosome<T>> seedCorpus) {

        IChromosome<T> best = seedCorpus.get(0);

        for (IChromosome<T> s : seedCorpus) {
            double coverage = CoverageUtils.getCoverage(coverageCriterion, s).getCoverage(coverageCriterion);
            if (coverage > CoverageUtils.getCoverage(coverageCriterion, best).getCoverage(coverageCriterion)) {
                best = s;
            }
        }

        return best;
    }

    /**
     * Assigns an energy to the given chromosome s. We assign more energy to those chromosomes that
     * are shorter and thus faster to execute.
     *
     * @param s The chromosome s for which the energy should be assigned.
     * @return Returns the energy assigned to the chromosome s.
     */
    @Override
    public int assignEnergy(IChromosome<T> s) {

        /*
        * We need to make a snapshot of the total coverage here in order to tell whether a
        * mutated chromosome s' is going to increase the total coverage or not.
         */
        totalCoverage = CoverageUtils.getCombinedCoverage(coverageCriterion).getCoverage(coverageCriterion);

        if (s.getValue() instanceof TestCase) {
            int size = ((TestCase) s.getValue()).getActionSequence().size();
            return Math.max(1, Math.round(maxEnergy
                    - ((float) maxEnergy / Properties.MAX_NUMBER_EVENTS()) * size));
        } else if (s.getValue() instanceof TestSuite) {
            /*
            * Test suites have per construction a fixed number of test cases, thus we need to stick
            * here to the size of the individual test cases, which may vary.
             */
            int size = 0;
            List<TestCase> testCases = ((TestSuite) s.getValue()).getTestCases();
            for (TestCase testCase : testCases) {
                size += testCase.getActionSequence().size();
            }
            return Math.max(1, Math.round(maxEnergy
                    - ((float) maxEnergy / Properties.NUMBER_TESTCASES()) * size));
        } else {
            throw new IllegalStateException("Chromosome type " + s.getValue().getClass()
                    + "not yet supported!");
        }
    }

    /**
     * Checks whether the given chromosome s is considered interesting. We consider a chromosome
     * interesting if it increases the total coverage.
     *
     * @param s The chromosome s.
     * @return Returns {@code true} if the chromosome is considered interesting, otherwise
     *          {@code false} is returned.
     */
    @Override
    public boolean isInteresting(IChromosome<T> s) {
        return isInteresting;
    }

    /**
     * Checks whether the given chromosome produced a crash or not.
     *
     * @param s The chromosome s.
     * @return Returns {@code true} if the chromosome produced a crash, otherwise {@code false}
     *          is returned.
     */
    @Override
    public boolean isCrashing(IChromosome<T> s) {

        /*
        * Since a mutated chromosome s' is considered here first, it might not undergo the
        * subsequent isInteresting() check. Thus, we need to make that decision here and update
        * the total coverage accordingly.
         */
        double combinedCoverage = CoverageUtils.getCombinedCoverage(coverageCriterion).getCoverage(coverageCriterion);
        if (combinedCoverage > totalCoverage) {
            isInteresting = true;
            totalCoverage = combinedCoverage;
        } else {
            isInteresting = false;
        }

        if (s.getValue() instanceof TestCase) {
            return ((TestCase) s.getValue()).hasCrashDetected();
        } else if (s.getValue() instanceof TestSuite) {
            return ((TestSuite) s.getValue()).getCrashDetected();
        } else {
            throw new IllegalStateException("Chromosome type " + s.getValue().getClass()
                    + "not yet supported!");
        }
    }
}
