package org.mate.exploration.genetic.util.ge;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.model.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A genotype phenotype mapping where the genotype is list based and the genotype is an android
 * {@link TestCase}. The mapping caches generated test cases and returns the cached one if the
 * phenotype for the same genotype is requested multiple times.
 * @param <T> the type of the list for the list based genotype
 */
public abstract class AndroidListBasedMapping<T> extends AndroidRandomChromosomeFactory implements IGenotypePhenotypeMapping<List<T>, TestCase> {
    protected final Map<IChromosome<List<T>>, IChromosome<TestCase>> associatedPhenotypeChromosome;
    protected IChromosome<List<T>> activeGenotypeChromosome;
    protected int activeGenotypeCurrentCodonIndex;

    /**
     * Creates the mapping where the generated android {@link TestCase} has the given maximum
     * length. The app will be reset before starting the test case.
     * @param maxNumEvents maximum length of the generated test cases
     */
    public AndroidListBasedMapping(int maxNumEvents) {
        this(true, maxNumEvents);
    }

    /**
     * Creates the mapping where the generated android {@link TestCase} has the given maximum
     * length. If the resetApp parameter is true the app will be reset before starting the test
     * case.
     * @param resetApp whether the app should be reset before starting the test case
     * @param maxNumEvents maximum length of the generated test cases
     */
    public AndroidListBasedMapping(boolean resetApp, int maxNumEvents) {
        super(resetApp, maxNumEvents);
        associatedPhenotypeChromosome = new HashMap<>();
        activeGenotypeChromosome = null;
        activeGenotypeCurrentCodonIndex = 0;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        IChromosome<TestCase> phenotypeChromosome = super.createChromosome();
        associatedPhenotypeChromosome.put(activeGenotypeChromosome, phenotypeChromosome);
        activeGenotypeChromosome = null;
        activeGenotypeCurrentCodonIndex = 0;
        return phenotypeChromosome;
    }

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
