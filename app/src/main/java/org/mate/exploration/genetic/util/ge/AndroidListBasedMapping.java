package org.mate.exploration.genetic.util.ge;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.model.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AndroidListBasedMapping<T> extends AndroidRandomChromosomeFactory implements IGenotypePhenotypeMapping<List<T>, TestCase> {
    protected final Map<IChromosome<List<T>>, IChromosome<TestCase>> associatedPhenotypeChromosome;
    protected IChromosome<List<T>> activeGenotypeChromosome;
    protected int activeGenotypeCurrentCodonIndex;

    public AndroidListBasedMapping(int maxNumEvents) {
        this(true, maxNumEvents);
    }

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
