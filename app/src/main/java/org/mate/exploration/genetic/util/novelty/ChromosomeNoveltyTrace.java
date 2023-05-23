package org.mate.exploration.genetic.util.novelty;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.Trace;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Associates with a test case chromosome its novelty and its traces.
 */
public final class ChromosomeNoveltyTrace {

    /**
     * The test case chromosome.
     */
    private final IChromosome<TestCase> chromosome;

    /**
     * The novelty of the test case chromosome.
     */
    private final double novelty;

    /**
     * The traversed transitions of the chromosome.
     */
    private final Trace trace;

    /**
     * Initialises a new chromosome novelty trace linkage.
     *
     * @param chromosome The test case chromosome.
     * @param novelty The novelty of the chromosome.
     * @param trace The traversed transitions of the chromosome.
     */
    public ChromosomeNoveltyTrace(final IChromosome<TestCase> chromosome,
                                  final double novelty, final Trace trace) {
        this.chromosome = requireNonNull(chromosome);
        this.novelty = requireNonNull(novelty);
        this.trace = requireNonNull(trace);
    }

    /**
     * Returns the test case chromosome.
     *
     * @return Returns the test case chromosome.
     */
    public IChromosome<TestCase> getChromosome() {
        return chromosome;
    }

    /**
     * Returns the novelty associated with the chromosome.
     *
     * @return Returns the novelty associated with the chromosome.
     */
    public double getNovelty() {
        return novelty;
    }

    /**
     * Returns the trace associated with the chromosome.
     *
     * @return Returns the trace associated with the chromosome.
     */
    public Trace getTrace() {
        return trace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final ChromosomeNoveltyTrace that = (ChromosomeNoveltyTrace) o;

            return Double.compare(novelty, that.novelty) == 0
                    && trace.equals(that.trace)
                    && chromosome.equals(that.chromosome);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(chromosome, novelty, trace);
    }

    @Override
    public String toString() {
        return String.format("ChromosomeNoveltyTrace{chromosome=%s, novelty=%s, trace=%s}",
                chromosome, novelty, trace);
    }
}

