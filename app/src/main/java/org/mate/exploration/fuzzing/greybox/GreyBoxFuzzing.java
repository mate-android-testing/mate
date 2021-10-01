package org.mate.exploration.fuzzing.greybox;

import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;

import java.util.ArrayList;
import java.util.List;

public abstract class GreyBoxFuzzing<T> implements Algorithm {

    protected final IChromosomeFactory<T> chromosomeFactory;
    protected final IMutationFunction<T> mutationFunction;
    protected final ITerminationCondition terminationCondition;
    protected final int corpusSize;

    protected final List<IChromosome<T>> crashingInputs;

    public GreyBoxFuzzing(IChromosomeFactory<T> chromosomeFactory,
                          IMutationFunction<T> mutationFunction,
                          ITerminationCondition terminationCondition,
                          int corpusSize) {
        this.chromosomeFactory = chromosomeFactory;
        this.mutationFunction = mutationFunction;
        this.terminationCondition = terminationCondition;
        this.crashingInputs = new ArrayList<>();
        this.corpusSize = corpusSize;
    }

    public List<IChromosome<T>> generateSeedCorpus() {
        List<IChromosome<T>> seedCorpus = new ArrayList<>();
        for (int i = 0; i < corpusSize; i++) {
            seedCorpus.add(chromosomeFactory.createChromosome());
        }
        return seedCorpus;
    }

    public abstract IChromosome<T> chooseNext(List<IChromosome<T>> seedCorpus);

    public abstract int assignEnergy(IChromosome<T> s);

    public abstract boolean isInteresting(IChromosome<T> s);

    public abstract boolean isCrashing(IChromosome<T> s);


    @Override
    public void run() {

        List<IChromosome<T>> seedCorpus = generateSeedCorpus();

        while (!terminationCondition.isMet()) {

            IChromosome<T> s = chooseNext(seedCorpus);
            int p = assignEnergy(s);

            for (int i = 0; i < p; i++) {
                IChromosome<T> sPrime = mutationFunction.mutate(s).get(0);
                if (isCrashing(sPrime)) {
                    crashingInputs.add(sPrime);
                } else if (isInteresting(sPrime)) {
                    seedCorpus.add(sPrime);
                }
            }
        }
    }
}
