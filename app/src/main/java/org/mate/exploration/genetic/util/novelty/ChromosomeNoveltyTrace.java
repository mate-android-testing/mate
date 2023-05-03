package org.mate.exploration.genetic.util.novelty;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.Trace;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class ChromosomeNoveltyTrace {

    private final IChromosome<TestCase> chromosome;
    private final double novelty;
    private final Trace trace;

    public ChromosomeNoveltyTrace(final IChromosome<TestCase> chromosome,
                                  final double novelty, final Trace trace) {
        this.chromosome = requireNonNull(chromosome);
        this.novelty = requireNonNull(novelty);
        this.trace = requireNonNull(trace);
    }

    public IChromosome<TestCase> chromosome() {
        return chromosome;
    }

    public double novelty() {
        return novelty;
    }

    public Trace trace() {
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
        return String.format("C{chromosome=%s, novelty=%s, trace=%s}", chromosome, novelty, trace);
    }
}

