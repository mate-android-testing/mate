package org.mate.utils;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.List;

public final class ChromosomeUtils {

    private ChromosomeUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * Concatenates the given chromosomes separated by '+' into a single {@link String}.
     *
     * @param chromosomes A list of chromosomes.
     * @param <T> Refers either to a {@link TestCase} or a {@link TestSuite}.
     * @return Returns a single {@link String} containing the chromosome ids.
     */
    public static <T> String getChromosomeIds(List<IChromosome<T>> chromosomes) {

        // Java 8: String.join("+", chromosomeIds);
        StringBuilder chromosomeIds = new StringBuilder();

        for (IChromosome<T> chromosome : chromosomes) {
            chromosomeIds.append(getChromosomeId(chromosome));
            chromosomeIds.append("+");
        }

        // remove '+' at the end
        if (chromosomeIds.length() > 0) {
            chromosomeIds.setLength(chromosomeIds.length() - 1);
        }

        return chromosomeIds.toString();
    }

    /**
     * Returns the chromosome id of the given chromosome.
     *
     * @param chromosome The chromosome.
     * @param <T> Refers either to a {@link TestCase} or a {@link TestSuite}.
     * @return Returns the chromosome id of the given chromosome.
     */
    public static <T> String getChromosomeId(IChromosome<T> chromosome) {

        String chromosomeId = null;

        if (chromosome.getValue() instanceof TestCase) {
            chromosomeId = ((TestCase) chromosome.getValue()).getId();
        } else if (chromosome.getValue() instanceof TestSuite) {
            chromosomeId = ((TestSuite) chromosome.getValue()).getId();
        } else {
            throw new IllegalStateException("Couldn't derive chromosome id for chromosome "
                    + chromosome + "!");
        }
        return chromosomeId;
    }

    /**
     * Retrieves the action entity id for the most recently executed action of the given test case.
     *
     * @param chromosome The given test case chromosome.
     * @return Returns the action entity id for most recently executed action.
     */
    public static String getActionEntityId(final IChromosome<TestCase> chromosome) {
        return getActionEntityId(chromosome, chromosome.getValue().getActionSequence().size());
    }

    /**
     * Retrieves the action entity id for the given test case.
     *
     * @param chromosome The given test case chromosome.
     * @param actionID The action id.
     * @return Returns the action entity id for the given test case.
     */
    public static String getActionEntityId(final IChromosome<TestCase> chromosome, final int actionID) {
        return actionID + "_" + chromosome.getValue().getId();
    }
}
