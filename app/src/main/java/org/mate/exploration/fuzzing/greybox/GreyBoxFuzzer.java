package org.mate.exploration.fuzzing.greybox;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.coverage.CoverageUtils;

import java.util.List;

public class GreyBoxFuzzer<T> extends GreyBoxFuzzing<T> {

    /**
     * Controls how many mutations should be performed upon the same chromosome s.
     */
    private static final int MAX_ENERGY = 10;

    public GreyBoxFuzzer(IChromosomeFactory<T> chromosomeFactory,
                         IMutationFunction<T> mutationFunction,
                         ITerminationCondition terminationCondition,
                         int corpusSize) {
        super(chromosomeFactory, mutationFunction, terminationCondition, corpusSize);
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
            double coverage = CoverageUtils.getCoverage(Properties.COVERAGE(), s);
            if (coverage > CoverageUtils.getCoverage(Properties.COVERAGE(), best)) {
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

        if (s.getValue() instanceof TestCase) {
            int size = ((TestCase) s.getValue()).getEventSequence().size();
            return Math.max(1, Math.round(MAX_ENERGY - ((float) MAX_ENERGY / Properties.MAX_NUMBER_EVENTS()) * size));
        } else if (s.getValue() instanceof TestSuite) {
            // TODO: Test suites seems to have always a fixed size, pick another criteria for them!
            int size = ((TestSuite) s.getValue()).getTestCases().size();
            return Math.max(1, Math.round(MAX_ENERGY - ((float) MAX_ENERGY / Properties.NUMBER_TESTCASES()) * size));
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
        return false;
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

        if (s.getValue() instanceof TestCase) {
            return ((TestCase) s.getValue()).getCrashDetected();
        } else if (s.getValue() instanceof TestSuite) {
            return ((TestSuite) s.getValue()).getCrashDetected();
        } else {
            throw new IllegalStateException("Chromosome type " + s.getValue().getClass()
                    + "not yet supported!");
        }
    }
}
