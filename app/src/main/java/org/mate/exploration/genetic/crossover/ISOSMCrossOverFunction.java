package org.mate.exploration.genetic.crossover;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.util.novelty.ChromosomeNoveltyTrace;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.Trace;
import org.mate.utils.Tuple;

import java.util.List;

/**
 * The interface for SOSM-based crossover functions.
 */
public interface ISOSMCrossOverFunction extends ICrossOverFunction<TestCase> {

    @Override
    default List<IChromosome<TestCase>> cross(List<IChromosome<TestCase>> parents) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * Performs a crossover on the given (two) parent chromosomes.
     *
     * @param parents The given (two) parent chromosomes.
     * @return Returns the generated offsprings along with their traces.
     */
    List<Tuple<IChromosome<TestCase>, Trace>> crossover(List<ChromosomeNoveltyTrace> parents);
}
