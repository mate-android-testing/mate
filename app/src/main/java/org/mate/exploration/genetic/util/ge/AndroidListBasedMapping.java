package org.mate.exploration.genetic.util.ge;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.model.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A genotype to phenotype mapping where the genotype is list based and the phenotype is an android
 * {@link TestCase}. The mapping caches generated test cases and returns the cached one if the
 * phenotype for the same genotype is requested multiple times.
 *
 * @param <T> The list-based genotype generic type.
 */
public abstract class AndroidListBasedMapping<T> extends AndroidRandomChromosomeFactory
        implements IGenotypePhenotypeMapping<List<T>, TestCase> {

    /**
     * A mapping of a list-based genotype to its phenotype (test case).
     */
    protected final Map<IChromosome<List<T>>, IChromosome<TestCase>> associatedPhenotypeChromosome;

    /**
     * The currently active list-based genotype chromosome.
     */
    protected IChromosome<List<T>> activeGenotypeChromosome;

    /**
     * The current codon index of the active genotype chromosome.
     */
    protected int activeGenotypeCurrentCodonIndex;

    /**
     * Creates the mapping where the generated android {@link TestCase} has the maximal number of
     * actions. The app will be reset before starting the test case.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public AndroidListBasedMapping(int maxNumEvents) {
        this(true, maxNumEvents);
    }

    /**
     * Creates the mapping where the generated android {@link TestCase} has he maximal number of
     * actions.
     *
     * @param resetApp Whether the app should be reset before starting the test case.
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public AndroidListBasedMapping(boolean resetApp, int maxNumEvents) {
        super(resetApp, maxNumEvents);
        associatedPhenotypeChromosome = new HashMap<>();
        activeGenotypeChromosome = null;
        activeGenotypeCurrentCodonIndex = 0;
    }

    /**
     * Creates a new phenotype chromosome and associates it with the currently active genotype
     * chromosome.
     *
     * @return Returns the generated chromosome.
     */
    @Override
    public IChromosome<TestCase> createChromosome() {
        IChromosome<TestCase> phenotypeChromosome = super.createChromosome();
        associatedPhenotypeChromosome.put(activeGenotypeChromosome, phenotypeChromosome);
        activeGenotypeChromosome = null;
        activeGenotypeCurrentCodonIndex = 0;
        return phenotypeChromosome;
    }

    /**
     * Performs the mapping of a genotype to a phenotype chromosome. If no mapping is present,
     * a new phenotype chromosome is created and associated with the current genotype.
     *
     * @param genotype The list-based genotype chromosome.
     * @return Returns the phenotype chromosome.
     */
    @Override
    public IChromosome<TestCase> map(IChromosome<List<T>> genotype) {
        IChromosome<TestCase> phenotypeChromosome = associatedPhenotypeChromosome.get(genotype);
        if (phenotypeChromosome == null) {
            activeGenotypeChromosome = genotype;
            activeGenotypeCurrentCodonIndex = 0;
            return createChromosome();
        }
        return phenotypeChromosome;
    }
}
