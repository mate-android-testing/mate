package org.mate.crash_reproduction.eda.univariate;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.crash_reproduction.eda.IDistributionModel;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

public abstract class VectorBasedDistributionModel<T extends Number> implements IDistributionModel {
    protected final IModelRepresentation modelRepresentation;

    protected VectorBasedDistributionModel(IModelRepresentation modelRepresentation) {
        this.modelRepresentation = modelRepresentation;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        Registry.getUiAbstractionLayer().resetApp();

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            ModelRepresentationIterator iterator = modelRepresentation.getIterator();
            for (int actionsCount = 0; actionsCount < Properties.MAX_NUMBER_EVENTS(); actionsCount++) {
                Action action = Randomness.randomIndexWithProbabilities(iterator.getActionProbabilities());

                if (!testCase.updateTestCase(action, actionsCount)) {
                    return chromosome;
                }

                IScreenState state = Registry.getUiAbstractionLayer().getLastScreenState();
                iterator.updatePositionImmutable(state);
            }
        } finally {
            FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
            CoverageUtils.logChromosomeCoverage(chromosome);
            testCase.finish();
        }
        return chromosome;
    }

    @Override
    public String toString() {
        return modelRepresentation.toString();
    }
}
